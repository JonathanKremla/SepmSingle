package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseParentDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator for Horses
 */
@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final OwnerService ownerService;

  protected HorseValidator(OwnerService ownerDao) {
    this.ownerService = ownerDao;
  }

  /**
   * validate Horse for Update
   *
   * @param horse data of horse to be updated
   * @throws ValidationException if the data given for the horse is in itself incorrect(description too long, no name, ...)
   * @throws ConflictException   if the data given for the horse is in conflict with the data currently in the system (child older than parent, ...)
   */
  public void validateForUpdate(HorseDetailDto horse) throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> conflictErrors = new ArrayList<>();
    List<String> validationErrors = new ArrayList<>();

    validationErrors.addAll(validateHorseDateOfBirth(horse.dateOfBirth()));
    validationErrors.addAll(validateHorseName(horse.name()));
    validationErrors.addAll(validateHorseSex(horse.sex()));
    validationErrors.addAll(validateDescription(horse.description()));
    validationErrors.addAll(validateId(horse.id()));
    validateOwner(horse.owner());

    try {
      validateParents(horse.mother(), horse.father(), horse.dateOfBirth());
    } catch (ValidationException e) {
      validationErrors.addAll(e.errors());
    } catch (ConflictException e) {
      conflictErrors.addAll(e.errors());
    }
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Error(s) updating Horse", validationErrors);
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Error(s) updating Horse", conflictErrors);
    }
  }

  /**
   * validate Horse for Creation
   *
   * @param horse data of horse to be created
   * @throws ValidationException if the data given for the horse is in itself incorrect(description too long, no name, ...)
   * @throws ConflictException   if the data given for the horse is in conflict with the data currently in the system (child older than parent, ...)
   */
  public void validateForCreation(HorseCreateDto horse) throws ValidationException, ConflictException {
    LOG.trace("validateForCreation({})", horse);
    List<String> conflictErrors = new ArrayList<>();
    List<String> validationErrors = new ArrayList<>();

    validationErrors.addAll(validateHorseDateOfBirth(horse.dateOfBirth()));
    validationErrors.addAll(validateHorseName(horse.name()));
    validationErrors.addAll(validateHorseSex(horse.sex()));
    validationErrors.addAll(validateDescription(horse.description()));
    validateOwner(horse.owner());

    try {
      validateParents(horse.mother(), horse.father(), horse.dateOfBirth());
    } catch (ValidationException e) {
      validationErrors.addAll(e.errors());
    } catch (ConflictException e) {
      conflictErrors.addAll(e.errors());
    }
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Error(s) creating Horse", validationErrors);
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Error(s) creating Horse", conflictErrors);
    }
  }

  /**
   * validate Relation between a Horse and its Children
   *
   * @param children children of the given parent horse
   * @param parent   parent horse
   * @param oldSex   sex of the parent horse persistent in the system
   * @throws ConflictException if the data given for the parent is in conflict with the data currently in the
   *                           system (child older than parent, ...)
   */
  public void validateParentChildRelation(List<HorseDetailDto> children, HorseDetailDto parent, Sex oldSex) throws ConflictException {
    LOG.trace("validateParentChildRelation({},{},{})", children, parent, oldSex);
    List<String> conflictErrors = new ArrayList<>();
    LocalDate earliestBirthdayOfChild = LocalDate.now();
    for (HorseDetailDto child : children) {
      if (child.dateOfBirth().compareTo(earliestBirthdayOfChild) < 0) {
        earliestBirthdayOfChild = child.dateOfBirth();
      }
    }
    if (earliestBirthdayOfChild.compareTo(parent.dateOfBirth()) < 0) {
      conflictErrors.add("Cannot change birthday to given date because there are one or multiple children of this horse "
              + "with birthdays earlier than this date");
    }
    if (parent.sex() != oldSex) {
      conflictErrors.add("Cannot change Sex of this horse because it is a parent to at least one Horse");
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Error validating Parent-Child relation", conflictErrors);
    }
  }


  /**
   * validate Parents for a horse
   *
   * @param mother           mother of a horse
   * @param father           father of a horse
   * @param childDateOfBirth birthday of the child horse
   * @throws ValidationException if the data of the horse given is in itself incorrect
   * @throws ConflictException   if the data given for the horse is in conflict with the data currently in the system (child older than parent, ...)
   */
  private void validateParents(HorseParentDto mother, HorseParentDto father, LocalDate childDateOfBirth) throws ValidationException, ConflictException {
    LOG.trace("validateParents({}{})", mother, father);
    List<String> conflictErrors = new ArrayList<>();
    if (father != null) {
      if (father.sex() == Sex.FEMALE) {
        conflictErrors.add("Father cannot be Female");
      }
      if (childDateOfBirth != null) {
        if (father.dateOfBirth().compareTo(childDateOfBirth) >= 0) {
          conflictErrors.add("Fathers birthday cannot be after children birthday ");
        }
      }
    }
    if (mother != null) {
      if (mother.sex() == Sex.MALE) {
        conflictErrors.add("Mother cannot be Male ");
      }
      if (childDateOfBirth != null) {
        if (mother.dateOfBirth().compareTo(childDateOfBirth) >= 0) {
          conflictErrors.add("Mothers birthday cannot be after children birthday ");
        }
      }
    }
    if (father != null && mother != null
            && father.sex() == mother.sex()) {
      conflictErrors.add("Horse parents cannot be of the same sex ");
    }
    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Error validating Parents of horse", conflictErrors);
    }
  }


  /**
   * validate Birthday of a horse
   *
   * @param dateOfBirth birthday of a horse
   * @return A list of strings containing the error messages which occurred during the validation
   */
  private List<String> validateHorseDateOfBirth(LocalDate dateOfBirth) {
    LOG.trace("validateHorseDateOfBirth({})", dateOfBirth);
    List<String> validationErrors = new ArrayList<>();

    if (dateOfBirth == null) {
      validationErrors.add("Birthdate of Horse can't be empty");
    } else if (dateOfBirth.compareTo(LocalDate.now()) > 0) {
      validationErrors.add("Horse Date of birth can't be in the future");
    }
    return validationErrors;
  }

  private List<String> validateHorseName(String name) {
    LOG.trace("validateHorseName({})", name);
    List<String> validationErrors = new ArrayList<>();

    if (name == null || name.isBlank()) {
      validationErrors.add("Name of Horse can't be empty");
    } else if (name.length() > 255) {
      validationErrors.add("Horse name is too long: longer than 255 characters");
    }

    return validationErrors;
  }

  /**
   * validate Sex of Horse
   *
   * @param sex {@link Sex} of a horse
   * @return A list of strings containing the error messages which occurred during the validation
   */
  private List<String> validateHorseSex(Sex sex) {
    LOG.trace("validateHorseSex({})", sex);
    List<String> validationErrors = new ArrayList<>();

    if (sex == null) {
      validationErrors.add("Sex of Horse can't be empty");
    }
    return validationErrors;
  }

  /**
   * validate description of Horse
   *
   * @param desc description of a horse
   * @return A list of strings containing the error messages which occurred during the validation
   */
  private List<String> validateDescription(String desc) {
    LOG.trace("validateDescription({})", desc);
    List<String> validationErrors = new ArrayList<>();

    if (desc != null) {
      if (desc.isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (desc.length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }
    return validationErrors;
  }

  /**
   * validate Owner of Horse
   *
   * @param owner owner of a horse
   * @Throws FatalException if owner is not found
   */
  private void validateOwner(OwnerDto owner) throws FatalException {
    LOG.trace("validateOwner({})", owner);
    if (owner != null) {
      try {
        ownerService.getById(owner.id());
      } catch (NotFoundException e) {
        throw new FatalException("Owner %d referenced by Horse not found".formatted(owner.id()));
      }
    }
  }

  /**
   * validate Id for horse
   *
   * @param id of a horse
   * @return List of errors which occured during the validation
   */
  private List<String> validateId(Long id) {
    LOG.trace("validateId({})", id);
    List<String> validationErrors = new ArrayList<>();

    if (id == null) {
      validationErrors.add("No Id given");
    }
    return validationErrors;
  }
}









