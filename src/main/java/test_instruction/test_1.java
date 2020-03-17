package test_instruction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import static interfaces.Log.LOG;
import java.io.StringWriter;
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
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

public class test_1{

  public static void main(String[] args) throws ParseException, JsonProcessingException {
// using Apache Guava:
//try{

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

        ObjectMapper om = new ObjectMapper();
        String jsonString = om.writeValueAsString(arrlist);
        LOG.info("jsonString = " + jsonString);
             
      JSONObject obj = new JSONObject();
      obj.put("Full member",2500);
      obj.put("Semainier",1000);
      obj.put("Monday",1000.21);
      obj.put("is_vip", true);
      StringWriter out = new StringWriter();
      obj.write(out);
      String jsonText = out.toString();
      System.out.println("json to text = " + jsonText);
  //      System.out.println("done louis");         
              
              
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
        
        

        
      }
        
  } //ed main
//} // end class
