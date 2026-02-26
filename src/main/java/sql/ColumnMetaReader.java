package sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import static sql.UpdateSqlBuilder.ColumnMeta;

public final class ColumnMetaReader {

    private static final Set<String> BLACKLIST = Set.of(
        "playerphotolocation", "playeractivation", "playermodificationdate",
        "playerpassword", "playerpreviouspasswords",
        "clubmodificationdate", "club_idclub",
        "coursemodificationdate", "course_idcourse",
        "teemodificationdate", "tee_idtee", "tee_course_idcourse",
        "holenumber", "holemodificationdate",
        "auditstartdate", "auditmodificationdate",
        "cmpdatacompetitionid"
    );

    public List<ColumnMeta> read(Connection conn, String table) throws Exception {

        DatabaseMetaData meta = conn.getMetaData();
        List<ColumnMeta> columns = new ArrayList<>();

        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table, null)) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME").toLowerCase();

                boolean updatable =
                        !BLACKLIST.contains(name)
                        && !"YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT"));

                columns.add(new ColumnMeta(name, updatable));
            }
        }
        return List.copyOf(columns); // 🔒 immuable
    }
}
