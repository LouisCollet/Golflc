
package calc;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.*;
import utils.ColumnComparator;

public class GolfCalc implements interfaces.GolfInterface , interfaces.Log // GolfInterface // throws IOException
{
private static int nb = 0; 
private static String []array_return_error = new String [3];
private static int [][] points = null; //new int [18][6];
private final static int PTS = 0;
/**
 *
 */
public GolfCalc()// constructor
 {
    nb++;
    //LOG.info(" from constructor GolfWriteLog = ");
 }

    public static void setPoints(int[][] points) {
        GolfCalc.points = points;
    }

public static  int[][] trfPoints() throws SQLException
{    LOG.info("getPoints executed");
    return points;
}

public static String[] setArrayExtraStrokes (int [][] points, int phcp) //, int in_holes)
        // input = playing handicap
        // ajoute les strokes à array points
{
     LOG.info(" -- Start of setArrayExtraStrokes for playing handicap = " + phcp + " ,holes = " + points.length);
try{
    int holes = points.length; //in_holes;

    int res = phcp / holes;
    int rem = phcp % holes;

    LOG.info(" -- setArrayExtraStrokes - loop Complete = " + res);
    LOG.info(" -- setArrayExtraStrokes - loop Uncomplete = " + rem);

    if (res !=0) {setArrayExtraComplete(points, res);} //,holes);} 13/01/2013
        
    if (rem !=0) {setArrayExtraUncomplete(points, rem);} //,holes);} 13/01/2013
 array_return_error[0]= "NO ERROR";
 array_return_error[1]= "Handicap = " + Integer.toString(phcp);
 array_return_error[2]= "Holes = " + Integer.toString(holes);
 return array_return_error; // mod 26/01/2013
} catch (ArithmeticException ae){
     LOG.error(" -- Error" + ae);
     array_return_error[0]= "ERROR";
     array_return_error[1]= "ErrorCode = ArithmeticException";
     array_return_error[2]= ae.getMessage();
     return array_return_error;
}catch (Exception e){
      LOG.error(" -- Error" + e );
     array_return_error[0]= "ERROR";
     array_return_error[1]= "ErrorCode = Exception";
     array_return_error[2]= e.getMessage();
     return array_return_error;
}
finally
{

LOG.info(NEWLINE + Arrays.deepToString(points) );
 }
} // end method setExtra Strokes

// -----------------------------------------------------
private static void setArrayExtraComplete (final int [][] points, final int in_times) //13/01/2013 , final int holes) // ajoute un stroke à chaque hole
{
  LOG.info(" -- Start of setArrayExtraComplete for the " + in_times + " times" );
 for (int k=0; k<in_times;k++)
{
      for (int[] point : points) // mod 11/01/2013
      {
          point[4]++;
          // LOG.info(className + "." + methodName + " -- complete Extra added = " + i + " " + points [i][4]);
      } // end for2
} //end for1

LOG.info(NEWLINE + Arrays.deepToString(points) );
} // end method
// -------------------------------------------
@SuppressWarnings("unchecked")
private static void setArrayExtraUncomplete (final int [][] points, final int in_strokes) //13/01/2013, final int holes)
{
   LOG.info(" Start of setArrayExtraUncomplete for strokes = " + in_strokes + " ,holes = " + points.length);
try
{
if (points.length == 9) // new 12/01/2012
{
    // why ? ex bawette si = 10,8,12,18,16,4,14,6,2
    // first , sort on an other (work) array = bz
 //http://techthinking.net/2010/02/sorting-two-dimensional-string-array-using-java/
 //http://stackoverflow.com/questions/10076982/sorting-java-multidimensional-array?rq=1

    int[][] bz = points; //.clone();
    Arrays.sort(bz, new ColumnComparator(2));  // sort Array on stroke index, col 3
        LOG.info(" bz sorted = " + NEWLINE + Arrays.deepToString(bz) );
    int hit = 0;
        LOG.info(" -- uncomplete Extra Stroke = " + " strokes = " + in_strokes);
    for (int[] bz1 : bz) {
        if (hit < in_strokes) {
            LOG.info(" stroke added for hole = " + bz1[0] + " ,hit = " + hit + " ,index = " + bz1[2]);
            bz1[4]++;
            hit++;
        } else {
            LOG.info(" -- NOT added for hole = " + bz1[0] + " ,index = " + bz1[2]);
        } //end else
    } // end for
        LOG.info(" bz added = " + NEWLINE + Arrays.deepToString(bz) );
   //points = bz;

        LOG.info("array trié sur SI = " + points[0][0]);
    Arrays.sort(points, new ColumnComparator(0)); // restore initial state : sort Array on hole #, col 1
        LOG.info("array trié sur Hole # = " + points[0][0]);
//LOG.info(points.length + " 9 HOLES, Stroke Index adapted = " + NEWLINE + Arrays.deepToString(points) );
} // enf if 9 holes

   //if (in_strokes != 0)
if (points.length == 18) // new 11/01/2012
{ //int max = 18 - strokes;  // trous les + difficiles, 1 aux trous dont l'index >
     int max = in_strokes + 1; // new 17/7/2011
    //for (int i=0; i<holes; i++)
    for (int[] point : points) {
        if (point[2] < max) {
            //LOG.info("before = " + points[i][2] + " / " + points[i][4]);
            point[4]++;
            LOG.info(" -- added for hole = " + point[0]);
            LOG.info("after  = " + point[2] + " / " + point[4]);
        } else {
            //LOG.info("not adapted = " + points[i][2] + " / " + points[i][4]);
            LOG.info(" -- not added for hole = " + point[0]);
        } //end else
    } // end for
//LOG.info(points.length + " HOLES, Stroke Index adapted = " + NEWLINE + Arrays.deepToString(points) );
} //end if
} catch (Exception e) {
     array_return_error[0]= "ERROR";
     //array_return_error[1]= "ErrorCode = ";
     array_return_error[2]= e.getMessage(); // from throw new exception !!!
//     e.printStackTrace();
 }
 finally
 {
    //LOG.info(" -- array = " + Arrays.deepToString(points) );
    LOG.info(points.length + " HOLES, Stroke Index adapted = " + NEWLINE + Arrays.deepToString(points) );
    //return array_return_error;
 }
} // end method

// --------------------------

public static String[] setArrayPoints(int[][] points) // (int in_holes)
        // points comme les pros - sans points stableford
        // ajouter une field in_holes pour distinguer 9 et 18 holes
{
    LOG.info(" -- Start setArrayPoints with holes = " + points.length );
try
{
    // points [i][0] = hole
    // points [i][1] = par
    // points [i][2] = index
    // points [i][3] = strokes
    // points [i][4] = extra
    // points [i][5] = points stableford
    for (int i=0; i<points.length; i++)
    {
                LOG.info(" -- setArrayPoints = " + Arrays.deepToString(points) );
            int strokes = points [i][3];
  ////              LOG.info(" -- setArrayPoints, strokes 01 = " + strokes);
            if (strokes == 0)  // pas possible !!!
            {   LOG.info(" -- ERROR:strokes = 0, setArrayPoints, strokes [3] = " + points[i][3] + " /i= " + i);
                LOG.info(" -- ERROR:strokes = 0, setArrayPoints, extra   [4] = " + points[i][4]);
                array_return_error[0]= "ERROR";
                array_return_error[1]= "  -- Exception GolfCalc.setArrayPoints = null strokes : " + Integer.toString(strokes);
                array_return_error[2]= Integer.toString(strokes);
                throw new Exception(" -- Exception = throw null strokes : " + Integer.toString(strokes));
            }
            int net = 0;
            net = points [i][3] - points [i][4];         // strokes - extra
  ////              LOG.info(" -- setArrayPoints, strokes 02 = " + net);
            if (net < 0)  // pas possible !!!
            {   LOG.info(" -- ERROR, strokes negative, setArrayPoints, strokes [3] = " + points[i][3] + " /i= " + i);
                LOG.info(" -- ERROR, strokes negative, setArrayPoints, extra   [4] = " + points[i][4]);
                array_return_error[0]= "ERROR";
                array_return_error[1]= "  -- Exception GolfCalc.setArrayPoints = throw negative strokes : " + Integer.toString(net);
                array_return_error[2]= Integer.toString(net);
                throw new Exception(" -- Exception = throw negative strokes : " + Integer.toString(strokes));
            }
            int par = points [i][1];
            points [i][5] = getPointsNew(net, par);
               LOG.info("calcul 0 = " + points [i][5]);
     } // end for
 array_return_error[0]= "NO ERROR";
 array_return_error[1]= "OK, tout roule !!!";
 array_return_error[2]= "Holes = " + Integer.toString(points.length);
 return array_return_error;
 } catch (Exception e) {
     array_return_error[0]= "ERROR";
     //array_return_error[1]= "ErrorCode = ";
     array_return_error[2]= e.getMessage(); // from throw new exception !!!
//     e.printStackTrace();
    return array_return_error;
 }
 finally
 {
    //LOG.info(" -- array = " + Arrays.deepToString(points) );
    LOG.info(NEWLINE + Arrays.deepToString(points) );
 //   return array_return_error;
 }
} // end method setPointsWithoutExtraStrokes
// ---------------------------

private static int getPointsNew(final int net, final int par)
{ // nouvelle méthode de calcul des points - 21/08/2016
    switch (par - net)
    {
        case 0  : return 2; // par
        case -1 : return 1; // bogey
        case 1  : return 3; // birdie
        case 2  : return 4; // eagle
        case 3  : return 5; // albatros
        default:
           String msg = " -- Falling in Default in getPointsNew - 0 points, par = " + par + " net = " + net;
           LOG.info(msg) ;
           return 0;
    } // end switch
} // end method

// -----------------------------------------------------------------------------

public static int getRoundStablefordResult (int [][] points) //(int in_holes)
        // totalise les points par hole
{
  LOG.info(" -- Start of getRoundStablefordResult with holes = " + points.length);
  LOG.info(NEWLINE + Arrays.deepToString(points) );
  int roundResult = 0;
    for (int[] point : points) {
        roundResult = roundResult + point[5];
    }
LOG.info(" -- round Result = " + roundResult);
return roundResult;
} // end method getRoundStablefordResult

public static int getRoundZwanzeursResult (int [][] points, int in_totalpar, int in_handicap) //new 7/12/2013
        // à modifier !!
{
  LOG.info(" -- Start of getRoundZwanzeursResult : ") ; //with holes = " + points.length + " totalpar = " + in_totalpar);
  LOG.info(" -- holes = " + points.length);
  LOG.info(" -- totalpar = " + in_totalpar);
  LOG.info(" -- handicap = " + in_handicap);
  LOG.info(NEWLINE + Arrays.deepToString(points) );
  int roundResult = 0;
  int strokes = 0;
    for (int[] point : points) {
        strokes = strokes + point[3];
    }
LOG.info(" -- total strokes  = " + strokes);

BigDecimal bg1 = BigDecimal.valueOf(strokes * 2 );
    LOG.info(" from getZwanzeur strokes : " + bg1);
BigDecimal bg2 = BigDecimal.valueOf(in_totalpar);
    LOG.info(" from getZwanzeur totalpar : " + bg2);
BigDecimal bg3 = BigDecimal.valueOf(in_handicap * 2); // handicap input = sur 9 holes , ici on calcule sur 18
    LOG.info(" from getZwanzeur handicap : " + bg3);
//BigDecimal bg4 = bg3.setScale(0, BigDecimal.ROUND_HALF_UP);
BigDecimal bg4 = bg3.setScale(0, RoundingMode.HALF_UP);
    LOG.info(" from getZwanzeur bg4 : " + bg4);
BigDecimal zwanzeur = bg1.subtract(bg2).subtract(bg4);
    LOG.info(" from getZwanzeur : " + zwanzeur);
roundResult= zwanzeur.intValue();
    LOG.info(" Result Zwanzeur : " + roundResult);
return roundResult;

} // end method getRoundZwanzeursResult
public static int getRoundGreenshirtResult (int [][] points, int in_handicap, String competition) //new 7/12/2013
{
    LOG.info(" -- Start of getRoundGreenshirtResult : ");
    LOG.info(" -- holes = " + points.length);
    LOG.info(" -- handicap zwanzeur = " + in_handicap * 2); // *2 parce que handicap 9 trous (divisé par 2)
    LOG.info(" -- competition = " + competition); 
  boolean green_hiver = competition.contains("hiver");
    LOG.info(" -- competition contains hiver = " + green_hiver); 
    LOG.info(NEWLINE + Arrays.deepToString(points) );
  int roundResult = 0;
if (green_hiver == false) // zwanzeurs : les points par/greenshirt ne comptent pas pour les greens d'hiver
{
    for (int i=0; i<points.length; i++)
    {
       if (points [i][1] == points [i][3]) // si Par   = strokes
       { 
           roundResult = roundResult + (in_handicap*2); // autant de points greenshirt que handicap
       }
    }
}
    LOG.info(" Result greenshirt : " + roundResult);
return roundResult;

} // end method getRoundGreenshirtResult
// -----------------------------------------------------------------------------

public static void main(String[] args) //throws SQLException // testing purposes
{
//double res = calcNewHandicap (41,33.2,round ??);
// LOG.info(" -- res = " + res );

}// end main

} // end class