package at.ac.tuwien.sepm.assignment.individual.mapper;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseParentDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mapper for all Horse related Objects
 */
@Component
public class HorseMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public HorseMapper() {
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse  the horse to convert
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseListDto}
   */
  public HorseListDto entityToListDto(Horse horse,
                                      Map<Long, OwnerDto> owners) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseListDto(
            horse.getId(),
            horse.getName(),
            horse.getDescription(),
            horse.getDateOfBirth(),
            horse.getSex(),
            getOwner(horse, owners)
    );
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse   the horse to convert
   * @param owners  a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @param mothers a map of horses by their id, which needs to contain the mother referenced by {@code horse}
   * @param fathers a map of horses by their id, which needs to contain the father referenced by {@code horse}
   * @return the converted {@link HorseListDto}
   */
  public HorseDetailDto entityToDetailDto(
          Horse horse,
          Map<Long, OwnerDto> owners,
          Map<Long, HorseParentDto> mothers,
          Map<Long, HorseParentDto> fathers) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }


    return new HorseDetailDto(
            horse.getId(),
            horse.getName(),
            horse.getDescription(),
            horse.getDateOfBirth(),
            horse.getSex(),
            getOwner(horse, owners),
            getMother(horse, mothers),
            getFather(horse, fathers)
    );
  }

  /**
   * Convert a {@link HorseDetailDto} object to a {@link HorseParentDto}.
   *
   * @param detailDto the horse to convert
   * @return the converted {@link HorseParentDto}
   */
  public HorseParentDto detailDtoToParentDto(HorseDetailDto detailDto) {
    LOG.trace("detailDtoToParentDto({})", detailDto);
    return new HorseParentDto(
            detailDto.id(),
            detailDto.name(),
            detailDto.description(),
            detailDto.dateOfBirth(),
            detailDto.sex()
    );
  }

  /**
   * Convert a List of {@link Horse} representing the family tree of one specific horse, with id, to
   * one {@link HorseFamilyTreeDto}.
   *
   * @param id     id of the root horse of the family tree
   * @param horses horses of all the ancestors of Horse with id
   * @return returns a Single {@link HorseFamilyTreeDto} representing the family Tree of the root Horse
   */
  public HorseFamilyTreeDto entityToFamilyTreeDto(Long id, List<Horse> horses) {
    LOG.trace("entityToFamilyTreeDto({},{})", id, horses);
    HorseFamilyTreeDto horse = null;
    for (int i = 0; i < horses.size(); i++) {
      if (Objects.equals(horses.get(i).getId(), id)) {
        horse = new HorseFamilyTreeDto(
                horses.get(i).getId(),
                horses.get(i).getName(),
                horses.get(i).getDateOfBirth(),
                horses.get(i).getMotherId() != null
                        ? entityToFamilyTreeDto(horses.get(i).getMotherId(), horses)
                        : null,
                horses.get(i).getFatherId() != null
                        ? entityToFamilyTreeDto(horses.get(i).getFatherId(), horses)
                        : null
        );
      }
    }
    return horse;
  }

  /**
   * Retrieve owner from Horse entity
   *
   * @param horse  the horse of which the owner should be retrieved
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the retrieved owner as {@link OwnerDto}
   */
  private OwnerDto getOwner(Horse horse, Map<Long, OwnerDto> owners) {
    LOG.trace("getOwner({},{})", horse, owners);
    OwnerDto owner = null;
    var ownerId = horse.getOwnerId();
    if (ownerId != null) {
      if (!owners.containsKey(ownerId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(horse.getId()));
      }
      owner = owners.get(ownerId);
    }
    return owner;
  }

  /**
   * Retrieve mother from Horse entity
   *
   * @param horse   the horse of which the mother should be retrieved
   * @param mothers a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the retrieved mother as {@link HorseParentDto}
   */
  private HorseParentDto getMother(Horse horse, Map<Long, HorseParentDto> mothers) {
    LOG.trace("getMother({},{})", horse, mothers);
    HorseParentDto mother = null;
    var motherId = horse.getMotherId();
    if (motherId != null) {
      if (!mothers.containsKey(motherId)) {
        throw new FatalException("Given mother map does not contain mother of this Horse (%d)".formatted(horse.getId()));
      }
      mother = mothers.get(motherId);
    }
    return mother;
  }

  /**
   * Retrieve father from Horse entity
   *
   * @param horse   the horse of which the mother should be retrieved
   * @param fathers a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the retrieved father as {@link HorseParentDto}
   */
  private HorseParentDto getFather(Horse horse, Map<Long, HorseParentDto> fathers) {
    LOG.trace("getFather({},{})", horse, fathers);
    HorseParentDto father = null;
    var fatherId = horse.getFatherId();
    if (fatherId != null) {
      if (!fathers.containsKey(fatherId)) {
        throw new FatalException("Given father map does not contain father of this Horse (%d)".formatted(horse.getId()));
      }
      father = fathers.get(fatherId);
    }
    return father;
  }
}
