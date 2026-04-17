package find;

import entite.HandicapIndex;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import rowmappers.HandicapIndexRowMapper;
import rowmappers.RowMapper;

@ApplicationScoped
public class FindHandicapIndexAtDate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject
    private create.CreateHandicapIndex createHandicapIndexService;

    /**
     * Trouve le handicap index à une date donnée (le plus récent avant cette date)
     * Si aucun HI trouvé, en crée un nouveau à compléter manuellement
     * @param handicapIndex avec HandicapPlayerId et HandicapDate définis
     * @return HandicapIndex trouvé ou null si créé
     */
    public HandicapIndex find(HandicapIndex handicapIndex) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering : " + methodName);

        final String query = """
            SELECT *
            FROM handicap_index
            WHERE HandicapPlayerId = ?
            AND HandicapDate < ?
            ORDER BY HandicapDate DESC
            LIMIT 1
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, handicapIndex.getHandicapPlayerId());
            ps.setTimestamp(2, Timestamp.valueOf(handicapIndex.getHandicapDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                RowMapper<HandicapIndex> handicapIndexMapper = new HandicapIndexRowMapper();
                int i = 0;

                if (rs.next()) {
                    i++;
                    handicapIndex = handicapIndexMapper.map(rs);
                }

                if (i == 0) {
                    // ✅ Aucun HI trouvé → création d'un nouveau
                    String msg = "No HandicapIndex found in " + methodName;
                    LOG.error(msg);

                    // ✅ Service injecté sans conn
                    var hi = createHandicapIndexService.create(handicapIndex);

                    msg = "Compléter manuellement la situation de départ // created = " + hi;
                    LOG.info(msg);

                    // ⚠️ Retourne null pour indiquer qu'il faut compléter manuellement
                    return null;

                } else {
                    LOG.debug("Handicap Index found = " + handicapIndex.getHandicapWHS());
                }

                return handicapIndex;
            }

        } catch (SQLException e) {
            String msg = "SQLException in " + methodName + ": " + e.getMessage()
                    + ", SQLState = " + e.getSQLState()
                    + ", ErrorCode = " + e.getErrorCode();
            LOG.error(msg, e);
            throw e;

        } catch (Exception e) {
            String msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg, e);
            throw new SQLException(msg, e);
        }
    } // end method

    /*
    void main() throws SQLException {
        HandicapIndex handicapIndex = new HandicapIndex();
        handicapIndex.setHandicapPlayerId(324713);
        Round round = new Round();
        round.setIdround(590);

        handicapIndex.setHandicapDate(round.getRoundDate());
        HandicapIndex hi = new find.FindHandicapIndexAtDate().find(handicapIndex);

        if (hi != null) {
            LOG.debug("FindHandicapIndexAtDate = " + hi.getHandicapWHS());
        } else {
            LOG.debug("HandicapIndex created - needs manual completion");
        }
    } // end main
    */

} // end Class
/*
import entite.HandicapIndex;
import entite.Round;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import connection_package.DBConnection;
import rowmappers.HandicapIndexRowMapper;
import rowmappers.RowMapper;
import utils.LCUtil;

public class FindHandicapIndexAtDate {


public HandicapIndex find(HandicapIndex handicapIndex, final Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering : " + methodName);
  //      LOG.debug("starting " + methodName + " for HandicapIndex = " + handicapIndex); //.getHandicapPlayerId());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
   final String query = """
         SELECT *
         FROM handicap_index
         WHERE HandicapPlayerId = ?
         AND HandicapDate < ?
         ORDER BY HandicapDate DESC
         LIMIT 1
   """;

//  hcp à une date déterminée
    ps = conn.prepareStatement(query);
    ps.setInt(1, handicapIndex.getHandicapPlayerId());
    ps.setTimestamp(2,Timestamp.valueOf(handicapIndex.getHandicapDate()));
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
//    HandicapIndex handicapIndex = new HandicapIndex();
    RowMapper<HandicapIndex> handicapIndexMapper = new HandicapIndexRowMapper();
    int i = 0;
    if(rs.next()){
        i++;
      //  handicapIndex = entite.HandicapIndex.map(rs);
         handicapIndex = handicapIndexMapper.map(rs);
     }
    if(i == 0){
         String msg = "££ No HandicapIndex found !! " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         var hi = new create.CreateHandicapIndex().create(handicapIndex, conn);
         msg = "compléter manuellement la situation de départ // created = " + hi;
         LOG.info(msg);
         LCUtil.showMessageInfo(msg);
         return null;
     }else{
         LOG.debug(" Handicap Index found = " + handicapIndex.getHandicapWHS()); //  + " at the date " + handicapIndex.getHandicapDate());
 //        LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //       LOG.debug("i = " + i + " Handicap Index = " + handicapIndex + " at the date " + handicapIndex.getHandicapDate());
     return handicapIndex;
}catch (SQLException e){
    String msg = "SQL Exception in = " + methodName + " /" + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
// return false;
}//end method

void main() throws SQLException, Exception{ // testing purposes
  final Connection conn = new DBConnection().getConnection();
  HandicapIndex handicapIndex = new HandicapIndex();
  handicapIndex.setHandicapPlayerId(324713);
  Round round = new Round();
  round.setIdround(590);
  round = new read.ReadRound().read(round, conn);
  handicapIndex.setHandicapDate(round.getRoundDate());
  HandicapIndex hi = new find.FindHandicapIndexAtDate().find(handicapIndex, conn);
        LOG.debug("FindHandicapIndexAtDate  = " + hi.getHandicapWHS());
  DBConnection.closeQuietly(conn, null, null, null);
}// end main

} // end Class
*/