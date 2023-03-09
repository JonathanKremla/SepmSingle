package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test"})
// enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class OwnerEndpointTest {
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
  }

  @Test
  @Sql(scripts = "/sql/insertData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(scripts = "/sql/reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void gettingAllOwners() throws Exception {
    byte[] body = mockMvc
            .perform(MockMvcRequestBuilders
                    .get("/owners")
                    .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

    List<OwnerDto> ownerResult = objectMapper.readerFor(OwnerDto.class).<OwnerDto>readValues(body).readAll();

    assertThat(ownerResult).isNotNull();
    assertThat(ownerResult.size()).isEqualTo(10);
    assertThat(ownerResult)
            .extracting(OwnerDto::id, OwnerDto::firstName, OwnerDto::lastName, OwnerDto::email)
            .contains(tuple(-1L, "Owner1For", "TestingPurpose", "someone@example.cd"));
  }
}
