
package delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import utils.DBConnection;
import utils.LCUtil;

/* à modifier !!!!!!!!!!!!!!!
        just for correctiong errors !!!
basic info

select * from handicap
	where handicap.player_idplayer = 324713
	and handicap.idhandicap = "2017-08-22";
-- delete row

select * from handicap
	where handicap.player_idplayer = 324713
	and handicap.idhandicap < "2017-08-22"
	ORDER BY idhandicap desc LIMIT 1
-- replace handicapend with : "2099-12-31"

ajouter une colonne à show_handicap.xhtml
        */
        
public class DeleteHandicap implements interfaces.Log, interfaces.GolfInterface
{
    public String deleteHandicap(final int idplayer, final Date date, final Connection conn) throws Exception
    {
    PreparedStatement ps = null;
    int row_update = 0;
try
{   //encore à  faire : delete du record activation s'il existe ...
     LOG.info("starting Delete Handicap ... = " );
     LOG.info("Delete Handicap for idplayer = "  + idplayer);
     LOG.info("Delete Handicap for date     = "  + SDF.format(date));
     
    String query = 
       " DELETE from handicap" +
       " WHERE handicap.player_idplayer = ?" +
       "    and handicap.idhandicap = ?"
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, idplayer);
    ps.setDate(2, LCUtil.getSqlDate(date));
    LCUtil.logps(ps); 
    int row_delete = ps.executeUpdate();
        LOG.info("deleted Handicap = " + row_delete);
        
if(row_delete > 0)
 //  ne continuer que si row_delete > 0   
    {query = "UPDATE handicap" +
        " SET handicap.HandicapEnd = '2099-12-31' " +
        " WHERE handicap.player_idplayer=?" +
        "	and handicap.idhandicap<?" +
        " ORDER BY idhandicap DESC LIMIT 1"
            ;
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    ps.setDate(2, LCUtil.getSqlDate((date)));
    LCUtil.logps(ps); 
    row_update = ps.executeUpdate();
        LOG.info("Updated HandicapEnd = " + row_update);
} // endif  
 /*   
    query = " delete from handicap where handicap.player_idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_hcp = ps.executeUpdate();
        LOG.info("deleting handicap = " + row_hcp);
    
    query = " delete from player where player.idplayer = ?";
    ps = conn.prepareStatement(query); 
    ps.setInt(1, idplayer);
    LCUtil.logps(ps); 
    int row_player = ps.executeUpdate();
        LOG.info("deleting player = " + row_player);
    */
    String msg = "<br/> <h1>Handicap deleted = " 
                        + " <br/></h1>player = " + idplayer
                        + " <br/>date = " + SDF.format(date)
                        + " <br/>deleted  = " + row_delete
                        + " <br/>updated = " + row_update;
  //                      + " <br/>handicap = " + row_hcp
  //                      + " <br/>player = " + row_player;
        LOG.info(msg);
       LCUtil.showMessageInfo(msg);
        return "Player deleted ! ";

}catch (SQLException e){
    String msg = "SQL Exception in DeleteHandicap = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
  LCUtil.showMessageFatal(msg);
    return null;
}catch (Exception ex){
    String msg = "Exception in DeleteHandicap() " + ex;
    LOG.error(msg);
   LCUtil.showMessageFatal(msg);
    return null;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
   
 public static void main(String[] args) throws SQLException, Exception 
 {
     DBConnection dbc = new DBConnection();
     Connection conn = dbc.getConnection();
 try{
       LOG.info("Input main = ");
    
    int idplayer = 2014102;
    Date date =SDF.parse("01/01/2000");
    DeleteHandicap dh = new DeleteHandicap();
    dh.deleteHandicap(idplayer,date, conn);
  //  DBConnection.closeQuietly(conn, null, null, null);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
   } // end method main
} //end class
