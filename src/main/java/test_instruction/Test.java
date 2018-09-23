
package test_instruction;

import entite.ScoreMatchplay;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import utils.LCUtil.*;
/**
 *
 * @author collet
 */
public class Test implements interfaces.GolfInterface{
   public static void main(String args[]) throws IOException  {
   try{
       
  //     format(5257);
  //      format(1308.75);
  //      format();
 
  
  
  
  
  String[] firstArray = {"test1", "", "test2", "test4", "", null};

LOG.info("datesSeason with null = " + Arrays.deepToString(firstArray));
    List<String> list = new ArrayList<String>();
    for(String s : firstArray) {
       if(s != null && s.length() > 0) {
          list.add(s);
       }
    }
    firstArray = list.toArray(new String[list.size()]);
   LOG.info("datesSeason without null = " + Arrays.deepToString(firstArray));
   
   // java 8
   String[] firstArra = {"test1", "", "test2", "test4", "", null};
        firstArra = Arrays.stream(firstArra)
                     .filter(s -> (s != null && s.length() > 0))
                     .toArray(String[]::new);    
   LOG.info("datesSeason without null arra = " + Arrays.deepToString(firstArra));
   
   
   
   String [][]datesSeason = new String[20][3];
       // fill 1st row
       datesSeason[0][0] = ("01/01/2018");
       datesSeason[0][1] = ("28/02/2018");
       datesSeason[0][2] = ("M");
       // fil 2nd row
       datesSeason[1][0] = ("01/03/2018");
       datesSeason[1][1] = ("31/05/2018");
       datesSeason[1][2] = ("H");
       // fil 3rd row
       datesSeason[2][0] = ("01/06/2018");
       datesSeason[2][1] = ("30/06/2018");
       datesSeason[2][2] = ("M");
       datesSeason[3][0] = ("01/07/2018");
       datesSeason[3][1] = ("31/08/2018");
       datesSeason[3][2] = ("L");
       datesSeason[4][0] = ("01/09/2018");
       datesSeason[4][1] = ("30/09/2018");
       datesSeason[4][2] = ("M");
       datesSeason[5][0] = ("01/10/2018");
       datesSeason[5][1] = ("30/11/2018");
       datesSeason[5][2] = ("H");
       datesSeason[6][0] = ("01/12/2018");
       datesSeason[6][1] = ("31/12/2018");
       datesSeason[6][2] = ("L");
   LOG.info("datesSeason with nulls    = " + Arrays.deepToString(datesSeason));
  
   datesSeason = utils.LCUtil.removeNull2D(datesSeason);
   LOG.info("datesSeason removed nulls = " + Arrays.deepToString(datesSeason));
      
Double d = 26.200000000000003;
        DecimalFormat df = new DecimalFormat("##.0");
        LOG.info("decimal format = " + d + " -> " + df.format(d));
       
       LOG.info("%.2f", d); // OR
       
       LOG.info("string format = " + String.format( "%.1f", d));
       
 ScoreMatchplay mp = new ScoreMatchplay();
         String[] players = {"2014210", "2014211", "2014112", "2014109"};
        String str = Arrays.toString(players);
            System.out.println("str = " + str);
        String[] array = utils.LCUtil.stringToArray1D(str);
  System.out.println(" arrays equals ? = " + Arrays.equals(players, array));
  //////////////////////////
  
  String input = "4,4,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0;"
          + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0;"
          + "5,5,5,5,5,5,5,5,5,0,0,0,0,0,0,0,0,0;"
          + "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0;"
          + "A,A,A,A,A,A,A,A,A,0,0,0,0,0,0,0,0,0;"
          + "1,2,3,4,5,6,7,8,9,0,0,0,0,0,0,0,0,0"; 
  
   String co = "4,4,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0;0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0;5,5,5,5,5,5,5,5,5,0,0,0,0,0,0,0,0,0;0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0;A,A,A,A,A,A,A,A,A,0,0,0,0,0,0,0,0,0;1,2,3,4,5,6,7,8,9,0,0,0,0,0,0,0,0,0";
// String S = utils.LCUtil.compress(input);   // on store dans DB
  mp.setScoreString(input);
  //System.out.println("in db = " + mp.getScoreString() );
System.out.println("back ? = " );
 
// String input1 = utils.LCUtil.decompress(mp.getScoreString());
 String input1 = mp.getScoreString();
 
   System.out.println("equal ? = " + input.equals(input1));
   // donc on store dans DB

   String[][] array2D = utils.LCUtil.stringToArray2D(input1);

// int[][] A_D2 = {{ 4,  4,  5,  3,  4,  2,  4,  4,  4,  3,  4,  5,  4,  3, 4 ,  4, 0 , 0 }, { 0, 0 ,0 ,0 ,0  ,0  , 0 ,0  , 0 ,0  ,0  , 0 , 0 ,0  , 0 , 0 ,0 , 0 }, { 5,  6,  4,  3,  4,  2,  4,  4,  4,  3,  4,  5, 4,  4,  4,  6, 0 ,0  }, {0 , 0 ,0  ,0  ,0  ,0 , 0 ,0  , 0 , 0 , 0 ,0  ,0  , 0 ,0 ,0  ,0  , 0 }, {0 ,0  , 0 ,0  ,0 , 0 ,0 ,  0, 0 ,0 ,0 ,0  , 0 , 0 ,0  ,0  ,0  ,0  }, { 0, 0 , 0 ,0  , 0 , 0 , 0 , 0 ,0  , 0 , 0 , 0 , 0 ,0 , 0 , 0 , 0 ,0  }};     
 String[][] A_D2 = {{ "4",  "4",  "5",  "3",  "4",  "2",  "4",  "4",  "4",  "3",  "4",  "5",  "4",  "3", "4" ,  "4", "0" , "0" }, { "0", "0" ,"0" ,"0" ,"0"  ,"0"  , "0" ,"0"  , "0" ,"0"  ,"0"  , "0" , "0" ,"0"  , "0" , "0" ,"0" , "0" }, { "5",  "6",  "4",  "3",  "4",  "2",  "4",  "4",  "4",  "3",  "4",  "5", "4",  "4",  "4",  "6", "0" ,"0"  }, {"0" , "0" ,"0"  ,"0"  ,"0"  ,"0" , "0" ,"0"  , "0" , "0" , "0" ,"0"  ,"0"  , "0" ,"0" ,"0"  ,"0"  , "0" }, {"0" ,"0"  , "0" ,"0"  ,"0" , "0" ,"0" ,  "0", "0" ,"0" ,"0" ,"0"  , "0" , "0" ,"0"  ,"0"  ,"0"  ,"0"  }, { "0", "0" , "0" ,"0"  , "0" , "0" , "0" , "0" ,"0"  , "0" , "0" , "0" , "0" ,"0" , "0" , "0" , "0" ,"0"  }};     
 
System.out.println(" starting : with 2D array = " + Arrays.deepToString(A_D2));
    int lon = A_D2.length;
    System.out.println(" starting : with 2D array length= " + lon);
String S_D2 = utils.LCUtil.array2DToString(A_D2);
 System.out.println(" string from array = " + S_D2);

 String S_C = utils.LCUtil.compress(S_D2);
 String S_D = utils.LCUtil.uncompress(S_C);
// String A_Back[][] = stringToArray2D(S_D, lon);
//  System.out.println(" finishing : with 2D array = " + Arrays.deepToString(A_Back));
// System.out.println(" final result = " + Arrays.deepEquals(A_D2, A_Back));

 }catch (Exception ex){
            String msg = "Exception !  = "// + e.getFile().getFileName()
                    + " is NOT  = " + ex.getMessage();
            System.out.println(msg);
        }
    } //end method main
 

  } //end class