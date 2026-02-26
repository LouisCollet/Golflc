package sql_test;

import static interfaces.Log.LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import sql.UpdateSqlBuilder;

class UpdateSqlBuilderTest {

    @Test
    void buildSetClause_shouldReturnSqlAndCount() {
        LOG.debug("entering buildSetClause_shouldReturnSqlAndCount");
        String className = System.getProperty("printClassName");
    if (className != null) {
        LOG.debug("Classe exécutée : " + className);
    }
        // GIVEN : colonnes simulées (sans DB)
        List<UpdateSqlBuilder.ColumnMeta> columns = List.of(
            new UpdateSqlBuilder.ColumnMeta("courseName", true),
            new UpdateSqlBuilder.ColumnMeta("courseId", false), // PK / non-updatable
            new UpdateSqlBuilder.ColumnMeta("coursePar", true),
            new UpdateSqlBuilder.ColumnMeta("courseModificationDate", false) // blacklist
        );

        // WHEN
        UpdateSqlBuilder.SetClause result = UpdateSqlBuilder.build(columns);

        // THEN : SQL exact
        assertEquals(
            "coursename = ?, coursepar = ?",
            result.sql(),
            "SET clause SQL incorrect"
        );

        // THEN : nombre de colonnes
        assertEquals(
            2,
            result.columnCount(),
            "Nombre de colonnes updatables incorrect"
        );
    }
}
