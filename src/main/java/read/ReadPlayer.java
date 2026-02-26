package read;

import connection_package.ConnectionProvider;
import connection_package.ProdDB;
import entite.Player;
import entite.composite.EPlayerPassword;
import rowmappers.PlayerRowMapper;
import rowmappers.RowMapper;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import utils.LCUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;

/**
 * Classe refactorée pour lire un Player ou EPlayerPassword
 * Similaire au style de CreatePlayer
 */
@ApplicationScoped
public class ReadPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    @ProdDB
    private ConnectionProvider connectionProvider;

      /**
     * DataSource injecté par WildFly
     */
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;
    
    
    /** Constructeur pour MAIN ou tests hors CDI */
    public ReadPlayer() {}

    public ReadPlayer(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    /**
     * Lecture d'un Player simple par id
     */
    public Player read(Player player) throws Exception {
        final String methodName = LCUtil.getCurrentMethodName();
        final String query = """
                SELECT *
                FROM Player
                WHERE idplayer = ?
                """;

        try (Connection conn = connectionProvider.getConnection();
       //   try (Connection conn = datasource.getConnection();        
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {

                RowMapper<Player> mapper = new PlayerRowMapper();

                if (rs.next()) {
                    Player p = mapper.map(rs);
                    LOG.debug("ReadPlayer OK in {}: {}", methodName, p);
                    return p;
                }

                String msg = "Player not found in " + methodName;
                LOG.warn(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            }
        } catch (Exception ex) {
            String msg = "Exception in " + methodName + ": " + ex.getMessage();
            LOG.error(msg, ex);
            LCUtil.showMessageFatal(msg);
            throw ex;
        }
    }

    /**
     * Lecture d'un EPlayerPassword
     */
    public EPlayerPassword read(EPlayerPassword epp) throws Exception {
        final String methodName = LCUtil.getCurrentMethodName();
        final String query = """
                SELECT *
                FROM Player
                WHERE idplayer = ?
                """;

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, epp.player().getIdplayer());
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {

                RowMapper<Player> playerMapper = new PlayerRowMapper();
                EPlayerPassword result = new EPlayerPassword(null, null);

                if (rs.next()) {
                    Player player = playerMapper.map(rs);
                    var password = entite.Password.map(rs); // mapping password
                    result = new EPlayerPassword(player, password);
                    LOG.debug("ReadPlayer EPlayerPassword OK in {}: {}", methodName, result);
                } else {
                    String msg = "Player not found in " + methodName;
                    LOG.warn(msg);
                    LCUtil.showMessageFatal(msg);
                }

                return result;
            }
        } catch (Exception ex) {
            String msg = "Exception in " + methodName + ": " + ex.getMessage();
            LOG.error(msg, ex);
            LCUtil.showMessageFatal(msg);
            throw ex;
        }
    }

    /**
     * Main pour tests hors CDI
     */
    public static void main(String[] args) {
        try {
            ConnectionProvider provider = new connection_package.JdbcConnectionProvider();
            ReadPlayer readPlayer = new ReadPlayer(provider);

            Player p = new Player();
            p.setIdplayer(324715);

            Player loaded = readPlayer.read(p);
            LOG.debug("Loaded Player = {}", loaded);

            // Test EPlayerPassword
            EPlayerPassword epp = new EPlayerPassword(p, null);
            EPlayerPassword loadedEpp = readPlayer.read(epp);
            LOG.debug("Loaded EPlayerPassword = {}", loadedEpp);

        } catch (Exception e) {
            LOG.error("Error in main", e);
        }
    }
}
