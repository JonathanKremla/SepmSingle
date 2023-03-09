package at.ac.tuwien.sepm.assignment.individual.dto;


/**
 * DTO to encapsulate Data for Owner creation
 *
 * @param firstName first Name of owner
 * @param lastName  last Name of owner
 * @param email     email of Owner
 */
public record OwnerCreateDto(
        String firstName,
        String lastName,
        String email
) {
}
