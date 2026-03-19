
package lc.golfnew;

import entite.Player;
import static interfaces.Log.LOG;
import java.lang.reflect.Field;

/**
 *
 * @author http://www.javapractices.com/topic/TopicAction.do?Id=55
 *   /**
  * Intended only for debugging.
  *  * <P>Here, a generic implementation uses reflection to print
  * names and values of all fields <em>declared in this class</em>. Note that
  * superclass fields are left out of this implementation.
  *  * <p>The format of the presentation could be standardized by using
  * a MessageFormat object with a standard pattern.
  */

public class AutotoString {
    //
    public AutotoString(){
    // constructor
}
    
 public String auto(Class<Object> cl){ //recoit une classe ex player  implements Serializable,
        LOG.debug("entering auto with Class = " + cl.toString());
        LOG.debug("class is " + cl.getClass().getName());
    StringBuilder result = new StringBuilder();
    String NL = System.getProperty("line.separator");

  //  result.append(this.getClass().getName());
    result.append(cl.getClass().getName());
 //   LOG.debug("class is " + cl.getClass().getName());
    result.append(" Object {");
    result.append(NL);

    //determine fields declared in this class only (no fields of superclass)
    Field[] fields = cl.getClass().getDeclaredFields();

    //print field names paired with their values
    for (Field field : fields) {
      result.append("  ");
      try {
   //       field.setAccessible(true);
        result.append(field.getName());
        result.append(": ");
        //requires access to private field:
        result.append(field.get(this));
      }catch (IllegalAccessException ex){
        LOG.debug(ex.toString());
      }
      result.append(NL);
    }
    result.append("}");

    return result.toString();
  } //end method

  /* PRIVATE 
  private String name;
  private Integer numDoors;
  private LocalDate whenManufactured;
  private String color;
  private List<String> options;
  
  private static void log(Object thing) {
    LOG.debug(thing.toString());
  }
} 
    */
    
 // end method
 
  void main() throws Exception{
  //   Connection conn = new DBConnection().getConnection();
  try{
     Player player = new Player();
     player.setIdplayer(324713);
   player.getClass();
///?        String s = new AutotoString().auto(player.getClass());
   ///   LOG.debug("from main, ec = " + s);
 }catch (Exception e){
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
   //      DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main
 
} // end class