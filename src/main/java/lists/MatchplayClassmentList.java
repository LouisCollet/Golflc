package lists;

import entite.Club;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class MatchplayClassmentList implements interfaces.Log{
    private static List<Round> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public List<Round> list(Round round, Club club,final Connection conn) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
       LOG.debug("entering " + methodName);
       LOG.debug("with round " + round);
       LOG.debug("with club " + club);
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
    final String query = """
            SELECT *
            FROM round
            WHERE round.RoundCompetition = ?
            ORDER BY idround DESC;
            """;

     ps = conn.prepareStatement(query);
     ps.setString(1, round.getRoundCompetition());
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
     liste = new ArrayList<>();
     Club c = null;
     // à modifier
//     club.setIdclub(1159);
  //   Round round = new Round();
     while(rs.next()){
       //   round = new entite.Round().map(rs, c);
          round = new entite.Round().dtoMapper(rs);
   //       ecl.setRound(new entite.Round().map(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
         liste.add(round);
     } // end while
     if(liste.isEmpty()){
         String error = "££ Empty Result Table in " + methodName;
         LOG.error(error);
         LCUtil.showMessageFatal(error);
  //       return liste;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //  liste.forEach(item -> LOG.debug("Players list with Players and passwords " + item));  // java 8 lambda
return liste;
} catch(SQLException sqle){
    String msg = "£££ SQL exception in " + methodName + "/" + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception e){
    String msg = "£££ Exception in " + methodName + " / " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
  //   LOG.debug("escaped to " + methodName + " repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
   //then you should introduce lazy loading inside the getter method. I.e. if the property is null,
    //then load and assign it to the property, else return it.
} //end if
} //end method
    

    public static List<Round> getListe() {
        return liste;
    }

    public static void setListe(List<Round> liste) {
        MatchplayClassmentList.liste = liste;
    }
    
  void main() throws SQLException, Exception {
    Connection conn = new utils.DBConnection().getConnection();
    Club club = new Club();
    club.setIdclub(1159);
    club = new read.ReadClub().read(club, conn);
    Round round = new Round();
    round.setIdround(608);
    round = new read.ReadRound().read(round, conn);
    List<Round> tees = new MatchplayClassmentList().list(round, club, conn);
        LOG.debug("round list  for a classment= " + tees.size());
    tees.forEach(item -> LOG.debug("Round list for a Competition " + item));
    utils.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class