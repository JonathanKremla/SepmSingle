package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = HorseEndpoint.BASE_PATH)
public class HorseEndpoint {
  static final String BASE_PATH = "/horses";
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseService service;

  public HorseEndpoint(HorseService service) {
    this.service = service;
  }

  /**
   * REST Endpoint for searching for Horses
   * <p>
   * A Horse is considered matched, if it matches all the criteria given in {@code searchParameters}
   * Strings ,name/description/owner , are matched if it contains {@code searchParameters.name}/{@code searchParameters.description}
   * /{@code searchParameters.owner} as a substring.
   * The birthdate is matched if it is before {@code searchParameters.bornBefore}
   * Sex needs to be an exact match, male or female
   * If searchParams.limit is not null the returned stream of horses never
   * contains more than {@code searchParameters.limit} elements,
   * even if there would be more matches in the persistent data store.
   * If searchParams.limit is null every matching element is returned in the stream
   * </p>
   *
   * @param searchParameters {@link HorseSearchDto} parameters to be searched for
   * @return a Stream containing all Horses matching the given searchParameters
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public Stream<HorseListDto> searchHorses(HorseSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH + " query Parameters: {}", searchParameters);
    LOG.debug("request parameters: \n{}", searchParameters);
    return service.searchForHorses(searchParameters);
  }

  /**
   * REST Endpoint for retrieving a Horse with specified id
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   */
  @GetMapping("{id}")
  @ResponseStatus(HttpStatus.OK)
  public HorseDetailDto getById(@PathVariable long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * REST Endpoint for updating a Horse
   * Updates the horse with the ID given in {@code toUpdate}
   * with the data given in {@code toUpdate}
   * in the persistent data store.
   *
   * @param toUpdate the horse to update
   * @return the updated horse
   * @throws ValidationException if the update data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException   if the update data given for the horse is in conflict the data currently in the system (child older than parent, …)
   */
  @PutMapping("{id}")
  @ResponseStatus(HttpStatus.OK)
  public HorseDetailDto update(@PathVariable long id, @RequestBody HorseDetailDto toUpdate) throws ValidationException, ConflictException {
    LOG.info("PUT " + BASE_PATH + "/{}", toUpdate);
    LOG.debug("Body of request:\n{}", toUpdate);
    try {
      return service.update(toUpdate.withId(id));
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to update not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * REST Endpoint for creating a new Horse
   * Create a new Horse with Data given in {@code newHorse}
   * stores the horse in the persistent data store and
   * generates a unique ID.
   *
   * @param newHorse the horse to create
   * @return Created Horse with generated ID
   * @throws ValidationException if the data given for the horse is in itself incorrect(description too long, no name, ...)
   * @throws ConflictException   if the data given for the horse is in conflict with the data currently in the system (child older than parent, ...)
   */
  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public HorseDetailDto create(@RequestBody HorseCreateDto newHorse) throws ValidationException, ConflictException {
    LOG.info("CREATE " + BASE_PATH + "/{}", newHorse);
    LOG.debug("Body of request:\n{}", newHorse);
    return service.create(newHorse);
  }

  /**
   * REST Endpoint for retrieving a FamilyTree of a given horse with id={@code id}
   * get the Ancestors of the specified(by id) horse as a {@link HorseFamilyTreeDto}
   * up to (including) the given generation
   *
   * @param id          id of the root horse of the family Tree
   * @param generations number of generations which are extracted
   * @return a {@link HorseFamilyTreeDto} representing the FamilyTree of the horse
   */
  @GetMapping("{id}/familytree")
  @ResponseStatus(HttpStatus.OK)
  public HorseFamilyTreeDto getFamilyTree(@PathVariable("id") long id, long generations) {
    LOG.info("GET " + BASE_PATH + "/{}/familytree?generations={}", id, generations);
    try {
      return service.getFamilyTree(id, generations);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to retrieve FamilyTree from not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  /**
   * REST Endpoint for deleting a Horse
   * Deletes a Horse with the id given {@code id}
   * deletes every relationship including this horse(parent-child
   * , child-parent, owner-horse)
   *
   * @param id the ID of the horse to delete
   */
  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    LOG.info("DELETE " + BASE_PATH + "/{}", id);
    try {
      service.delete(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to delete not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }


  /**
   * logs Error cause by Client
   *
   * @param status  HttpStatus thrown
   * @param message error message
   * @param e       Exception
   */
  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.error("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }
}
