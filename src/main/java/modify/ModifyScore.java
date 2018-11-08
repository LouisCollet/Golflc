
package modify;

//import static interfaces.GolfInterface.NEWLINE;
//import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import utils.LCUtil;

public class ModifyScore implements interfaces.Log, interfaces.GolfInterface{
 
   final private static String []array_return_error = new String [3]; 
   
public String[] updateScore(Connection conn, int[][] points, int in_player, int in_round) throws SQLException
{
    LOG.info(" -- \nStart of setScore with array = " +  Arrays.deepToString(points) );
    LOG.info(" -- \nStart of setScore for player = " + in_player +" , round = " + in_round);
    PreparedStatement ps = null;
try
{
    final String query =
         "UPDATE score"
       + " SET ScorePar=?, ScoreStrokeIndex=?, ScoreStroke=?, ScoreExtraStroke=?,"
            + " scorePoints=?, ScoreModificationDate=?"
       + " WHERE ScoreHole = ?"
       + "  AND player_has_round_player_idplayer=?"
       + "  AND player_has_round_round_idround=?"
       ;

    for (int i=0; i<points.length; i++)
    {
        //    LOG.info("Starting loop with i = " + i);
        ps = conn.prepareStatement(query);
   // updated fields
        ps.setInt(1,points [i][1]); // Par
        ps.setInt(2,points [i][2]); // stroke index
        ps.setInt(3,points [i][3]); // strokes
        ps.setInt(4,points [i][4]); // extra strokes
        ps.setInt(5,points [i][5]); // points
        ps.setTimestamp(6,LCUtil.getCurrentTimeStamp());
   // where fields
        ps.setInt(7,points[i][0]); // hole number  was i+1);  error si round start hole 10, mod 14/11/2013]
        ps.setInt(8,in_player);
        ps.setInt(9,in_round);
             //    String p = ps.toString();
             utils.LCUtil.logps(ps);
        int row = ps.executeUpdate(); // write into database
        if(row!=0)
            {
                array_return_error[0]= "NO ERROR";
                array_return_error[1]= array_return_error[2]= "";
                    LOG.info("-- Successfull update Score for hole : " 
                            + points[i][0]);  //; + " / " + Arrays.deepToString(points) ); // was [4]
            }else{
                array_return_error[0]= "ERROR";
                array_return_error[1]= " -- SQL Exception in update score for hole = "
                        + points[i][0] + " / points : " + Arrays.toString(points [i+1]);
                    LOG.error(" -- SQL Exception in update score for hole = "
                        + points[i][0] + " / " + Arrays.toString(points [i+1]));
                    LOG.error("-- NOT NOT successful update Score, return code = "  + row + " index = " + i);
            }
     } // end for
 //array_return_error[0]= "NO ERROR";
 //array_return_error[1]= array_return_error[2]= "";
 return array_return_error;
} catch(SQLException sqle) {
       LOG.error(" -- SQL Exception by LC = " + sqle.getMessage());
       LOG.error(" -- ErrorCode = " +  sqle.getErrorCode() );
       LOG.error(" -- SQLSTATE =  " + sqle.getSQLState());
       array_return_error[0]= "ERROR";
       array_return_error[1]= sqle.getMessage();
       array_return_error[2]= "ErrorCode = " + sqle.getErrorCode() + " / SQLState = " + sqle.getSQLState();
       throw(sqle);
    } catch(Exception e) {
        LOG.error(" -- SQL Exception by LC = " + e.getMessage());
        return array_return_error;
    }finally{
        ps.close();
        LOG.info("end of setScore with : ");
        LOG.info(NEWLINE + Arrays.deepToString(points) );
   //     return array_return_error;
    }
} //end setScore

} // end class