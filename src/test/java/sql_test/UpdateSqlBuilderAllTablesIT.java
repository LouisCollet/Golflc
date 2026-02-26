package sql_test;

import connection_package.DBConnection2;
import static interfaces.Log.LOG;
import org.junit.jupiter.api.Test;
import sql.ColumnMetaReader;
import sql.UpdateSqlBuilder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateSqlBuilderAllTablesIT {

    @Test
    void buildSetClause_shouldMatchLegacySql_forAllTables() throws Exception {
        try (Connection conn = DBConnection2.getConnection()) {

            DatabaseMetaData meta = conn.getMetaData();
            Map<String, String> baselines = legacyBaselines();

            try (ResultSet tables = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                LOG.debug("tables = " + tables.toString());
                while (tables.next()) {
                    String table = tables.getString("TABLE_NAME");

                    if (!baselines.containsKey(table)) {
                        continue; // table volontairement ignorée
                    }

                    ColumnMetaReader reader = new ColumnMetaReader();
                    var setClause = UpdateSqlBuilder.build(reader.read(conn, table));
                    String generated = setClause.sql();
                    int columnCount = setClause.columnCount();

                    String expected = baselines.get(table);

                    assertEquals(
                            expected,
                            generated,
                            () -> diff(table, expected, generated, columnCount)
                    );
                }
            }
        }
    }

    private static String diff(String table, String expected, String actual, int columnCount) {
        return """
                ❌ SQL NON RÉGRESSIF CASSÉ
                
                TABLE : %s
                
                EXPECTED [%d]:
                %s
                
                ACTUAL   [%d] (colCount=%d):
                %s
                """
                .formatted(
                        table,
                        expected.length(), expected,
                        actual.length(), columnCount,
                        actual
                );
    }

    /**
     * 🔒 BASELINES FIGÉES
     */
    private static Map<String, String> legacyBaselines() {
        LOG.debug("entering legacyBaseLines");
        Map<String, String> m = new LinkedHashMap<>();

        m.put("COURSE",
                "coursename = ?, courseholes = ?, coursepar = ?, " +
                        "coursebegindate = ?, courseenddate = ?"
        );

        m.put("CLUB",
                "clubname = ?, clubaddress = ?, clubzipcode = ?, clubcity = ?"
        );

        m.put("TEE",
                "teename = ?, teecolor = ?, teeslope = ?, teerating = ?"
        );

        // ➕ ajouter ici les tables validées
        return Map.copyOf(m);
    }
}
