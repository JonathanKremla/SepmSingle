package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseParentDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;

  public HorseServiceImpl(HorseDao dao, HorseMapper mapper, HorseValidator validator, OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  @Override
  public Stream<HorseListDto> allHorses() {
    LOG.trace("allHorses()");
    HorseSearchDto emptyParams = new HorseSearchDto(
            null, null,
            null,
            null,
            null,
            null);
    return searchForHorses(emptyParams);
  }

  @Override
  public HorseFamilyTreeDto getFamilyTree(Long id, Long generations) throws NotFoundException {
    LOG.trace("getFamilyTree({})", id);
    if (generations <= 0) {
      generations = 0L;
    }
    if (generations > 100) {
      generations = 100L;
    }
    List<Horse> ancestorsList = dao.getFamilyTree(id, generations);
    return mapper.entityToFamilyTreeDto(id, ancestorsList);
  }

  @Override
  public Stream<HorseListDto> searchForHorses(HorseSearchDto searchParams) {
    LOG.trace("searchForHorses({})", searchParams);
    var horses = dao.searchForHorses(searchParams);
    var ownerIds = horses.stream()
            .map(Horse::getOwnerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());

    Map<Long, OwnerDto> ownerMap;
    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }

    return horses.stream().map(horse -> mapper.entityToListDto(horse, ownerMap));
  }

  @Override
  public HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", horse);
    validator.validateForUpdate(horse);
    if (isParent(horse)) {
      List<HorseDetailDto> children = dao.getAllChildren(horse)
              .stream()
              .map((child) -> mapper.entityToDetailDto(child,
                      ownerMapForSingleId(child.getOwnerId()),
                      parentMapForSingleId(child.getMotherId()),
                      parentMapForSingleId(child.getFatherId()))).toList();
      validator.validateParentChildRelation(children, horse, dao.getById(horse.id()).getSex());
    }
    var updatedHorse = dao.update(horse);
    return mapper.entityToDetailDto(
            updatedHorse,
            ownerMapForSingleId(updatedHorse.getOwnerId()),
            parentMapForSingleId(updatedHorse.getMotherId()),
            parentMapForSingleId(updatedHorse.getFatherId()));
  }


  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    return mapper.entityToDetailDto(
            horse,
            ownerMapForSingleId(horse.getOwnerId()),
            parentMapForSingleId(horse.getMotherId()),
            parentMapForSingleId(horse.getFatherId()));
  }

  @Override
  public HorseDetailDto create(HorseCreateDto horse) throws ValidationException, ConflictException {
    LOG.trace("create({})", horse);
    validator.validateForCreation(horse);
    var newHorse = dao.create(horse);
    return mapper.entityToDetailDto(
            newHorse,
            ownerMapForSingleId(newHorse.getOwnerId()),
            parentMapForSingleId(newHorse.getMotherId()),
            parentMapForSingleId(newHorse.getFatherId()));
  }

  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);
    dao.delete(id);
  }

  /**
   * checks if given horse is parent of any other horse
   *
   * @param horse given horse to be checked for children
   * @return true if horse is parent, false otherwise
   */
  private boolean isParent(HorseDetailDto horse) {
    LOG.trace("isParent({})", horse);
    return dao.isParent(horse);
  }


  /**
   * maps an owner to a Map with a single key value pair
   *
   * @param ownerId id of owner to be mapped
   * @return a Map with one pair, with the key being the owners id and the value being the representative {@link OwnerDto}
   */
  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    LOG.trace("ownerMapForSingleId({})", ownerId);
    try {
      return ownerId == null
              ? null
              : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

  /**
   * maps a horse, representing a Parent, to a Map with a single key value pair
   *
   * @param parentId id of horse to be mapped
   * @return a Map with one pair, with the key being the horses id and the value being
   *        the representative {@link HorseParentDto}
   */
  private Map<Long, HorseParentDto> parentMapForSingleId(Long parentId) {
    LOG.trace("parentMapForSingleId({})", parentId);
    try {
      return parentId == null
              ? null
              : Collections.singletonMap(parentId, mapper.detailDtoToParentDto(getById(parentId)));
    } catch (NotFoundException e) {
      throw new FatalException("Parent %d referenced by horse not found".formatted(parentId));
    }
  }

}
