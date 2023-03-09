package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

/**
 * DTO representing parent of a Horse
 *
 * @param id          unique ID of Horse
 * @param name        name of Horse
 * @param description description of Horse
 * @param dateOfBirth birthdate of Horse
 * @param sex         sex of Horse
 */
public record HorseParentDto(
        Long id,
        String name,
        String description,
        LocalDate dateOfBirth,
        Sex sex
) {
}
