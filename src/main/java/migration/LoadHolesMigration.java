package migration;

import entite.HolesGlobal;
import entite.Tee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadHolesMigration implements interfaces.Log{

public int[]load(Tee tee, Connection conn) throws SQLException{ 
        ResultSet rs = null;
        PreparedStatement ps = null;
        HolesGlobal holesGlobal = new HolesGlobal();
try{ 
  //  LOG.debug("entering LoadHoles Migration...");
    LOG.debug("starting load with tee = " + tee) ;
  String query =  """
        SELECT *
        FROM hole, tee
        WHERE tee.idtee = ?
            AND hole.tee_idtee = tee.TeeMasterTee
        ORDER by holenumber
    """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, tee.getIdtee());
 //    utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     int i = 0;
     while(rs.next()){
          holesGlobal.getDataHoles()[i][0] = (rs.getInt("HoleNumber") );
          holesGlobal.getDataHoles()[i][1] = (rs.getInt("HolePar") );
          holesGlobal.getDataHoles()[i][2] = (rs.getInt("HoleStrokeIndex"));
          holesGlobal.getDataHoles()[i][3] = (rs.getInt("HoleDistance") );
          i++;
        } // end while
      LOG.debug("there are rows = " + i);
  //  LOG.debug(" -- holesGlobal.dataHoles = " + Arrays.deepToString(holesGlobal.getDataHoles()));
        var v = utils.LCUtil.extractFrom2D(holesGlobal.getDataHoles(),3); //// 18 trous, 4 données : number, par, strokeindex, distance
      //    LOG.debug("array extracted = " + Arrays.toString(v) + NEW_LINE);
  return v;
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

void main() throws SQLException, Exception{
    Connection conn = new DBConnection().getConnection();
    //LoadHolesMigration lha = new LoadHolesMigration();
  //  Tee tee = new Tee();
  //  tee.setIdtee(203);   // 203

    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class