package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * Detailed Class for Horse DTOs
 *
 * @param name        name of Horse
 * @param description description of Horse
 * @param dateOfBirth birthdate of Horse
 * @param sex         sex of Horse
 * @param owner       owner of Horse represented as a OwnerDto
 * @param mother      Mother of this Horse represented as a HorseParentDto
 * @param father      Father of this Horse represented as a HorseParentDto
 */
public record HorseDetailDto(
        Long id,
        String name,
        String description,
        LocalDate dateOfBirth,
        Sex sex,
        OwnerDto owner,
        HorseParentDto mother,
        HorseParentDto father
) {
  public HorseDetailDto withId(long newId) {
    return new HorseDetailDto(
            newId,
            name,
            description,
            dateOfBirth,
            sex,
            owner,
            mother,
            father
    );
  }

  public Long ownerId() {
    return owner == null
            ? null
            : owner.id();
  }

  public Long motherId() {
    return mother == null
            ? null
            : mother.id();
  }

  public Long fatherId() {
    return father == null
            ? null
            : father.id();
  }

}
