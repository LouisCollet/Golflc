package read;

import entite.HolesGlobal;
import entite.Tee;
import find.FindDistances;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

public class ReadHoles implements interfaces.Log{

public HolesGlobal read(Tee tee, Connection conn) throws SQLException{ 
        ResultSet rs = null;
        PreparedStatement ps = null;
        HolesGlobal holesGlobal = new HolesGlobal();
try{ 
    LOG.debug("entering ReadHoles ...");
    LOG.debug(" with tee = " + tee) ;
  String query =  """
        SELECT *
        FROM hole, tee
        WHERE tee.idtee = ?
            AND hole.tee_idtee = tee.TeeMasterTee
        ORDER by holenumber
    """;
  //AND hole.tee_idtee = tee.idtee // mod 09-08-2023 pour 01-09 et 10-18
     ps = conn.prepareStatement(query);
     ps.setInt(1, tee.getIdtee());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
 //   rs.beforeFirst(); //  Initially the cursor is positionned before the first row
    //  int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
      int i = 0;
      var v = new FindDistances().find(tee, conn).getDistanceArray();
      LOG.debug("line 00");
      if(v == null){
         LOG.debug("array distance = null , filled with 0");
        //  for(int[] subarray : v){
            Arrays.fill(v, 0);
            LOG.debug("array filled with 0 = " + v);
       // }
      }
         LOG.debug("array distance = " + Arrays.toString(v));
      while(rs.next()){
 /*         i ++;
  //        LOG.debug("i = " + i);
          rowNum = rs.getRow() - 1;
          holesGlobal.getDataHoles()[rowNum][0] = (rs.getInt("HoleNumber") );
          holesGlobal.getDataHoles()[rowNum][1] = (rs.getInt("HolePar") );
          holesGlobal.getDataHoles()[rowNum][2] = (rs.getInt("HoleStrokeIndex") );
             //   holesGlobal.getDataHoles()[rowNum][3] = (rs.getInt("HoleDistance") );
          holesGlobal.getDataHoles()[rowNum][3] = v[i-1]; //15-08-2023 les distances sont stockées dans la table 'distances'
   */       
          holesGlobal.getDataHoles()[i][0] = (rs.getInt("HoleNumber") );
          holesGlobal.getDataHoles()[i][1] = (rs.getInt("HolePar") );
          holesGlobal.getDataHoles()[i][2] = (rs.getInt("HoleStrokeIndex"));
          
          holesGlobal.getDataHoles()[i][3] = v[i];
          i++;
        } // end while
      LOG.debug("there are rows = " + i);
  //  LOG.debug(" -- holesGlobal.dataHoles = " + Arrays.deepToString(holesGlobal.getDataHoles()));
  return holesGlobal;
}catch (SQLException e){
    String msg = "SQLException in ReadHoles() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception in ReadHoles = " + ex.toString() );
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); //mod 14/08/2014
}
} //end method

void main() throws SQLException, Exception{
    Connection conn = new DBConnection().getConnection();
    ReadHoles lha = new ReadHoles();
    Tee tee = new Tee();
    tee.setIdtee(203);   // 203
    HolesGlobal h = lha.read(tee, conn);

   // LOG.debug(" -- HOLES [][] = " + Arrays.deepToString(h.getDataHoles()) );
    LOG.debug(" -- HOLES [][] = " + h.toString());   // mod 14/08/2017
    LOG.debug(" -- HOLES [0][0] = " + h.getDataHoles()[0][0]);
    LOG.debug(" -- HOLES [0][1] = " + h.getDataHoles()[0][1]);
    LOG.debug(" -- HOLES [0][2] = " + h.getDataHoles()[0][2] );
    LOG.debug(" -- HOLES [0][3] = " + h.getDataHoles()[0][3] );

    LOG.debug(" -- HOLES [1][0] = " + h.getDataHoles()[1][0] );
    LOG.debug(" -- HOLES [1][1] = " + h.getDataHoles()[1][1] );
    LOG.debug(" -- HOLES [1][2] = " + h.getDataHoles()[1][2] );
    LOG.debug(" -- HOLES [1][3] = " + h.getDataHoles()[1][3] );
    
    int i = 1;
    LOG.debug(" - HOLES [0][0] = " + h.getDataHoles()[i-1][0] );
    LOG.debug(" -- HOLES [1][0] = " + h.getDataHoles()[1][0] );
    LOG.debug(" -- HOLES [2][0] = " + h.getDataHoles()[2][0] );
    LOG.debug(" -- HOLES [3][0] = " + h.getDataHoles()[3][0] );
    LOG.debug(" -- HOLES [4][0] = " + h.getDataHoles()[4][0] );
    LOG.debug(" -- HOLES [5][0] = " + h.getDataHoles()[5][0] );
  //      i = 1;
    LOG.debug(" -HOLES [0][0] = " + h.getDataHoles()[0][1] );
    LOG.debug(" -- HOLES [1][1] = " + h.getDataHoles()[1][1] );
    LOG.debug(" -- HOLES [2][1] = " + h.getDataHoles()[2][1] );
    LOG.debug(" -- HOLES [3][1] = " + h.getDataHoles()[3][1] );
    LOG.debug(" -- HOLES [4][1] = " + h.getDataHoles()[4][1] );
    LOG.debug(" -- HOLES [5][1] = " + h.getDataHoles()[5][1] );
    LOG.debug(" -- HOLES [6][1] = " + h.getDataHoles()[6][1] );

DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class