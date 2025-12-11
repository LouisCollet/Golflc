package find;

import entite.Club;
import entite.Round;
import entite.TarifMember;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import jakarta.inject.Inject;
import utils.DBConnection;
import utils.LCUtil;

public class FindTarifMembersData implements interfaces.Log, interfaces.GolfInterface{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 @Inject private TarifMember tarif;
 
//public TarifMember find(final Club club, LocalDateTime ldt, final Connection conn) throws SQLException{
    public TarifMember find(final Club club,
            Round round,
            final Connection conn) throws SQLException{
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug("...entering " + methodName);
        LOG.debug(" ... for club = " + club);
        LOG.debug(" ... for round = " + round);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
      final String query = """
            SELECT *
            FROM tarif_members
            WHERE tarif_members.TarifMemberIdClub = ?
            AND ? BETWEEN TarifMemberStartDate AND TarifMemberEndDate
         """;

    ps = conn.prepareStatement(query);
    ps.setInt(1, club.getIdclub());
    ps.setTimestamp(2,Timestamp.valueOf(round.getRoundDate()));
        utils.LCUtil.logps(ps);
        rs =  ps.executeQuery();
        int i = 0;
	while(rs.next()){
            tarif = entite.TarifMember.map(rs);
            i++;
	}
     //   if(s == null){
        if(i == 0){
            String err =  LCUtil.prepareMessageBean("tarif.notfound") + " findTarif " 
                    + club.getClubName() + " / " + club.getIdclub();
               LOG.error(err);
            LCUtil.showMessageFatal(err);
            return null;
     }else{
         LOG.debug("ResultSet " + methodName + " rs.getRow() = " + rs.getRow() + " has " + 1 + " lines.");
     }
   return tarif;
}catch (SQLException e){
    String msg = "SQL Exception for " + methodName + " " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

void main() throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
    Club club = new Club();
    club.setIdclub(101);
    Round round = new Round();
    round.setIdround(699);
    round = new read.ReadRound().read(round, conn);
  //  round.setRoundDate(LocalDateTime.of(2021,Month.MAY,31,0,0));
    TarifMember tarifMember = new FindTarifMembersData().find(club,round, conn);
     LOG.debug("Tarif extracted from database = "  + tarifMember);

DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class