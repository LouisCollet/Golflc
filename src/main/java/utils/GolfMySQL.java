package utils;

import java.sql.*;
import java.util.*;

public class GolfMySQL implements interfaces.GolfInterface, interfaces.Log    // constantes
{
// variables instance

final private static String []array_return_error = new String [3];
private static int nb = 0;     // nb par constructor Ã  chaque appel Ã  la classe!!
// variables de classe
private static int round;
private static double player_hcp;
private static int csa;
//private static int starthole;
private static String qualifying;
private static int [][] points = null; //new int [18][6];

public GolfMySQL()    // constructor
{
    nb++;    // comment
    //LOG.info("class Name = " + className);
  //  LOG.info(" from constructor GolfMySQL = " + nb);
}

public String [] getQueryLoadArray(Connection conn) throws SQLException{   // charge Array points Ã  partir table myPoints
    Statement st = null;
    ResultSet rs = null;
try{
        LOG.debug(" -- Start 1 of getQueryLoadArray");

//load array points Ã  partir de table myPoints
      st = conn.createStatement();
      rs = st.executeQuery("select * from golflc.myPoints");
      rs.beforeFirst(); //  Initially the cursor is positionned before the first row
      int rowNum = 0; //The method getRow lets you check the number of the row
                        //where the cursor is currently positioned
      while (rs.next())
        {rowNum = rs.getRow() - 1;
            points [rowNum][0]= rs.getInt(1);   //  hole #
            points [rowNum][1]= rs.getInt(2);   //  hole par
            points [rowNum][2]= rs.getInt(3);   //  hole index
            points [rowNum][3]= rs.getInt(4);   //  hole strokes
            points [rowNum][4]= 0;              //  extra
            points [rowNum][5]= 0;              //  points
        } // end while
     // LOG.info(" -- array points [][]= " + Arrays.deepToString(points) );

      LOG.info("Row" + TAB + "Hole" + TAB + "Par" + TAB + "Index" + TAB +
              "Stroke" + TAB + "Extra" + TAB + "Points");
   //   LOG.info(TAB + TAB + "Hole " +
   //           ",Par " + ",Index " + ",Stroke " + ",Extra " + ",Points ");
      LOG.info(NEWLINE + Arrays.deepToString(points));
      //LOG.info(NEWLINE + " -- End of getQueryLoadArray for 09th element = " + points[9] [3]); // 3=strokes : pas de points introduits
      //LOG.info("  -- table myPoints, player = " + rs.getInt(5));

       for (int i=0; i<points.length; i++)
    {
            //int strokes = points [i][3];
            LOG.info(" -- ending : hole = " + points [i][0] + " , strokes = " + points [i][3]);
    } 
      
      
array_return_error[0]= "NO ERROR";
array_return_error[1]= array_return_error[2]= "";
return array_return_error;
} catch(final Exception e){
       LOG.error(" -- Exception by LC = " + e.getMessage());
       LOG.error(" -- ErrorCode = ");
       LOG.error(" -- SQLSTATE =  ");
       array_return_error[0]= "ERROR";
       array_return_error[1]= e.getMessage();
       array_return_error[2]= "ErrorCode = " ;
       return array_return_error;

} // end catch

finally
{ rs.close();
  st.close();
  //meta.close();
  //LOG.info(" -- end of method\n");
  //return array_return_error;
}
} // end method

/*
public static int getCountScore(Connection conn, int idplayer, int idround, String operation)
        throws SQLException
{
    PreparedStatement ps = null;
    ResultSet rs = null;
    String query = null;
try
{
   //     LOG.info("player  = " + idplayer);
   //     LOG.info("round  =  " + idround);
   //     LOG.info("Connection =  " + conn);
if (operation.equalsIgnoreCase("rows") )
    {query =
            "SELECT count(*)"
          + " FROM score"
          + " WHERE score.player_has_round_player_idplayer=?"
          + "   and player_has_round_round_idround=?"
          ;
    }else{
    query =
            "SELECT sum(scorestroke)"
          + " FROM score"
          + " WHERE score.player_has_round_player_idplayer=?"
          + " and player_has_round_round_idround=?"
          ;
    }

    ps = conn.prepareStatement(query);
    ps.setInt(1,idplayer);
    ps.setInt(2,idround);
          //    String p = ps.toString();
  //        utils.LCUtil.logps(ps);
    rs = ps.executeQuery(); // attention !! il ne faut rien mettre entre les ()
        //LOG.debug(" -- resultset = " + rs.toString());
    if (rs.next())
    {  // LOG.debug("resultat : getCountScore = " + rs.getInt(1) );
      return rs.getInt(1);
    }else{
      //  LOG.debug("no next : getCountScore = " + rs.getInt(1) );
        return 99;  //error code
    }
} //end try

catch(MySQLSyntaxErrorException sqle)
{
    String msg = "-- Â£Â£Â£ MySQLSyntaxErrorException in getCountScore = " + sqle.getMessage()
            + " ,SQLState = " + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
        LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 99;
}
catch(SQLException sqle)
{
    String msg = "Â£Â£Â£ SQLException in getCountScore = " + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
        LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 99;
}
catch(NumberFormatException nfe)
{
    String msg = "Â£Â£Â£ NumberFormatException in getCountScore = " + nfe.getMessage();
        LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return 99;
}
finally
{
      DBConnection.closeQuietly(null, null, rs, ps);
}

} //end method
*/
public String [] getIdRoundPro(Connection conn, String in_round_date, int in_player) throws SQLException
{
    LOG.info(" -- Start of getIdRoundPro with round date = " + in_round_date);
    CallableStatement cs = null;
try
{
    final String stored_name = "get_idround(?,?,?)";   // nom de la stored pro, 30 parameters
        LOG.info(" -- Start with : " + stored_name + " player = " + in_player + " round = " + in_round_date);
    cs = conn.prepareCall("{CALL " + stored_name + "}");
 //Bind IN parameter first : player and round date
     LOG.info(" -- Before setdate ");
      cs.setDate(1, java.sql.Date.valueOf(in_round_date));
          LOG.info("Round Date = " + in_round_date);
      cs.setInt(2,in_player);
          LOG.info("Player = " + in_player);
 // Register OUT parameters
      cs.registerOutParameter(3, Types.INTEGER); // # rows
      cs.execute();
        LOG.info("Out parameter 3 - idround  = " + cs.getInt("out_idround") ); // = name used in stored pro
    round = cs.getInt(3);
        LOG.info(" field round = " + round); // = name used in stored pro

        if(round == 0)
        {
            array_return_error[0]= "ERROR";
            array_return_error[1]= "getIdRoundPro";
            array_return_error[2]= "Wrong Date Round = " + in_round_date;
            //throw new Exception(" -- Exception = wrong Round Date : " + cs.getInt("out_idround"));
            return array_return_error;
        }
array_return_error[0]= "NO ERROR";
array_return_error[1]= array_return_error[2]= "";
return array_return_error;
}catch(final SQLException sqle){
       LOG.error(" -- SQL Exception by " + sqle.getMessage());
       LOG.error(" -- ErrorCode = " + sqle.getErrorCode() );
       LOG.error(" -- SQLSTATE =  " + sqle.getSQLState());
       array_return_error[0]= "ERROR";
       array_return_error[1]= sqle.getMessage();
       array_return_error[2]= "ErrorCode = " + sqle.getErrorCode() + " SQLState =  " + sqle.getSQLState();
       return array_return_error;
} // end catch
finally
{
  cs.close();
  LOG.info(" -- end of method\n");
  //return array_return_error;
}
} //end getIdRound
// -----------------------------------------------------------------------------

public int getIdRound() throws SQLException
{
    return round;
}
public int getCSA() throws SQLException
{
    return csa;
}

    public void setPoints(int[][] points)
    {
        GolfMySQL.points = points;
        LOG.info("GolfMySQL : points transferred !");
    }


public int[][] createArrayPoints(int holes) throws SQLException, Exception
{   // nod 21/06/2015 argument holes
    LOG.info(" ... entering creatgeArrayPoints with holes = " + holes);
    switch (holes) {
        case 9:
                points = new int [9][6];
            LOG.info("array created for holes  = " + holes);
            break;
        case 18:
                points = new int [18][6];
            LOG.info("array created for holes  = " + holes);
            break;
        default:
            LOG.info("[][]holes = not 9 or 18 " + holes);
            throw new Exception("error holes, not 9/18");
    }
    points = LCUtil.initArrayPoints(points);
return points;
}

public static String getQualifying() throws SQLException
{
    return qualifying;
}

public static void main(String[] args) throws SQLException // testing purposes
{
}// end main
} // end Class GolfMySQL