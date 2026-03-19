package read;

import entite.HolesGlobal;
import entite.Tee;
import find.FindDistances;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service de lecture de HolesGlobal (tous les holes d'un tee)
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO - Connection pooling
 *
 * IMPORTANT : Lit les holes du MASTER TEE (TeeMasterTee)
 * Les distances viennent de FindDistances (table distances)
 */
@ApplicationScoped
public class ReadHole implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * FindDistances injecté par CDI
     */
    @Inject
    private FindDistances findDistances;

    /**
     * Lit tous les holes d'un tee (structure HolesGlobal)
     *
     * @param tee Le tee dont on veut lire les holes
     * @return HolesGlobal avec le tableau int[][] rempli
     * @throws SQLException en cas d'erreur SQL
     */
    public HolesGlobal read(Tee tee) throws SQLException {

        HolesGlobal holesGlobal = new HolesGlobal();

        try (Connection conn = dao.getConnection()) {

            LOG.debug("entering ReadHoles ...");
            LOG.debug(" with tee = " + tee);

            String query = """
                SELECT *
                FROM hole, tee
                WHERE tee.idtee = ?
                    AND hole.tee_idtee = tee.TeeMasterTee
                ORDER by holenumber
                """;
            //AND hole.tee_idtee = tee.idtee // mod 09-08-2023 pour 01-09 et 10-18

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, tee.getIdtee());
                LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {

                    // ✅ PARTIE NON MODIFIÉE (comme demandé) - DÉBUT
                    int i = 0;
                    var v = findDistances.find(tee).getDistanceArray();
                    LOG.debug("line 00");
                    if(v == null){
                        LOG.debug("array distance = null , filled with 0");
                        Arrays.fill(v, 0);
                        LOG.debug("array filled with 0 = " + v);
                    }
                    LOG.debug("array distance = " + Arrays.toString(v));
                    while(rs.next()){
                        holesGlobal.getDataHoles()[i][0] = (rs.getInt("HoleNumber") );
                        holesGlobal.getDataHoles()[i][1] = (rs.getInt("HolePar") );
                        holesGlobal.getDataHoles()[i][2] = (rs.getInt("HoleStrokeIndex"));
                        holesGlobal.getDataHoles()[i][3] = v[i];
                        i++;
                    } // end while
                    LOG.debug("there are rows = " + i);
                    // ✅ PARTIE NON MODIFIÉE - FIN

                    return holesGlobal;
                }
            }

        } catch (SQLException e) {
            String msg = "SQLException in ReadHoles() = " + e.toString() + ", SQLState = " + e.getSQLState()
                    + ", ErrorCode = " + e.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e;

        } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            LCUtil.showMessageFatal("Exception in ReadHoles = " + ex.toString());
            throw new SQLException(ex);
        }
    }

    /**
     * Main pour tests
     */
    public static void main(String[] args) {
        try {
            Tee tee = new Tee();
            tee.setIdtee(203);

            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test tee ID: {}", tee.getIdtee());

        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
        }
    }
}
/*
import entite.HolesGlobal;
import entite.Tee;
import find.FindDistances;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import connection_package.DBConnection;
import utils.LCUtil;
import static interfaces.Log.LOG;
public class ReadHoles {

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
*/