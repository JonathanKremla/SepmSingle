package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.invoke.MethodHandles;

/**
 * Class for handling {@link ValidationException} and {@link ConflictException}
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * handles Validation Exceptions {@link ValidationException}
   *
   * @param e ValidationException e
   * @return a {@link ValidationErrorRestDto} wrapping ValidationException {@code e}
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ValidationErrorRestDto handleValidationException(ValidationException e) {
    LOG.error("Terminating request processing with status 422 due to {}: {}", e.getClass().getSimpleName(), e.getMessage());
    return new ValidationErrorRestDto(e.summary(), e.errors());
  }

  /**
   * handles Conflict Exceptions {@link ConflictException}
   *
   * @param e ConflictException e
   * @return a {@link ConflictErrorRestDto} wrapping ConflictException {@code e}
   */
  @ExceptionHandler
  @ResponseStatus(HttpStatus.CONFLICT)
  @ResponseBody
  public ConflictErrorRestDto handleConflictException(ConflictException e) {
    LOG.error("Terminating request processing with status 409 due to {}: {}", e.getClass().getSimpleName(), e.getMessage());
    return new ConflictErrorRestDto(e.summary(), e.errors());
  }
}
