package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;

import java.util.Collection;
import java.util.List;

/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {
  /**
   * Get all horses stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  List<Horse> getAll();


  /**
   * Update the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(HorseDetailDto horse) throws NotFoundException;

  /**
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  /**
   * Create a new Horse with Data given in {@code horse}
   * stores the horse in the persistent data store and
   * generates a unique ID.
   *
   * @param horseCreateDto the horse to create
   * @return created Horse with generated ID
   */
  Horse create(HorseCreateDto horseCreateDto);

  /**
   * Deletes a Horse with the id given {@code id}
   * deletes every relationship including this horse (parent-child
   * , child-parent, owner-horse)
   *
   * @param id the ID of the horse to delete
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  void delete(long id) throws NotFoundException;

  /**
   * Is true if given horse is Parent of any other Horse
   *
   * @param horse horse to be checked for children
   * @return true iff parent
   */
  boolean isParent(HorseDetailDto horse);

  /**
   * Get all Children of a given Horse
   *
   * @param horse horse to retrieve children from
   * @return a Collection of Horses which are children to given horse
   */
  Collection<Horse> getAllChildren(HorseDetailDto horse);

  /**
   * Search for Horses matching the criteria in {@code searchParameters}.
   * <p>
   * A Horse is considered matched, if it matches all the criteria given in {@code searchParams}
   * Strings ,name/description/owner , are matched if it contains {@code searchParams.name}/{@code searchParams.description}
   * /{@code searchParams.owner} as a substring.
   * The birthdate is matched if it is before {@code searchParams.bornBefore}
   * Sex needs to be an exact match, male or female
   * If searchParams.limit is not null the returned stream of horses never
   * contains more than {@code searchParameters.limit} elements,
   * even if there would be more matches in the persistent data store.
   * If searchParams.limit is null every matching element is returned in the stream
   * </p>
   *
   * @param searchParams object containing the search parameters to match
   * @return List containging Horses matching criteria in {@code searchParams}
   */
  List<Horse> searchForHorses(HorseSearchDto searchParams);

  /**
   * Get a List of Horses representing the ancestors of the Horse specified by id.
   *
   * @param id          the id of the root horse of the family tree
   * @param generations number of generations of ancestors
   * @return returns a List of all ancestors up the specified generation
   * @throws NotFoundException if horse with given id is not found in system
   */
  List<Horse> getFamilyTree(Long id, Long generations) throws NotFoundException;
}
