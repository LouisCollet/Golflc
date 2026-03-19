package utils;

//import com.google.inject.internal.util.ImmutableMap;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;


public class test_1{

    
    
 /*  public static String typedPatternMatching(Object o) {
  return switch(o) {
    case null      -> "I am null";
    case String s  -> "I am a String. My value is " + s;
    case Integer i -> "I am an int. My value is " + i;
    default        -> "I am of an unknown type. My value is " + o.toString();
  };
}
*/
  void main() throws ParseException  {
//try{ ^▲

   //    System.out.println("isNamed: " + typedPatternMatching(null));

   //  printMap(FacesContext.getCurrentInstance().getExternalContext().getSessionMap());

String unsafeInputString = "A-1Up";
Pattern p = Pattern.compile ("U|&"); //("^[^<>§%\\$]*$");
Matcher m = p.matcher(unsafeInputString);
if(m.matches()){
    LOG.info("Invalid input: reject it, or remove/change the offending characters");
}else{
    LOG.info("VALID input: accept it !!");
}

short s1 = 10;                     // 2 bytes
    int i1 = s1;                       // 2 bytes assigned to 4 bytes
 
    System.out.println("short value: " + s1);            // prints 10
    System.out.println("Converted int value: " + i1);    // prints 10


              int month = 1;
           String monthString = switch (month) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 ->  "March";
            case 4 ->  "April";
            case 5 ->  "May";
            case 6 ->  "June";
            case 7 ->  "July";
            case 8 ->  "August";
            case 9 ->  "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> "Invalid month";
} ;
           LOG.info("new switch insgruction : " + monthString);

     List<String> list = new ArrayList<>();
     list.add("first");
     list.add("second");
     list.add("third");
     LOG.info("list 2 = " + list);
            // creating object of List<Integer> 
            List<String> arrlist = new ArrayList<>();
       //     List<Output> outputList = new ArrayList<Output>();

            // Adding element to srclst 
            arrlist.add("A"); 
            arrlist.add("B"); 
            arrlist.add("Cz"); 
    //        boolean containsElement = arrlist.contains("Cz");
    //          LOG.info("containsElement =  " + containsElement);
              if(arrlist.contains("Cz")){
                  LOG.info("containsElement2 = true ");
              }else{
                  LOG.info("containsElement2 = false ");
              }
              
         //Converting the ArrayList to a Long
           String[] array = list.toArray(new String[list.size()]);

        //Printing the results
          LOG.info("résultat = " + array[0] + " " + array[1] + " " + array[2]);     
              
 List<String> lis = new ArrayList<>();
//... add values lis.add"
lis.add("premier 1");
lis.add("second 2");
lis.add("troisième 3");
 String[] array2 = lis.toArray(new String[lis.size()]);
LOG.info("array2 = " + Arrays.toString(array2));
String[] resultArray = new String[lis.size()];
resultArray = lis.toArray(resultArray);
LOG.info("array copied");
LOG.info("resultAay mod = " + Arrays.toString(resultArray));
 
 //List<String> li = Arrays.asList(resultArray);
//LOG.info("list copiée = " + li.toString());


List<String> ls = new ArrayList<>(Arrays.asList(resultArray));// éviter java.lang.UnsupportedOperationException !!
ls.add("quatrième 4");
LOG.info("list after add quatrième = " + ls.toString());

              
              
            int index1 = arrlist.indexOf("B");
            LOG.info("index1  " + index1);
            // print the elements 
            System.out.println("List elements before fill: "
                               + arrlist); 
  
            // fill the list 
            Collections.fill(arrlist, "TAJMAHAL"); 
  
            // print the elements 
            System.out.println("\nList elements after fill: "
                               + arrlist); 






LOG.info("LoclaDateTime now plus 10 minutes= " + LocalDateTime.now().plusMinutes(10));


Date date = new Date();
LOG.info("date = " + date);


String dup = "Duplicate entry 'teste.test@test.be' for key 'player.unique_email'";
if(dup.endsWith("'player.unique_email'")){
    LOG.info("cette adresse mail est déjà utilisée");
};

DecimalFormat df = new DecimalFormat();
df.setGroupingSize(4);
df.setGroupingUsed(true);
DecimalFormatSymbols dfs = new DecimalFormatSymbols();
dfs.setGroupingSeparator(' ');
df.setDecimalFormatSymbols(dfs);
System.out.println(df.format(new BigDecimal("1234567890123456")));
// 1234 5678 9012 3456 pour carte de crédit ?

//https://www.codota.com/code/java/classes/java.text.DecimalFormat
 DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
symbols.setGroupingSeparator(' ');//simple space

DecimalFormat df2 = new DecimalFormat("#,###.00", symbols);

BigDecimal example = new BigDecimal("1250");
String str = df2.format(example);

DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
date = dateFormat.parse("23/09/2007");
long time = date.getTime();
System.out.println("new timestamp = " + new Timestamp(time));



List<Pair<String, Integer>> myPairs = new ArrayList<>();
myPairs.add(Pair.of("val1", 11));
myPairs.add(Pair.of("val2", 17));

//...

for(Pair<String, Integer> pair : myPairs) {
  //following two lines are equivalent... whichever is easier for you...
//  LOG.info("pair version L/R : " + pair.getLeft() + ": " + pair.getRight());
  LOG.info("pair version K/V : " + pair.getKey() + ": " + pair.getValue());
}


/*
      Map<String, Integer> members = ImmutableMap.of(
              "Full Member", 2500,
              "Semainier", 2100,
              "Monday", 1400
      );
      for (Map.Entry<String, Integer> entry : members.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue().toString());
}
      members.forEach((key, value) -> System.out.println("members information = " + key + ":" + value));
      
      Map<String, Double> double2 = ImmutableMap.of(
              "Full Member d", 250.0,
              "Semainier d", 2100.0,
              "Monday d", 140.50);
      
        double2.forEach((key, value) -> System.out.println(key + ":" + value));
*/
  } //ed main
} // end class
