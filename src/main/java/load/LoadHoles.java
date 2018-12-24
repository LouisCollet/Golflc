package load;

import entite.HolesGlobal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadHoles implements interfaces.Log
{

public HolesGlobal LoadHolesArray(Connection conn, int idtee) throws SQLException // throws SQLException
{ 
        ResultSet rs = null;
        PreparedStatement ps = null;
        HolesGlobal hg = new HolesGlobal();
try
{    LOG.info("entering LoadHoles ...");
    LOG.info("starting LoadHolesArray with idtee = = " + idtee) ;
  String ho = utils.DBMeta.listMetaColumnsLoad(conn, "hole");
  String query =  
        "SELECT "
          + ho +
   //       + "holenumber, holepar, holestrokeindex, holedistance" +
        " FROM hole, tee" +
        " WHERE tee.idtee = ?" +
            " AND hole.tee_idtee = tee.idtee" +
        " ORDER by holenumber"
;
     ps = conn.prepareStatement(query);
     ps.setInt(1, idtee);
 //    ps.setInt(2, idround);
         utils.LCUtil.logps(ps);
		//get round data from database
    rs =  ps.executeQuery();
    rs.beforeFirst(); //  Initially the cursor is positionned before the first row
      int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
      while (rs.next())
        {rowNum = rs.getRow() - 1;
        hg.getDataHoles()[rowNum][0] = (rs.getInt("HoleNumber") );
        hg.getDataHoles()[rowNum][1] = (rs.getInt("HolePar") );
        hg.getDataHoles()[rowNum][2]= (rs.getInt("HoleStrokeIndex") );
        hg.getDataHoles()[rowNum][3]= (rs.getInt("HoleDistance") );
        
    //       HOLES[rowNum][0]= (rs.getInt("HoleNumber") );
    //       HOLES[rowNum][1]= (rs.getInt("HolePar") );
    //       HOLES[rowNum][2]= (rs.getInt("HoleStrokeIndex") );
    //       HOLES[rowNum][3]= (rs.getInt("HoleDistance") );
      
        } // end while
   //   LOG.info(" -- HOLES [][] = " + Arrays.deepToString(HOLES) );
 //  hg.setDataHoles(HOLES);
return hg;

}catch (SQLException e){
    String msg = "SQLException in LoadHoles() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in LoadHoles = " + ex.toString() );
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); //mod 14/08/2014
}
} //end method

public static void main(String[] args) throws SQLException, Exception // testing purposes
{
    DBConnection dbc = new DBConnection();
    Connection conn = dbc.getConnection();
    LoadHoles lha = new LoadHoles();
    HolesGlobal h = lha.LoadHolesArray(conn,125);

   // LOG.info(" -- HOLES [][] = " + Arrays.deepToString(h.getDataHoles()) );
    LOG.info(" -- HOLES [][] = " + h.toString());   // mod 14/08/2017
    LOG.info(" -- HOLES [0][0] = " + h.getDataHoles()[0][0]);
    LOG.info(" -- HOLES [0][1] = " + h.getDataHoles()[0][1]);
    LOG.info(" -- HOLES [0][2] = " + h.getDataHoles()[0][2] );
    LOG.info(" -- HOLES [0][3] = " + h.getDataHoles()[0][3] );

    LOG.info(" -- HOLES [1][0] = " + h.getDataHoles()[1][0] );
    LOG.info(" -- HOLES [1][1] = " + h.getDataHoles()[1][1] );
    LOG.info(" -- HOLES [1][2] = " + h.getDataHoles()[1][2] );
    LOG.info(" -- HOLES [1][3] = " + h.getDataHoles()[1][3] );
    
    int i = 1;
    LOG.info(" - HOLES [0][0] = " + h.getDataHoles()[i-1][0] );
    LOG.info(" -- HOLES [1][0] = " + h.getDataHoles()[1][0] );
    LOG.info(" -- HOLES [2][0] = " + h.getDataHoles()[2][0] );
    LOG.info(" -- HOLES [3][0] = " + h.getDataHoles()[3][0] );
    LOG.info(" -- HOLES [4][0] = " + h.getDataHoles()[4][0] );
    LOG.info(" -- HOLES [5][0] = " + h.getDataHoles()[5][0] );
        i = 1;
    LOG.info(" -HOLES [0][0] = " + h.getDataHoles()[0][1] );
    LOG.info(" -- HOLES [1][1] = " + h.getDataHoles()[1][1] );
    LOG.info(" -- HOLES [2][1] = " + h.getDataHoles()[2][1] );
    LOG.info(" -- HOLES [3][1] = " + h.getDataHoles()[3][1] );
    LOG.info(" -- HOLES [4][1] = " + h.getDataHoles()[4][1] );
    LOG.info(" -- HOLES [5][1] = " + h.getDataHoles()[5][1] );
    LOG.info(" -- HOLES [6][1] = " + h.getDataHoles()[6][1] );

DBConnection.closeQuietly(conn, null, null, null);
}// end main

} // end class