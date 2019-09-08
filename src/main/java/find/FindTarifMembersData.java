package find;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import entite.TarifMember;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindTarifMembersData implements interfaces.Log, interfaces.GolfInterface{
    
final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 


public TarifMember findTarif(final Club club, final Connection conn) throws SQLException{
        final String METHODNAME = Thread.currentThread().getStackTrace()[1].getMethodName();
        LOG.info("entering " + CLASSNAME+"."+METHODNAME + " ...");
        LOG.info("starting " + METHODNAME + " for club = " + club.toString());
        // ultérieurement, ajouter une date
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
  String query = 
    "SELECT TarifMembersJson"
          + " from tarif_members"
          + " where tarif_members.TarifMembersidClub = ?";

    ps = conn.prepareStatement(query);
    ps.setInt(1, club.getIdclub() );
        utils.LCUtil.logps(ps);
        rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet " + CLASSNAME +  " has " + rs.getRow() + " lines.");
        if(rs.getRow() == 0){ 
                String err =  LCUtil.prepareMessageBean("tarif.notfound");
                err = err + " findTarif " + club.getClubName() + " / " + club.getIdclub();
                LOG.error(err);
                LCUtil.showMessageFatal(err);
                return null;
            }
 //       if(rs.getRow() > 1)
 //           {   throw new Exception(" -- More than 1 tarif = " + rs.getRow() );  }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
        String s = null;
	while(rs.next())
        {
             s = rs.getString("TarifMembersJson");
	}

   ObjectMapper om = new ObjectMapper();
   om.registerModule(new JavaTimeModule()); // important !! handle LocalDateTime  cherché lontemps ...
 //       LOG.info("line 03");
  //https://stackoverflow.com/questions/48868034/cannot-construct-instance-of-java-time-localdate-spring-boot-elasticseach
        TarifMember tm = om.readValue(s,TarifMember.class);
            LOG.info("TarifMember extracted from database = "  + tm.toString());
            LOG.info("nombre d'items MembersBase = " + tm.getMembersBase().length);
        return tm;
}catch (SQLException e){
    String msg = "SQL Exception for " + METHODNAME + " " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + METHODNAME + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

public static void main(String[] args) throws Exception , Exception{

    Connection conn = new DBConnection().getConnection();
    Club club = new Club();
    club.setIdclub(101);  // la tournette

    TarifMember tm = new FindTarifMembersData().findTarif(club, conn);
     LOG.info("Tarif extracted from database = "  + tm.toString());
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class

