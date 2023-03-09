package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * Class for Horse DTOs
 * Contains all common properties
 *
 * @param name        name of Horse
 * @param description description of Horse
 * @param dateOfBirth birthdate of Horse
 * @param sex         sex of Horse
 * @param owner       owner of Horse represented as a OwnerDto
 */
public record HorseListDto(
        Long id,
        String name,
        String description,
        LocalDate dateOfBirth,
        Sex sex,
        OwnerDto owner
) {
  public Long ownerId() {
    return owner == null
            ? null
            : owner.id();
  }
}
