package at.ac.tuwien.sepm.assignment.individual.dto;

/**
 * DTO for Owner
 *
 * @param id        unique ID of owner
 * @param firstName first Name of owner
 * @param lastName  last Name of owner
 * @param email     email of Owner
 */
public record OwnerDto(
        long id,
        String firstName,
        String lastName,
        String email
) {
}
