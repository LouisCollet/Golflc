package read;

import entite.Club;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import rowmappers.ClubRowMapper;
import rowmappers.RowMapper;
import utils.LCUtil;
import connection_package.ConnectionProvider;
import connection_package.TestDB;

@ApplicationScoped
public class ReadClub2 {

    // Injection via constructeur avec qualifier @TestDB
     @Inject
    private ConnectionProvider connectionProvider;

    @Inject
    public ReadClub2(@TestDB ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public Club read(Club club) throws SQLException, Exception {
        final String methodName = LCUtil.getCurrentMethodName();

        final String query = """
            SELECT *
            FROM Club
            WHERE idclub = ?
        """;

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, club.getIdclub());
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                RowMapper<Club> clubMapper = new ClubRowMapper();
                if (rs.next()) {
                    club = clubMapper.map(rs);
                } else {
                    LOG.debug("No club found with id = {}", club.getIdclub());
                    throw new Exception("No club found with id: " + club.getIdclub());
                }
            }

            return club;

  } catch (SQLException e) {
    handleSQLException(e, methodName);
    throw e;  // Propager l'exception
} catch (Exception e) {
    handleGenericException(e, methodName);
    throw e;
    }
    }

    // Optionnel : méthode main pour tests rapides hors CDI
    /*
    public static void main(String[] args) throws SQLException {
        ReadClub2 readClub = new ReadClub2(new TestConnectionProvider()); // utilisation directe du provider de test
        Club club = new Club();
        club.setIdclub(154);
        Club c = readClub.read(club);
        if (c != null) {
            LOG.debug("Club loaded = {}", c);
        }
    }
    */
}
