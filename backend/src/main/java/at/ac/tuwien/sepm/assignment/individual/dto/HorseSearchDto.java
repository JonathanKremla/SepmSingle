package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO to bundle the query parameters used in searching horses.
 * Each field can be null, in which case this field is not filtered by.
 *
 * @param name        substring of Horses name
 * @param description substring of Horses description
 * @param bornBefore  LocalDate cutoff for horses birthday
 * @param sex         sex of horse
 * @param ownerName   substring of Owners name
 * @param limit       maximum number of horses to return
 */
public record HorseSearchDto(
        String name,
        String description,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate bornBefore,
        Sex sex,
        String ownerName,
        Integer limit //if this is null every matching horse is returned
) {
}
