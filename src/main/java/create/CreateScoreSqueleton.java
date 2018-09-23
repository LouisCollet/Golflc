/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package create;

//import static interfaces.GolfInterface.NEWLINE;
//import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import utils.LCUtil;

/**
 *
 * @author collet
 */
public class CreateScoreSqueleton implements interfaces.GolfInterface, interfaces.Log
{
    final private static String []array_return_error = new String [3];
    
   public  String[] setScoreSqueleton (final Connection conn, final int[][] points, final int in_player, final int in_round, final int holes)
        throws SQLException
{
    LOG.info(" -- Start of setScoreSqueleton with array points = " +  Arrays.deepToString(points) );
////    LOG.info(" -- Start of setScoreSqueleton with starthole = " + starthole );
    PreparedStatement ps = null;
try

{
    final String query = LCUtil.generateInsertQuery(conn,"score");
        //String query = "INSERT INTO score VALUES (?,?,?,?,?,?,?,?,?)";
    ps = conn.prepareStatement(query);

        LOG.info(points.length + " HOLES, Stroke Index before transfert = " + NEWLINE + Arrays.deepToString(points) );
   // points = GolfCalc.trfPoints();   // est-ce utile ???
        LOG.info(points.length + " HOLES, Stroke Index transfered = " + NEWLINE + Arrays.deepToString(points) );
    for (int i=0; i<points.length; i++) // mod 12/01/2012
{
    //LOG.info("inside : i = " + i + " | start =  " + start + " | stop = " + stop);
    ps.setNull(1,java.sql.Types.INTEGER);// auto-increment
    ps.setInt(2,points[i][0]);  //Hole Number
    ps.setInt(3,points[i][3]); // scoreStroke, introduit à 0
    ps.setInt(4,points[i][4]);  // ScoreExtraStroke,
    ps.setInt(5,points[i][5]); // ScorePoints, introduit à zéro,
    ps.setInt(6,points[i][1]);  // Score Par
    ps.setInt(7,points[i][2]); // ScoreStrokeIndex
    ps.setInt(8,0); // ScoreFairway, introduit à zéro
    ps.setInt(9,0); // ScoreGreen, introduit à zéro
    ps.setInt(10,0); // ScorePutts, introduit à zéro
    ps.setInt(11,0); // ScoreBunker, introduit à zéro
    ps.setInt(12,0); // ScorePenalty, introduit à zéro

    ps.setInt(13,in_player);
    ps.setInt(14,in_round);
    ps.setTimestamp(15,LCUtil.getCurrentTimeStamp());
         //    String p = ps.toString();
         utils.LCUtil.logps(ps);
    int row = ps.executeUpdate(); // write into database
    if (row!=0)
        {
            int key = LCUtil.generatedKey(conn);
            String msg = "Successful insert for scoreId = " + key;
              LOG.info(msg);
    }else{
        LOG.info("-- score already exists !!! ");}
} // end for loop

 array_return_error[0]= "NO ERROR";
 array_return_error[1]= "";
 array_return_error[2]= "";
 return array_return_error;
} // endtry
catch(SQLException sqle)
    {
       LOG.error(" -- SQL Exception by LC = " + sqle.getMessage());
       LOG.error(" -- ErrorCode = " +  sqle.getErrorCode() );
       LOG.error(" -- SQLSTATE =  " + sqle.getSQLState());
       array_return_error[0]= "ERROR";
       array_return_error[1]= sqle.getMessage();
       array_return_error[2]= "ErrorCode = " + sqle.getErrorCode() + " / SQLState = " + sqle.getSQLState();
       return array_return_error;
    } // end catch
finally
{
  ps.close();
  LOG.info(" -- Finally : end of setScoreSqueleton");
  LOG.info(points.length + " HOLES, after squeleton = " + NEWLINE + Arrays.deepToString(points) );
  //return array_return_error;
}
} //end setTableExtraStrokes
 
}
