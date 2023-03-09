package at.ac.tuwien.sepm.assignment.individual.rest;

import java.util.List;

/**
 * record representing a ConflictError
 * used to pass conflict Error to frontend as DTO
 */
public record ConflictErrorRestDto(
        String message,
        List<String> errors
) {
}
