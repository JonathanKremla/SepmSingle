package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseParentDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ErrorListException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles({"test"})
// enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseServiceTest {

  @Autowired
  HorseService horseService;

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void getAllReturnsAllStoredHorses() {
    List<HorseListDto> horses = horseService.allHorses().toList();
    assertThat(horses.size()).isEqualTo(10);
    assertThat(horses)
            .map(HorseListDto::id, HorseListDto::sex)
            .contains(tuple(-1L, Sex.FEMALE));
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void getByIdReturnsCorrectHorse() {
    boolean exceptionThrown = false;
    HorseDetailDto horse = null;
    try {
      horse = horseService.getById(-1L);
    } catch (NotFoundException e) {
      exceptionThrown = true;
    }
    assertThat(exceptionThrown).isFalse();
    assertThat(horse.id()).isEqualTo(-1L);
    assertThat(horse.name()).isEqualTo("Wendy");
    assertThat(horse.description()).isEqualTo("The famous one!");
    assertThat(horse.dateOfBirth()).isEqualTo(LocalDate.of(2012, 12, 12));
    assertThat(horse.sex()).isEqualTo(Sex.FEMALE);

  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testValidationForCreatingHorseForValidHorse() {
    boolean errorThrown = false;
    HorseCreateDto horse = new HorseCreateDto(
            "A valid Horse",
            "A valid Description",
            LocalDate.of(2020, 1, 1),
            Sex.MALE,
            null,
            null,
            null
    );
    HorseDetailDto returnedHorse = null;
    try {
      returnedHorse = horseService.create(horse);
    } catch (ErrorListException e) {
      errorThrown = true;
    }
    assertThat(errorThrown).isFalse();
    assertThat(returnedHorse).isNotNull();
    assertThat(returnedHorse.name()).isEqualTo("A valid Horse");
    assertThat(returnedHorse.description()).isEqualTo("A valid Description");
    assertThat(returnedHorse.dateOfBirth()).isEqualTo(LocalDate.of(2020, 1, 1));
    assertThat(returnedHorse.sex()).isEqualTo(Sex.MALE);
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testValidationForCreatingHorseForInvalidHorse() {
    boolean validationErrorThrown = false;
    boolean conflictErrorThrown = false;
    HorseCreateDto horseCreateDto = new HorseCreateDto(
            "An invalid Horse",
            "",
            LocalDate.of(2100, 1, 1),
            Sex.MALE,
            null,
            null,
            null
    );

    try {
      horseService.create(horseCreateDto);
    } catch (ValidationException e) {
      assertThat(e.getMessage()).isEqualTo("Error(s) creating Horse. Failed validations: "
              + "Horse Date of birth can't be in the future, "
              + "Horse description is given but blank.");
      validationErrorThrown = true;
    } catch (ConflictException e) {
      conflictErrorThrown = true;
    }
    assertThat(validationErrorThrown).isTrue();
    assertThat(conflictErrorThrown).isFalse();
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void testValidtionOfValidParentChildRelation() {
    boolean exceptionThrown = false;
    HorseDetailDto oldChild = null;
    HorseDetailDto tempFather = null;
    HorseDetailDto tempMother = null;
    try {
      oldChild = horseService.getById(-4);
      tempFather = horseService.getById(-3);
      tempMother = horseService.getById(-2);
    } catch (NotFoundException ignored) {
      exceptionThrown = true;
    }
    assertThat(exceptionThrown).isFalse();
    HorseParentDto father = new HorseParentDto(
            tempFather.id(),
            tempFather.name(),
            tempFather.description(),
            tempFather.dateOfBirth(),
            tempFather.sex()
    );

    HorseParentDto mother = new HorseParentDto(
            tempMother.id(),
            tempMother.name(),
            tempMother.description(),
            tempMother.dateOfBirth(),
            tempMother.sex()
    );


    HorseDetailDto newChild = new HorseDetailDto(
            oldChild.id(),
            oldChild.name(),
            oldChild.description(),
            oldChild.dateOfBirth(),
            oldChild.sex(),
            null,
            mother,
            father
    );
    exceptionThrown = false;
    HorseDetailDto returnedChild = null;
    try {
      returnedChild = horseService.update(newChild);
    } catch (NotFoundException | ValidationException | ConflictException e) {
      exceptionThrown = true;
    }

    assertThat(exceptionThrown).isFalse();
    assertThat(returnedChild).isNotNull();
    assertThat(returnedChild.motherId()).isEqualTo(mother.id());
    assertThat(returnedChild.fatherId()).isEqualTo(father.id());

  }
}

