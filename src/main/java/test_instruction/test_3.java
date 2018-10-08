package test_instruction;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
//import org.apache.commons.lang3.ArrayUtils;


public class test_3 implements interfaces.Log
{

  public static void main(String[] args)
  {
      Map<String, Integer> items = new HashMap<>();

        items.put("coins", 3);
        items.put("pens", 2);
        items.put("keys", 1);
        items.put("sheets", 12);

        items.forEach((k, v) -> {
            System.out.printf("%s : %d%n", k, v);
        });

      
      
   // from Double[] to double[]   
      Double[] boxed = new Double[] { 1.0, 2.0, 3.0 };
      double[] unboxed = Stream.of(boxed).mapToDouble(Double::doubleValue).toArray();
   //   LOG.info("itme double = " + Arrays.deepToString(unboxed));
      for(double speed : unboxed) {
            LOG.info("Print ad element double = " + speed);
        }
    // from double[] to Double[]
        double[] boxed1 = new double[] { 26.1, 2.0, 3.0 };
        
String numberAsString = "153.25";
double number = Double.parseDouble(numberAsString);
System.out.println("The number is: " + number);


   double[] ddouble = new double[] { 26.1, 2.0, 3.0, 29.4 };
//   System.out.println("double Array[ = " + Arrays.deepToString(ddouble));
   // Double[] dDouble = new Double[] { 0.0, 0.0, 0.0 };
   Double[] dDouble = utils.LCUtil.doubleArrayToDoubleArray(ddouble);
   
   LOG.info("Double Array[ = " + Arrays.deepToString(dDouble));
/*   
   System.out.println("Double Array = " + Arrays.deepToString(dDouble));
   for(int i=0; i < ddouble.length ; i++)
   {
       double d = ddouble[i];
    Double dd = new Double(d);
 //      System.out.println("dObj dd = " + dd);
   dDouble[i] = dd;    
 //   System.out.println("dDouble[i] = " + dDouble[i]);
   }
   */
    /*
    Use Double constructor to convert double primitive type to a Double object.
    */

 //   Double dObj = new Double(d);
 //   dDouble[1] = valueOf(ddouble[1]);
  //  dDouble[1] = ddouble[1]..doubleValue());
  //  System.out.println("dObj = " + dDouble[1]);


//String numberAsString2 = "153.25";




//We can shorten to:

//String numberAsString = "153.25";
//double number = new Double(numberAsString).doubleValue();


   //     double[] tempArray = new double[4];
   //     int i = 0;
   //     for(Double d : (Double) data[columnIndex]) {
   //         tempArray[i] = (double) d;
   //         i++;
   //     }
        
        
        for(Double speed : unboxed) {
            LOG.info("Print ad element Double = " + speed);
        }

      List<String> myList = new ArrayList<>();
    myList.add("Black");
    myList.add("White");
    myList.add("Yellow");
    myList.add("Blue");
    myList.add("Red");
    System.out.println("longueur list = " + myList.size());

String[] myArray = new String[myList.size()];
myList.toArray(myArray);

for (String myString:myArray) {
   System.out.println("myString = " + myString);
}
      
      System.out.println("myArray = " + Arrays.deepToString(myArray));
      
      
      
      
      
      
      
      
      
      
      
      
      
      String s1 = "Corinne Henrotte <henrottecorinne@hotmail.com>"; 
      System.out.println(s1);
      System.out.println(s1.indexOf('<'));
      System.out.println(s1.indexOf('>'));
      System.out.println(s1.substring(s1.indexOf('<')+1, s1.indexOf('>')) ); 
      s1 = "brigitte.vanderschueren@gmail.com"; 
      System.out.println(s1);
 //     int found = s1.indexOf('<');
      if (s1.indexOf('<') != -1) {
        System.out.println(s1.substring(s1.indexOf('<')+1, s1.indexOf('>')) ); 
      }else{
        System.out.println(s1);
      }

      System.out.println(s1.indexOf('<'));
      
      
      
       s1 = s1.trim(); //.replace(" ","0");
      System.out.println( "s1 = /" + s1 + "/");
      String s2 = " 4";
 //     s2 = s2.replace(" ","0");
 //     System.out.println( "s2 = /" + "/");
      if(s2 == "  ")      {
          System.out.println( "s2 = blanco/" + "/");
          s2 = "0";
      }else{
       s2 = s2.trim();} //.replace(" ","0");
      System.out.println( "s2 = /" + s2 + "/");
      
      
  }    
}