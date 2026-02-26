package read;

import entite.Creditcard;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import rowmappers.CreditcardRowMapper;
import rowmappers.RowMapper;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class ReadCreditcard implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ReadCreditcard() { }

    public Creditcard read(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for idplayer = " + player.getIdplayer());

        final String query = """
                SELECT *
                FROM creditcard
                WHERE CreditcardIdPlayer = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                RowMapper<Creditcard> creditcardMapper = new CreditcardRowMapper();
                Creditcard creditcard = new Creditcard();
                while (rs.next()) {
                    creditcard = creditcardMapper.map(rs);
                }
                if (creditcard.getCreditcardNumber() == null) {
                    String msg = prepareMessageBean("creditcard.notfound");
                    LOG.debug(methodName + " - " + msg);
                    showMessageInfo(msg);
                } else {
                    String msg = utils.LCUtil.prepareMessageBean("creditcard.found") + creditcard;
                    LOG.debug(methodName + " - " + msg);
                }
                return creditcard;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Player player = new Player(); player.setIdplayer(324733);
        // Creditcard cc = new ReadCreditcard().read(player);
        // LOG.debug("creditcard found = " + cc.toString());
    } // end main
    */

} // end class
