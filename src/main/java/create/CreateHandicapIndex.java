
package create;

import entite.HandicapIndex;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import javax.sql.DataSource;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import manager.PlayerManager;
import utils.LCUtil;

@ApplicationScoped
public class CreateHandicapIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    @Inject
    private PlayerManager playerManager;

    /**
     * Crée un nouvel handicap index
     * @param handicapIndex à créer
     * @return HandicapIndex créé avec son ID généré
     */
    public HandicapIndex create(final HandicapIndex handicapIndex) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        LOG.debug("entering " + methodName);
        LOG.debug("with HandicapIndex = " + handicapIndex);

        try (Connection conn = dataSource.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "handicap_index");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setNull(1, java.sql.Types.INTEGER);
                ps.setInt(2, handicapIndex.getHandicapPlayerId());
                ps.setInt(3, handicapIndex.getHandicapRoundId());
                ps.setBigDecimal(4, handicapIndex.getHandicapScoreDifferential());
                ps.setTimestamp(5, Timestamp.valueOf(handicapIndex.getHandicapDate()));
                ps.setBigDecimal(6, handicapIndex.getHandicapWHS()); // sera calculé ultérieurement et modifié
                ps.setInt(7, 0); // HandicapExceptionalScoreReduction
                ps.setString(8, "0"); // HandicapSoftHardCap
                ps.setString(9, handicapIndex.getHandicapComment());
                ps.setInt(10, handicapIndex.getHandicapPlayedStrokes());
                ps.setDouble(11, 0.0); // LowHandicapIndex
                ps.setDouble(12, handicapIndex.getHandicapExpectedSD9Holes()); // new 15-04-2025
                ps.setShort(13, handicapIndex.getHandicapHolesNotPlayed()); // new 15-04-2025
                ps.setTimestamp(14, Timestamp.from(Instant.now()));

                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    handicapIndex.setHandicapId(LCUtil.generatedKey(conn));
                    String msg = "HandicapIndex created = " + handicapIndex;
                    LOG.debug(msg);
                    return handicapIndex;
                } else {
                    String msg = "Not successful insert for HandicapIndex = " + handicapIndex;
                    LOG.error(msg);
                    throw new SQLException(msg);
                }
            }

        } catch (SQLException sqle) {
            String msg = "SQLException in " + methodName + ": " + sqle.getMessage()
                    + ", SQLState = " + sqle.getSQLState()
                    + ", ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg, sqle);
            throw sqle;

        } catch (Exception e) {
            String msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg, e);
            throw new SQLException(msg, e);
        }
    } // end method

    void main() throws SQLException {
        try {
            Player player = new Player();
            player.setIdplayer(456782);

            // ✅ PlayerManager déjà migré
            player = playerManager.readPlayer(player.getIdplayer());
            Round round = new Round();
            round.setIdround(717); // test WHS le 11/11/2020
            HandicapIndex index = new HandicapIndex();
            index.setHandicapPlayerId(player.getIdplayer());
            index.setHandicapRoundId(round.getIdround());
            index.setHandicapPlayedStrokes((short) 0);
            index.setHandicapDate(round.getRoundDate().minusDays(1)); // créé à la veille du round
            index.setHandicapScoreDifferential(new BigDecimal("36.0").setScale(3, RoundingMode.HALF_UP));
            index.setHandicapWHS(new BigDecimal("36.0").setScale(3, RoundingMode.HALF_UP));

            HandicapIndex hi = new create.CreateHandicapIndex().create(index);
            LOG.debug("from main, CreateHandicapIndex = " + hi);

        } catch (Exception e) {
            String msg = "Exception in main CreateHandicapIndex: " + e.getMessage();
            LOG.error(msg, e);
        }
    } // end main

} // end Class

