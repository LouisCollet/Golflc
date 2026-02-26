
package rowmappers;

import entite.Club;
import java.sql.ResultSet;
import java.sql.SQLException;
// new 17-12-2025

/**
 * Mapper pour Round avec deux paramètres : ResultSet et Club
 */
public interface RowMapperRound<T> {
    T map(ResultSet rs, Club club) throws SQLException;
}


/*
@FunctionalInterface
public interface RowMapperRound<T> {
    T map(ResultSet rs, Club club) throws SQLException; 
    // special deux paramètres
}
*/
