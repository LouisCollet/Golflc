package read;

import entite.Club;
import entite.UnavailableStructure;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ReadUnavailableStructure implements interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 

public UnavailableStructure read(final Club club, final Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        LOG.debug("entering " + methodName + " ...");
        LOG.debug(" for club = " + club);
        // ultérieurement, ajouter une date
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
   //    String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
  final String query = """
      SELECT *
      FROM club
      WHERE idclub = ?
  """;

    ps = conn.prepareStatement(query);
    ps.setInt(1, club.getIdclub() );
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    UnavailableStructure structure = new UnavailableStructure();
	while(rs.next()){
             structure = UnavailableStructure.map(rs); // mod 30-03-2020
	}
     if(structure == null){
         String msg = "££ No Structure found for club = " + club.getIdclub();
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + 1 + " lines.");
         LOG.debug("found json structure = " + structure.getStructureList().toString() );
         LOG.debug("found items = " + structure.getStructureList().size());
     }   
        return structure;
}catch (SQLException e){
    String msg = "SQL Exception for " + methodName + " " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
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
    club.setIdclub(101);  // la cala 1075
  //  UnavailableStructure us = new ReadUnavailableStructure().read(club, conn);
     LOG.debug("UnavailableStructure = "  + new ReadUnavailableStructure().read(club, conn));
  DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class