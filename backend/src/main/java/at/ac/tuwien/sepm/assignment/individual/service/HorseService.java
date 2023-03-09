package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseFamilyTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;

import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {
  /**
   * Lists all horses stored in the system.
   *
   * @return list of all stored horses
   */
  Stream<HorseListDto> allHorses();


  /**
   * Updates the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException   if the horse with given ID does not exist in the persistent data store
   * @throws ValidationException if the update data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException   if the update data given for the horse is in conflict the data currently in the system (child older than parent, …)
   */
  HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException;


  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  /**
   * Create a new Horse with Data given in {@code horse}
   * stores the horse in the persistent data store and
   * generates a unique ID.
   *
   * @param horse the horse to create
   * @return Created Horse with generated ID
   * @throws ValidationException if the data given for the horse is in itself incorrect(description too long, no name, ...)
   * @throws ConflictException   if the data given for the horse is in conflict with the data currently in the system (child older than parent, ...)
   */
  HorseDetailDto create(HorseCreateDto horse) throws ValidationException, ConflictException;

  /**
   * Deletes a Horse with the id given {@code id}
   * deletes every relationship including this horse(parent-child
   * , child-parent, owner-horse)
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  void delete(long id) throws NotFoundException;

  /**
   * Search for Horses matching the criteria in {@code searchParameters}.
   * <p>
   * A Horse is considered matched, if it matches all the criteria given in {@code searchParams}
   * Strings ,name/description/owner , are matched if it contains {@code searchParams.name}/{@code searchParams.description}
   * /{@code searchParams.owner} as a substring.
   * The birthdate is matched if it is before {@code searchParams.bornBefore}
   * Sex needs to be an exact match, male or female
   * If searchParams.limit is not null the returned stream of horses never
   * contains more than {@code searchParams.limit} elements,
   * even if there would be more matches in the persistent data store.
   * If searchParams.limit is null every matching element is returned in the stream
   * </p>
   *
   * @param searchParams object containing the search parameters to match
   * @return a Stream containing horses matching the criteria in {@code searchParams}
   */
  Stream<HorseListDto> searchForHorses(HorseSearchDto searchParams);

  /**
   * get the Ancestors of the specified(by id) horse as a {@link HorseFamilyTreeDto}
   * up to (including) the given generation
   *
   * @param id          id of the root horse of the family Tree
   * @param generations number of generations which are extracted
   * @return a {@link HorseFamilyTreeDto} representing the FamilyTree of the horse
   * @throws NotFoundException if Horse with id is not found in system
   */
  HorseFamilyTreeDto getFamilyTree(Long id, Long generations) throws NotFoundException;
}
