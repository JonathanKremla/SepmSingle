package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test"})
// enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class HorseEndpointTest {

  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;

  private static String asJsonString(final Object obj) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void gettingAllHorses() throws Exception {
    byte[] body = mockMvc
            .perform(MockMvcRequestBuilders
                    .get("/horses")
                    .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horseResult = objectMapper.readerFor(HorseListDto.class).<HorseListDto>readValues(body).readAll();

    assertThat(horseResult).isNotNull();
    assertThat(horseResult.size()).isEqualTo(10);
    assertThat(horseResult)
            .extracting(HorseListDto::id, HorseListDto::name)
            .contains(tuple(-1L, "Wendy"));
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void gettingNonexistentUrlReturns404() throws Exception {
    mockMvc
            .perform(MockMvcRequestBuilders
                    .get("/asdf123")
            ).andExpect(status().isNotFound());
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void getHorseWithId() throws Exception {
    byte[] body = mockMvc
            .perform(MockMvcRequestBuilders
                    .get("/horses/-1")
                    .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
    HorseDetailDto horseResult = (HorseDetailDto) objectMapper.readerFor(HorseDetailDto.class).readValues(body).next();

    assertThat(horseResult).isNotNull();
    assertThat(horseResult.id()).isEqualTo(-1L);
    assertThat(horseResult.name()).isEqualTo("Wendy");
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void createHorse() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
                    .post("/horses")
                    .content(asJsonString(new HorseCreateDto("TestHorse", "lalala", LocalDate.of(2020, 1, 1), Sex.MALE, null, null, null)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("TestHorse"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("lalala"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.dateOfBirth[0]").value("2020"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.dateOfBirth[1]").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.dateOfBirth[2]").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sex").value("MALE"));
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void createHorseWithInvalidBodyReturns400() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
                    .post("/horses")
                    .content("lalalal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist());
  }
}
