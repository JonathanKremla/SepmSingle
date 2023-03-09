package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator for Owners
 */
@Component
public class OwnerValidator {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Validate Owner for Creation
   *
   * @param owner data of owner to be created
   * @throws ValidationException if the data given for the owner is in itself incorrect (no name, ...)
   */
  public void validateOwnerForCreation(OwnerCreateDto owner) throws ValidationException {
    LOG.trace("validateOwnerForCreation({})", owner);

    List<String> validationErrors = new ArrayList<>();
    if (owner.firstName() == null || owner.firstName().isBlank()) {
      validationErrors.add("First Name of Owner cannot be blank");
    }
    if (owner.lastName() == null || owner.lastName().isBlank()) {
      validationErrors.add("Last Name of Owner cannot be blank");
    }
    if (owner.lastName() != null && owner.lastName().length() > 255) {
      validationErrors.add("Last Name too long, max length = 255");
    }
    if (owner.firstName() != null && owner.firstName().length() > 255) {
      validationErrors.add("First Name too long, max length = 255");
    }

    if (owner.email() != null) {
      if (owner.email().length() > 255) {
        validationErrors.add("Email too long, max length = 255");
      }
      String regex = "^[\\w!#$%&'+/=?`{|}~^-]+(?:\\.[\\w!#$%&'+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
      Pattern emailPattern = Pattern.compile(regex);
      Matcher matcher = emailPattern.matcher(owner.email());
      if (!matcher.matches()) {
        validationErrors.add("Email must be of form: example@mail.com");
      }
    }
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation for owners failed: ", validationErrors);
    }
  }
}
