package at.ac.tuwien.sepm.assignment.individual.dto;

import java.time.LocalDate;

/**
 * DTO representing the family tree of a single Horse,
 * essentially a recursive Datastructure (tree) with the nodes beeing horses and their
 * child nodes their parents.
 *
 * @param name        name of Horse
 * @param dateOfBirth birthdate of Horse
 * @param mother      Mother of this Horse represented recursively as another HorseFamilyTreeDto
 * @param father      Father of this Horse represented recursively as another HorseFamilyTreeDto
 */
public record HorseFamilyTreeDto(
        Long id,
        String name,
        LocalDate dateOfBirth,
        HorseFamilyTreeDto mother,
        HorseFamilyTreeDto father
) {
}
