package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

@RestController
@RequestMapping(OwnerEndpoint.BASE_PATH)
public class OwnerEndpoint {
  static final String BASE_PATH = "/owners";
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final OwnerService service;

  public OwnerEndpoint(OwnerService service) {
    this.service = service;
  }

  /**
   * REST Endpoint for searching for Owners
   * Search for owners matching the criteria in {@code searchParameters}.
   * <p>
   * A owner is considered matched, if its name contains {@code searchParameters.name} as a substring.
   * The returned stream of owners never contains more than {@code searchParameters.maxAmount} elements,
   * even if there would be more matches in the persistent data store.
   * </p>
   *
   * @param searchParameters object containing the search parameters to match
   * @return a stream containing owners matching the criteria in {@code searchParameters}
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH + " query parameters: {}", searchParameters);
    LOG.debug("Request parameters: \n{}", searchParameters);
    return service.search(searchParameters);
  }

  /**
   * REST Endpoint for creating a new owner
   * Create a new owner in the persistent data store.
   *
   * @param newOwner the data for the new owner
   * @return {@link OwnerDto} of the created horse
   * @throws ValidationException if the data given for the owner is in itself incorrect(no name, ...)
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OwnerDto create(@RequestBody OwnerCreateDto newOwner) throws ValidationException, ConflictException {
    LOG.info("Create " + BASE_PATH + "/{}", newOwner);
    LOG.debug("Body of request:\n{}", newOwner);
    return service.create(newOwner);
  }

}
