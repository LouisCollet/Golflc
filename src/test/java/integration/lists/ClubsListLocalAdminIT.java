package integration.lists;

import connection_package.JdbcConnectionProvider;
import static interfaces.Log.LOG;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
public class ClubsListLocalAdminIT {

    private static final int    LOCAL_ADMIN_ID = 324713;
    private static final String QUERY          = lists.ClubsListLocalAdmin.QUERY;
    private static final String EXPLAIN_QUERY  = "EXPLAIN ANALYZE " + QUERY;

    @Test
    void query_realDB_executesWithoutError() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY)) {

            ps.setInt(1, LOCAL_ADMIN_ID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idclub    = rs.getInt("idclub");
                    String name   = rs.getString("ClubName");
                    int adminId   = rs.getInt("ClubLocalAdmin");

                    assertTrue(idclub > 0, "idclub doit être > 0");
                    assertNotNull(name, "ClubName ne doit pas être null");
                    assertEquals(LOCAL_ADMIN_ID, adminId, "ClubLocalAdmin doit correspondre au paramètre");

                    LOG.info("club — idclub={} name={}", idclub, name);
                }
            }
        }
    } // end method

    @Test
    void query_explainAnalyze_showsExecutionPlan() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(EXPLAIN_QUERY)) {

            ps.setInt(1, LOCAL_ADMIN_ID);

            try (ResultSet rs = ps.executeQuery()) {
                LOG.info("--- EXPLAIN ANALYZE ClubsListLocalAdmin ---");
                while (rs.next()) {
                    LOG.info("{}", rs.getString(1));
                }
            }
        }
    } // end method

} // end class
