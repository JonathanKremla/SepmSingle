package at.ac.tuwien.sepm.assignment.individual.rest;

import java.util.List;

/**
 * record representing a ValidationError
 * used to pass validation Error to frontend as DTO
 */
public record ValidationErrorRestDto(
        String message,
        List<String> errors
) {
}