/*
import entite.HandicapIndex;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import connection_package.DBConnection;
import jakarta.inject.Inject;
import manager.PlayerManager;
import utils.LCUtil;

public class CreateHandicapIndex {
     // Injection ou récupération du PlayerManager (CDI recommandé)
    @Inject
    private PlayerManager playerManager;
 public HandicapIndex create(final HandicapIndex handicapIndex, final Connection conn) throws SQLException{ 
      final String methodName = utils.LCUtil.getCurrentMethodName();
     PreparedStatement ps = null;
 try{
            LOG.debug("entering " + methodName);
            LOG.debug("  with HandicapIndex = " + handicapIndex);
          final String query = LCUtil.generateInsertQuery(conn, "handicap_index");
          ps = conn.prepareStatement(query);
          ps.setNull(1, java.sql.Types.INTEGER);
          ps.setInt(2, handicapIndex.getHandicapPlayerId()); //
          ps.setInt(3, handicapIndex.getHandicapRoundId());
          ps.setBigDecimal(4, handicapIndex.getHandicapScoreDifferential());
          ps.setTimestamp(5,Timestamp.valueOf(handicapIndex.getHandicapDate()));  
          ps.setBigDecimal(6,handicapIndex.getHandicapWHS()); // handicapWHS - sera calculé ultérieurement et modifié
          ps.setInt(7, 0); //index.getHandicapExceptionalScoreReduction());
          ps.setString(8, "0"); // HandicapSoftHardCap mod 12-11-2020
 // enlevé 22-09-2024
        //handicapIndex.setHandicapComment("initial handicap");//   pour affichage résultat !
             // ou EDS ou COMPET vient d'ou ?
     // complété ou ?
          ps.setString(9, handicapIndex.getHandicapComment());
          ps.setInt(10, handicapIndex.getHandicapPlayedStrokes());
          ps.setDouble(11, 0.0); //handicapIndex.getLowHandicapIndex());
          ps.setDouble(12, handicapIndex.getHandicapExpectedSD9Holes()); //new 15-04-2025
          ps.setShort(13, handicapIndex.getHandicapHolesNotPlayed()); //new 15-04-2025
          ps.setTimestamp(14, Timestamp.from(Instant.now()));
          utils.LCUtil.logps(ps);
          int row = ps.executeUpdate();
       if(row != 0){
          handicapIndex.setHandicapId(LCUtil.generatedKey(conn));
          String msg = "HandicapIndex created = " + handicapIndex;
          LOG.debug(msg);
          LCUtil.showMessageInfo(msg);
          return handicapIndex;
      }else{
          String msg = "<br/><br/>NOT NOT Successful insert for HandicapIndex = " + handicapIndex;
          LOG.debug(msg);
          LCUtil.showMessageFatal(msg);
          return null;
      }
  }catch (SQLException sqle){
            String msg = "£££ exception in " + methodName + " / "  + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }catch (Exception e){
            String msg = "£££ Exception in " + methodName  + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
         // utils.DBConnection.closeQuietly(conn, null, null, ps); // not used because of try-with-resources
        }
    } // end method createHandicapIndex

void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
 try{
     Player player = new Player();
     player.setIdplayer(456782);
     
   //  player = new read.ReadPlayer().read(player, conn);
     player = playerManager.readPlayer(player.getIdplayer());

     Round round = new Round();
     round.setIdround(717);  // test WHS le 11/11/2020 !!!
     round = new read.ReadRound().read(round, conn);
     
     HandicapIndex index = new HandicapIndex();
     index.setHandicapPlayerId(player.getIdplayer());
     index.setHandicapRoundId(round.getIdround());
     index.setHandicapPlayedStrokes((short)0); // new 19-08-2023
     index.setHandicapDate(round.getRoundDate().minusDays(1)); ///modifié 12-11-2020 = créé à la veille du round !!
     index.setHandicapScoreDifferential(new BigDecimal("36.0").setScale(3,RoundingMode.HALF_UP)); //BigDecimal scaled = value.setScale(0, RoundingMode.HALF_UP);
     index.setHandicapWHS(new BigDecimal("36.0").setScale(3,RoundingMode.HALF_UP));
     HandicapIndex hi = new create.CreateHandicapIndex().create(index, conn);
      LOG.debug("from main, CreateHandicapIndex = " + hi);
 }catch (Exception e){
            String msg = "££ Exception in main CreateHandicapIndex = " + e.getMessage();
            LOG.error(msg);
 }finally{
            DBConnection.closeQuietly(conn, null, null, null);
 }
} // end main//
} //end Class
*/