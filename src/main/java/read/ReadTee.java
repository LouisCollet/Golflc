package read;

import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ReadTee{

public Tee read(final Tee tee,Connection conn) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering ReadTee.read");
        LOG.debug(" for tee = " + tee);
     final String query = """
        SELECT *
        FROM Tee
        WHERE idtee = ?
      """;
   //     LOG.debug("Tee loaded = " + tee.getIdtee()); 
     ps = conn.prepareStatement(query);
     ps.setInt(1, tee.getIdtee()); // where
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     int i = 0;
     int t = 0;
     Tee teef = new Tee();
     while(rs.next()){
           i++;
           teef = entite.Tee.dtoMapper(rs);
      }  //end while
     
     if(i == 0){
            teef.setNotFound(true);
            String msg = LCUtil.prepareMessageBean("distancetee.notfound") + " for tee = " + tee + " / " + tee.getTeeStart();
            LOG.debug(msg);
            LCUtil.showMessageFatal(msg);
     }
     if(i == 1){
          //  String msg = LCUtil.prepareMessageBean("distancetee.found") + " = " + distanceTee + " for course = " + course.getIdcourse() + " / " + tee.getTeeStart();
                 //   player.getPlayerLastName() + " / " + player.getIdplayer()
                 //   + " for round : " + round.getRoundName();
            String msg = "distancetee.found" + " = " + teef.getTeeDistanceTee() + " tee = " + teef.getTeeStart();
            LOG.info(msg);
        //    LCUtil.showMessageInfo(msg);
        }
    return teef;
}catch (SQLException e){
    String msg = "SQLException in ReadTee() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in ReadTee = " + ex.toString() );
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

void main() throws SQLException, Exception{ // testing purposes
   Connection conn = new DBConnection().getConnection(); // main
   Tee tee = new Tee();
   tee.setIdtee(3000); // existe pas !!
   Tee t = new ReadTee().read(tee,conn);
   if(t.isNotFound()){
       LOG.debug(" Tee not found ! = " + t.toString());
   }else{
       LOG.debug(" loaded tee = " + t.toString());
   }
     
   DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class