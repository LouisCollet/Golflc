package utils;
/**
 * using Connector-J
 * @author Louis Collet
 * @since      1.0
 */

//import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
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
private static int starthole;
private static String qualifying;
private static int [][] points = null; //new int [18][6];
/**
 *
 */
//@Deprecated
public GolfMySQL()    // constructor
{
    nb++;    // comment
    //LOG.info("class Name = " + className);
    LOG.info(" from constructor GolfMySQL = " + nb);
}

// ---------------------------------------
public String [] getQueryLoadArray(Connection conn) throws SQLException
{   // charge Array points Ã  partir table myPoints
    Statement st = null;
    ResultSet rs = null;
try
{
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
/**
 *
 * @param in_player
 * @param in_round
 * @return
 * @throws SQLException
 */
/*
public static String [] getStoredList_points(final Connection conn, final Player player, final Round round) throws SQLException, Exception
{
    LOG.info(" -- Start of get StoredList_points - Connection = "+ conn);
 //   CallableStatement cs = null;
try
{
    
    /*
    
    final String stored_name = "get_list_points(?,?,?,?,?,?,?,?,?,?,?,?,?)";   // nom de la stored pro, 10 parameters
        LOG.info(" -- Start with : " + stored_name + " Player = " + player + " Round = " + round);
    cs = conn.prepareCall("{CALL " + stored_name + "}");
    String p = cs.toString();
        LOG.debug("Callable statement " + p.substring(p.indexOf(":"), p.length() ));
 ///   utilspackage.LCUtil.listMetaStoredPro(meta, conn, "get_list_points"); // build + compile ??
 //Bind IN parameter first : player and round date
      cs.setInt(1, player.getIdplayer() );
            LOG.info("Player = " + player);
      cs.setInt(2,round.getIdround());
            LOG.info("Id round = " + round);
 // Register OUT parameters
      cs.registerOutParameter(3, Types.DOUBLE); // # rows
      cs.registerOutParameter(4, Types.DOUBLE); // slope int
      cs.registerOutParameter(5, Types.DECIMAL); // rating dec 3,1 ex 77.1
      cs.registerOutParameter(6, Types.DECIMAL); // handicap dec 3,1 ex 33.0
      cs.registerOutParameter(7, Types.DOUBLE); // par du parcours ex : 72
      cs.registerOutParameter(8, Types.BOOLEAN); // indic error 1 = OK
      cs.registerOutParameter(9, Types.VARCHAR); // reason error
      cs.registerOutParameter(10, Types.VARCHAR); // csa
      cs.registerOutParameter(11, Types.INTEGER); // holes : 9 or 18
      cs.registerOutParameter(12, Types.CHAR); // qualifying
      cs.registerOutParameter(13, Types.INTEGER); // start hole : 1 or 10 new 06/01/2012
        LOG.debug(" -- Callable Statement completed = " + cs.toString());
      cs.execute();

      LOG.info("unused parameter 3"); // - rows  = " + cs.getDouble("out_rows") ); // = name used in stored pro
      LOG.info("Out parameter 4 - slope = " + cs.getDouble("out_slope") );
      LOG.info("Out parameter 5 - rating= " + cs.getBigDecimal("out_rating") );
      LOG.info("Out parameter 6 - hcp   = " + cs.getBigDecimal("out_handicap") );
      LOG.info("Out parameter 7 - par   = " + cs.getDouble("out_par") );

      LOG.info("Out parameter 8 - error_indic = " + cs.getBoolean("out_error_indic") ); // mod 7/4/2012
      LOG.info("Out parameter 9 - error_reason = " + cs.getString("out_error_reason") ); // mod 7/4/2012

      LOG.info("Out parameter 10 - csa   = " + cs.getString("out_csa") );
      LOG.info("Out parameter 11 - holes = " + cs.getString("out_holes") );
      LOG.info("Out parameter 12 - qualifying = " + cs.getString("out_qualifying") );
      LOG.info("Out parameter 13 - start hole = " + cs.getString("out_start") ); // new 06/01/2012

      boolean indic = cs.getBoolean("out_error_indic");
      String error = cs.getString("out_error_reason");
        LOG.info("Value of error indicator = " + indic ); // was getString mod 7/4/2012
        LOG.info("Value of error reason = " + error); // was getString mod 7/4/2012
      if (indic)    // si true, alors pas d'erreurs
      {
          LOG.info("indic = no errors in stored pro = " + error );
      }else{
///////          //adapter : renvoyer la cause de l'erreur Ã  main
      LOG.error("indic = false : error = " + error );
          array_return_error[0]= "ERROR";
          array_return_error[1]= error;
          throw new Exception(error + " for round = " + round); // mod 8/2/2012
      }
     // double rows = cs.getDouble(3); not used 06/01/2012
      
             List<StablefordResult> stb = find.FindSlopeRating.getSlopeRating(player, round, conn);
      
      
      
      
///      double slope = cs.getDouble(4);
         double slope = stb.get(0).getTeeSlope(); // new

///      BigDecimal bd_rating = cs.getBigDecimal(5);
      BigDecimal bd_rating = stb.get(0).getTeeRating(); //new
///      if (cs.wasNull())
///        {LOG.info("BigDecimal bd_rating = [NULL]");}
      double rating = bd_rating.doubleValue(); //turn the BigDecimal object into a double

///      BigDecimal bd_hcp = cs.getBigDecimal(6);
    //  BigDecimal bd_hcp = stb.get(0).  ///// Ã  faire
///      player_hcp = bd_hcp.doubleValue();
      player_hcp = 27.3;

///      double par = cs.getDouble(7);
double par = stb.get(0).getCoursePar();
 
 ///     csa = cs.getInt(10);
      csa = stb.get(0).getRoundCBA();
      
      holes = stb.get(0).getRoundHoles();
 ///     holes = cs.getInt(11);
      
 ///     qualifying = cs.getString(12);
      qualifying = stb.get(0).getRoundQualifying();
      
  ///   starthole = cs.getInt(13);
    starthole = stb.get(0).getRoundStart();    // hole de dÃ©part : 1 ou 10 (pour les parcours 9 trous)
    
      LOG.info("Elements de calcul du handicap : slope = " + slope +
              ", rating = " + rating + ", hcp = " + player_hcp +
              ", par = " + par + ", csa = " + csa + ", holes = " + holes);
// -----------------
      handicap_strokes = (int) Math.round( (player_hcp * (slope/113.0) ) + rating - par);
// -----------------
        LOG.info("-- old system full (18 holes) playing hcp  = " + handicap_strokes );
      int playing_hcp_calculated = calculatePlayingHcp(player_hcp, slope, rating, par);
        LOG.info("-- calculated  (18 holes) playing hcp  = " + playing_hcp_calculated );
      
// enlevÃ© 7/12/2013 pour calcul zwanzeurs
    if (holes == 9)
       {
            handicap_strokes = Math.round(handicap_strokes / 2);
           LOG.info("-- reduced (9 holes) playing hcp  = " + handicap_strokes );
       }
      
    array_return_error[0]= "NO ERROR";
    array_return_error[1]= "Handicap = " + Integer.toString(handicap_strokes);
    array_return_error[2]= "";
    return array_return_error;
} // end try

catch(final SQLException sqle)
{
       LOG.error(" -- SQL Exception by LC mod = " + sqle.getMessage());
       LOG.error(" -- ErrorCode = " + sqle.getErrorCode() );
       LOG.error(" -- SQLSTATE =  " + sqle.getSQLState());
       array_return_error[0]= "ERROR";
       array_return_error[1]= "GetStoredListPoints" + sqle.getMessage();
       array_return_error[2]= "ErrorCode = " + sqle.getErrorCode() + " SQLState =  " + sqle.getSQLState();
               LOG.error("-- Â£Â£Â£ Exception stored pro : " + sqle.toString());
       //throw(sqle);
       return array_return_error;

        //LOG.error("sql error return msg =  = " + session.getAttribute("return_msg") );
} // end catch
catch(final Exception e)
{       LOG.error(" -- Exception by LC = " + e.getMessage());
       array_return_error[0]= "ERROR";
       array_return_error[1]= e.getMessage();
       array_return_error[2]= "" ;
       //throw(e);
       return array_return_error;
} // end catch

finally
{
//  cs.close();
  LOG.info("finally : end of getStoredList ");
  //LOG.info(NEWLINE + Arrays.deepToString(points) );
  //return array_return_error;

}
} //end getStoredList_points

// new 04/05/2014

*/

/**
 *
 * @param conn
 * @param tableName
 * @return
 * @throws SQLException
 */

// --------------------- getters ----------------------------
/**
 *
 * @return
 * @throws SQLException
 */
public int getIdRound() throws SQLException
{
    return round;
}

/**
 *
 * @return
 * @throws SQLException
 */
//public static int getPlayingHcp() throws SQLException
//{

// Ã  modifier ultÃ©rieurement
    //LOG.info(" -- getStrokes for 09th element = " + points[9][3]);
    //LOG.info("there are = " + holes);
        // mod 8/12/2012
 //   if (points[9][3] == 0) // 3=strokes : pas de strokes introduits for hole 10
 //   {
 //       handicap_strokes/= 2;  // division par 2 : on complique pour le plaisir
 //       LOG.info(" -- FORCED playing handicap, because no result for hole 10 = " + handicap_strokes);
 //   }
//    return handicap_strokes;
//}

/**
 *
 * @return
 * @throws SQLException
 */
public static double getPlayerHcp() throws SQLException
{
    return player_hcp;
}
/**
 *
 * @return
 * @throws SQLException
 */
public int getCSA() throws SQLException
{
    return csa;
}
/**
 *
 * @return
 * @throws SQLException
 */
//public static int getHoles() // throws SQLException
//{
//    return holes;
//}

    public void setPoints(int[][] points)
    {
        GolfMySQL.points = points;
        LOG.info("GolfMySQL : points transferred !");
    }


public int[][] getPoints(int holes) throws SQLException, Exception
{   // nod 21/06/2015 argument holes
    LOG.info(" ... entering getPoints with holes = " + holes);
    if (holes == 9)
        {points = new int [9][6];
        LOG.info("array created for holes  = " + holes);}
    else if (holes == 18)
        {points = new int [18][6];
        LOG.info("array created for holes  = " + holes);}
    else
        {    LOG.info("[][]holes = not 9 or 18 " + holes);
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