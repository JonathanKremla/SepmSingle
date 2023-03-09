package at.ac.tuwien.sepm.assignment.individual.persistence.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";
  private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_SELECT_ALL_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id IN (:ids)";
  private static final String SQL_SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_UPDATE = "UPDATE " + TABLE_NAME
          + " SET name = ?"
          + "  , description = ?"
          + "  , date_of_birth = ?"
          + "  , sex = ?"
          + "  , owner_id = ?"
          + "  , mother_id = ?"
          + "  , father_id = ?"
          + " WHERE id = ?";
  private static final String SQL_CREATE = "INSERT INTO " + TABLE_NAME
          + " (name, description, date_of_birth, sex, owner_id, mother_id, father_id)"
          + " VALUES(?,?,?,?,?,?,?)";
  private static final String SQL_REMOVE_FATHER = "UPDATE " + TABLE_NAME + " SET father_id = NULL WHERE father_id = ?";
  private static final String SQL_REMOVE_MOTHER = "UPDATE " + TABLE_NAME + " SET mother_id = NULL WHERE mother_id = ?";
  private static final String SQL_SELECT_ALL_CHILDREN = "SELECT * FROM " + TABLE_NAME + " WHERE mother_id = ? OR father_id = ?";
  private static final String SQL_SEARCH_WITH_PARAMS =
          "SELECT * FROM " + TABLE_NAME
                  + " LEFT JOIN owner ON " + TABLE_NAME + ".owner_id = owner.id"
                  + " WHERE (? IS NULL OR UPPER(name) LIKE UPPER('%'||?||'%'))" //matching name
                  + " AND (? IS NULL OR UPPER(description) LIKE UPPER('%'||?||'%'))"  //matching description
                  + " AND (? IS NULL OR sex = ?)" //matching sex
                  + " AND (? IS NULL OR date_of_birth < ?)" //matching date of birth
                  + " AND (? IS NULL OR UPPER(first_name||' '||last_name) like UPPER('%'||COALESCE(?, '')||'%'))";
  private static final String SQL_LIMIT = " LIMIT ?";
  private static final String SQL_RECURSIVE_TREE = "WITH RECURSIVE ancestors"
          + " (id, name, description, date_of_birth, sex, owner_id, mother_id, father_id, recLevel) AS"
          + " (SELECT *, 1 AS recLevel FROM " + TABLE_NAME
          + " WHERE id = ?"
          + " UNION "
          + " SELECT  h.id, h.name, h.description, h.date_of_birth, h.sex, h.owner_id, h.mother_id, h.father_id, (recLevel + 1) AS recLevel"
          + " FROM horse h"
          + " JOIN ancestors a ON (h.id = a.mother_id OR h.id = a.father_id)"
          + " WHERE recLevel < ?)"
          + " SELECT * FROM ancestors";

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate jdbcNamed;

  @Autowired
  public HorseJdbcDao(
          JdbcTemplate jdbcTemplate,
          NamedParameterJdbcTemplate jdbcNamed) {
    this.jdbcTemplate = jdbcTemplate;
    this.jdbcNamed = jdbcNamed;
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    return jdbcTemplate.query(SQL_SELECT_ALL, this::mapRow);
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses;
    horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.get(0);
  }

  @Override
  public Horse create(HorseCreateDto horse) {
    LOG.trace("create({})", horse);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    int updated = jdbcTemplate.update(
            connection -> {
              PreparedStatement ps = connection.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
              ps.setString(1, horse.name());
              ps.setString(2, horse.description());
              ps.setDate(3, Date.valueOf(horse.dateOfBirth()));
              ps.setString(4, horse.sex().toString());
              if (horse.ownerId() == null) {
                ps.setNull(5, Types.BIGINT);
              } else {
                ps.setLong(5, horse.ownerId());
              }
              if (horse.motherId() == null) {
                ps.setNull(6, Types.BIGINT);
              } else {
                ps.setLong(6, horse.motherId());
              }
              if (horse.fatherId() == null) {
                ps.setNull(7, Types.BIGINT);
              } else {
                ps.setLong(7, horse.fatherId());
              }
              return ps;
            }, keyHolder);

    if (updated == 0) {
      throw new FatalException("Could not create Horse");
    }
    Number key = keyHolder.getKey();
    if (key == null) {
      throw new FatalException("Could not generate Id for horse");
    }

    return new Horse()
            .setId(key.longValue() == 0 ? null : key.longValue())
            .setName(horse.name())
            .setDescription(horse.description())
            .setDateOfBirth(horse.dateOfBirth())
            .setSex(horse.sex())
            .setOwnerId(horse.ownerId())
            .setMotherId(horse.motherId())
            .setFatherId(horse.fatherId());


  }

  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);
    try {
      getById(id);
    } catch (NotFoundException e) {
      throw new NotFoundException("Could not delete horse with ID " + id + ", because it does not exist");
    }
    int updated = jdbcTemplate.update(SQL_DELETE, id);
    if (updated == 0) {
      throw new FatalException("Could not delete Horse with ID " + id);
    }
  }


  @Override
  public boolean isParent(HorseDetailDto horse) {
    LOG.trace("isParent({})", horse);
    return !getAllChildren(horse).isEmpty();
  }

  @Override
  public Collection<Horse> getAllChildren(HorseDetailDto horse) {
    LOG.trace("getAllChildren({})", horse);
    return jdbcTemplate.query(SQL_SELECT_ALL_CHILDREN, this::mapRow, horse.id(), horse.id());
  }

  public List<Horse> getFamilyTree(Long id, Long generations) throws NotFoundException {
    LOG.trace("getFamilyTree({})", id);
    List<Horse> horses = jdbcTemplate.query(SQL_RECURSIVE_TREE, this::mapRow, id, generations);
    if (horses.isEmpty()) {
      throw new NotFoundException("Horse not Found");
    }
    if (generations == 0) {
      //return empty list
      return new ArrayList<>();
    }
    return horses;
  }

  @Override
  public List<Horse> searchForHorses(HorseSearchDto params) {
    LOG.trace("SearchForHorse({})", params);
    final String SQL_SEARCH = params.limit() == null ? SQL_SEARCH_WITH_PARAMS : SQL_SEARCH_WITH_PARAMS + SQL_LIMIT;
    if (params.limit() == null) {
      return jdbcTemplate.query(SQL_SEARCH, this::mapRow,
              params.name(), params.name(),
              params.description(), params.description(),
              params.sex(), params.sex() != null ? params.sex().toString() : "",
              params.bornBefore(), params.bornBefore(),
              params.ownerName(), params.ownerName()
      );
    } else {
      return jdbcTemplate.query(SQL_SEARCH, this::mapRow,
              params.name(), "%" + params.name() + "%",
              params.description(), "%" + params.description() + "%",
              params.sex(), params.sex() != null ? params.sex().toString() : "",
              params.bornBefore(), params.bornBefore(),
              params.ownerName(), params.ownerName(),
              params.limit()
      );
    }
  }

  @Override
  public Horse update(HorseDetailDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcTemplate.update(SQL_UPDATE,
            horse.name(),
            horse.description(),
            horse.dateOfBirth(),
            horse.sex().toString(),
            horse.ownerId(),
            horse.motherId(),
            horse.fatherId(),
            horse.id());

    if (updated == 0) {
      throw new NotFoundException("Could not update horse with ID " + horse.id() + ", because it does not exist");
    }

    return new Horse()
            .setId(horse.id())
            .setName(horse.name())
            .setDescription(horse.description())
            .setDateOfBirth(horse.dateOfBirth())
            .setSex(horse.sex())
            .setOwnerId(horse.ownerId())
            .setMotherId(horse.motherId())
            .setFatherId(horse.fatherId())
            ;
  }


  /**
   * Maps ResultSet to Object of Horse
   *
   * @param result ResultSet to map
   * @param rownum number of the current row
   * @return the result object(Horse) for the current row (may be null)
   * @throws SQLException if an SQLException is encountered getting column values
   */
  private Horse mapRow(ResultSet result, int rownum) throws FatalException {
    LOG.trace("mapRow({},{})", result, rownum);
    try {
      return new Horse()
              .setId(result.getLong("id"))
              .setName(result.getString("name"))
              .setDescription(result.getString("description"))
              .setDateOfBirth(result.getDate("date_of_birth").toLocalDate())
              .setSex(Sex.valueOf(result.getString("sex")))
              .setOwnerId(result.getObject("owner_id", Long.class))
              .setMotherId(result.getObject("mother_id", Long.class))
              .setFatherId(result.getObject("father_id", Long.class))
              ;
    } catch (SQLException e) {
      throw new FatalException(e);
    }
  }
}
