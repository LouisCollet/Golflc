package test_instruction;
import com.google.common.collect.ImmutableMap;
import static interfaces.Log.LOG;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

public class test_1{

  public static void main(String[] args) throws ParseException {
// using Apache Guava:
//try{

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
        
      JSONObject obj = new JSONObject();
      obj.put("Full member",2500);
      obj.put("Semainier",1000);
      obj.put("Monday",1000.21);
      obj.put("is_vip", true);
      StringWriter out = new StringWriter();
      obj.write(out);
      String jsonText = out.toString();
      System.out.println("json to text = " + jsonText);
        System.out.println("done louis");
        
        

        
      }
        
  } //ed main
//} // end class
