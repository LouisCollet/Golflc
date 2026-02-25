
package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.lang.System.out;
/**
 *https://docs.oracle.com/en/java/javase/24/language/java-language-changes-release.html#GUID-6459681C-6881-45D8-B0DB-395D1BD6DB9B
 * https://docs.oracle.com/en/java/javase/24/language/pattern-matching-switch.html#GUID-08C56E57-B95B-45D8-930D-E3FAFA29B5B7
 */
public class Test_preview{
/*
    List<List<String>> records = new ArrayList<>();
    private final static String COMMA_DELIMITER = ",";
          record Point(int x, int y) { }
     enum Color { RED, GREEN, BLUE; }
//    public void logout(String lgt) throws FileNotFoundException, IOException{
//try (BufferedReader br = new BufferedReader(new FileReader("book.csv"))) {
//    String line;
//    while ((line = br.readLine()) != null) {
//        String[] values = line.split(COMMA_DELIMITER);
//        records.add(Arrays.asList(values));
//    }
//} //end method
 //  }

 //public void handlecsv (String lgt) throws FileNotFoundException, IOException, CsvValidationException{
 //       List<List<String>> recs = new ArrayList<>();
 //   try (CSVReader csvReader = new CSVReader(new FileReader("book.csv"));) {
  //      String[] values = null;
  //      while ((values = csvReader.readNext()) != null) {
  //          recs.add(Arrays.asList(values));
  //  }
//}
//} 

 public static void main(String[] args) {

    try{ 
  //    record Point(int x, int y) { }
  //   enum Color { RED, GREEN, BLUE; }
     Point obj = new Point(5,7);   
        typeTester(obj);
            // java 23 Old way Primitive Pattern Matching not supported
            long lo = 100;
         bigNumbers(lo);
         double rating = 3.5;
         String s = doubleToRating(rating);
           System.out.println("s = " + s);
         patternMatching();
         s = getHttpStatusMessage(700);
           System.out.println("s = " + s);
        } catch (Exception e) {
            out.println("fatal error : " + e);
        }
 } //end method
 static void  bigNumbers(long v) {
       System.out.println("-- entering bigNumbers");
    switch (v) {
        case long x when x < 1_000_000L ->
            out.println("Less than a million");
        case long x when x < 1_000_000_000L ->
            out.println("Less than a billion");
        case long x when x < 1_000_000_000_000L ->
            out.println("Less than a trillion");
        case long x when x < 1_000_000_000_000_000L ->
            out.println("Less than a quadrillion");
        default -> System.out.println("At least a quadrillion");    
    }
} // end method
 
 // https://docs.oracle.com/en/java/javase/24/language/type-patterns.html#GUID-E9B6F4DC-5863-4714-9DC0-78BF933BD818
static void typeTester(Object obj) {
    System.out.println("-- entering typeTester");
    switch (obj) {
        case null     -> out.println("null");
        case String s -> out.println("String");
        case Color c  -> out.println("Color with " + c.values().length + " values");
        case Point p  -> out.println("Record class: " + p.toString());
        case int[] ia -> out.println("Array of int values of length" + ia.length);
        default       -> out.println("Something else");
    }
}
 static String doubleToRating(double rating) {
     out.println("-- entering doubleToRating");
    return switch(rating) {
        case 0d -> "0 stars";
        case double d when d > 0d && d < 2.5d
            -> d + " is not good";
        case double d when d >= 2.5f && d < 5d
            -> d + " is better";
        case 5d -> "5 stars";
        default -> "Invalid rating";
    };
} // end method
 
  static void patternMatching() {
      System.out.println("-- entering patternMatching");
    Object o = 127;
    switch (o) {
        case Integer i -> out.println("Integer: "+ i);
        case Long l -> out.println("Long: "+ l);
        default -> out.println("Other1: "+ o);
    }
    // Java 23: Primitive Pattern Matching supported
   // out.println("Pattern Matching Java 23 New Model.... Primitive Supported");
  //  o = 126;
    switch (o) {
        case int in -> out.println("Java 23 int: "+ in);
        case long l0 -> out.println("Java  23 long: "+ l0);
        default -> out.println("Other2: "+ o);
    }
  //  https://www.happycoders.eu/java/primitive-type-patterns/
    out.println("Other2");
    int value = 65;
  if (value instanceof byte b)   System.out.println(value + " instanceof byte:   " + b);
  if (value instanceof short s)  System.out.println(value + " instanceof short:  " + s);
  if (value instanceof int i)    System.out.println(value + " instanceof int:    " + i);
  if (value instanceof long l)   System.out.println(value + " instanceof long:   " + l);
  if (value instanceof float f)  System.out.println(value + " instanceof float:  " + f);
  if (value instanceof double d) System.out.println(value + " instanceof double: " + d);
  if (value instanceof char c)   System.out.println(value + " instanceof char:   " + c);
    
  out.println("new");
    float value1 = 3.5f;
  if (value1 instanceof byte b)   System.out.println(value + " instanceof byte:   " + b);
  if (value1 instanceof short s)  System.out.println(value + " instanceof short:  " + s);
  if (value1 instanceof int i)    System.out.println(value + " instanceof int:    " + i);
  if (value1 instanceof long l)   System.out.println(value + " instanceof long:   " + l);
  if (value1 instanceof float f)  System.out.println(value + " instanceof float:  " + f);
  if (value1 instanceof double d) System.out.println(value + " instanceof double: " + d);
  if (value1 instanceof char c)   System.out.println(value + " instanceof char:   " + c);
 
  out.println("new 2");  
    double value2 = 100000.0;
  switch (value2) {
    case byte   b -> System.out.println(value2 + " instanceof byte:   " + b);
    case short  s -> System.out.println(value2 + " instanceof short:  " + s);
    case char   c -> System.out.println(value2 + " instanceof char:   " + c);
    case int    i -> System.out.println(value2 + " instanceof int:    " + i);
    case long   l -> System.out.println(value2 + " instanceof long:   " + l);
    case float  f -> System.out.println(value2 + " instanceof float:  " + f);
    case double d -> System.out.println(value2 + " instanceof double: " + d);
  }
 //In the following examples, I have used the unnamed variable _ (underscore), finalized in Java 22.
  
  double value4 = 3.5;
switch (value4) {
//  case byte   _ -> System.out.println(value4 + " instanceof byte");
//  case int    _ -> System.out.println(value4 + " instanceof int");
  case double   _ -> System.out.println(value4 + " instanceof double");
}
  short value5 = 3;
switch (value5) {
  case byte   _ -> System.out.println(value5 + " = instanceof byte");
  case int    _ -> System.out.println(value5 + " = instanceof int");
}
  
    
 //   };
} // end method
  
  private static String getHttpStatusMessage(int code) {
        System.out.println("-- entering getHttpStatusMessage");
  return switch (code) {
    case 200 -> "OK";
    case 400 -> "Bad request";
    case 404 -> "Not found";
    case 500 -> "Internal server error";

    case int i when i > 100 && i < 200 -> "Informational";
    case int i when i > 200 && i < 300 -> "Success";
    case int i when i > 302 && i < 400 -> "Redirection";
    case int i when i > 400 && i < 500 -> "Client error";
    case int i when i > 502 && i < 600 -> "Server error";
    default -> "Unknown code";
  };
  
} // end method
  */
}// end class