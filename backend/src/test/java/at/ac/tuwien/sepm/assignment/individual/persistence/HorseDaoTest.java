package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@ActiveProfiles({"test"})
// enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseDaoTest {

  @Autowired
  HorseDao horseDao;
  @Autowired
  HorseMapper horseMapper;

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void getAllReturnsAllStoredHorses() {
    List<Horse> horses = horseDao.getAll();
    assertThat(horses.size()).isEqualTo(10);
    assertThat(horses)
            .extracting(Horse::getId, Horse::getName)
            .contains(tuple(-1L, "Wendy"));
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void getWendyReturnsWendy() {
    boolean exceptionThrown = false;
    Horse horse = new Horse();
    try {
      horse = horseDao.getById(-1);
    } catch (NotFoundException ignored) {
      exceptionThrown = true;
    }
    assertThat(exceptionThrown).isFalse();
    assertThat(horse).isNotNull();
    assertThat(horse.getName()).isEqualTo("Wendy");
    assertThat(horse.getId()).isEqualTo(-1L);
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void updateHorseReturnsCorrectHorse() {
    Horse horse;
    Horse updatedHorse;

    try {
      horse = horseDao.getById(-1);
      horse.setName("Wendus");
      horse.setSex(Sex.MALE);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
    try {
      updatedHorse = horseDao.update(horseMapper.entityToDetailDto(horse, null, null, null));
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
    assertThat(updatedHorse.getId()).isEqualTo(-1L);
    assertThat(updatedHorse.getName()).isEqualTo("Wendus");
    assertThat(updatedHorse.getSex()).isEqualTo(Sex.MALE);
    assertThat(updatedHorse.getDescription()).isEqualTo(horse.getDescription());
    assertThat(updatedHorse.getDateOfBirth()).isEqualTo(horse.getDateOfBirth());
    assertThat(updatedHorse.getMotherId()).isEqualTo(horse.getMotherId());
    assertThat(updatedHorse.getFatherId()).isEqualTo(horse.getFatherId());
    assertThat(updatedHorse.getOwnerId()).isEqualTo(horse.getOwnerId());

  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void deletionOfInvalidHorseReturnsException() {
    boolean exceptionThrown = false;
    try {
      horseDao.delete(-11L);
    } catch (NotFoundException e) {
      exceptionThrown = true;
      assertThat(e.getMessage()).isEqualTo("Could not delete horse with ID -11, because it does not exist");
    }
    assertThat(exceptionThrown).isTrue();
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void searchForParamsReturnsCorrectSet() {
    HorseSearchDto params = new HorseSearchDto(
            "Sui",
            null,
            null,
            null,
            null,
            null
    );
    List<Horse> returnedHorses = horseDao.searchForHorses(params);
    assertThat(returnedHorses.size()).isEqualTo(3);
    assertThat(returnedHorses)
            .extracting(Horse::getId, Horse::getName)
            .contains(tuple(-2L, "SuitableMother"))
            .contains(tuple(-3L, "SuitableFather"))
            .contains(tuple(-4L, "SuitableChildForSuitableMotherAndSuitableFather"));
  }


}
