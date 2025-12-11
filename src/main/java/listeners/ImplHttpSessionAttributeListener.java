
package listeners;
import static interfaces.Log.LOG;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;


// enlevé 31-08-2025 @WebListener() // reactivé 26-08-2025 enlevé 22-04-2025 

public class ImplHttpSessionAttributeListener implements HttpSessionAttributeListener {
// https://server2client.com/servletsadv/sessionlisteners.html
    @Override
 public void attributeAdded(HttpSessionBindingEvent event) { 
 try{
   //  LOG.debug("entering attributeAdded with event = " + event);
        LOG.debug("Session attribute added."
                + " Name: "    + event.getName()
              //  + " ,Value: "   + (String)event.getValue()
                + " ,Value: "   + event.getValue()
                + " ,toString: "   + event.toString()
                + " ,Session: " + event.getSession());
            //   + " Source: " + event.getSource());
        LOG.debug("ServletContext attribute added::{" + event.getName()+"," + event.getValue() + "}");
 }catch(Exception e){
   String msg = "£££ Exception attributeAdded = " + e.getMessage();
   LOG.error(msg);
 //   LCUtil.showMessageFatal(msg);
   // return null;
    }
} // end method
    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) { 
      //  Receive notification that a new attribute has been added to a session.
try{
        LOG.debug("Session attribute removed."
                + " Name: "    + event.getName()
             //   + " ,Value: "   + (String)event.getValue()
                + " ,Value: "   + event.getValue()
                + " ,toString: "   + event.toString()
                + " ,Session: " + event.getSession());

  }catch(Exception e){
   String msg = "£££ Exception attributeRemoved = " + e.getMessage() + " for player = "; // + player.idplayer; //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
 //   LCUtil.showMessageFatal(msg);
   // return null;
    }
} // end method
    
    @Override
  public void attributeReplaced(HttpSessionBindingEvent event) { 
    //    Receive notification that an existing attribute has been replaced within a session.
  try{
        LOG.debug("Session attribute replaced."
                + " Name: "    + event.getName()
                + " ,Value: "   + (String)event.getValue()
                + " ,Session: " + event.getSession());
  }catch(Exception e){
   String msg = "£££ Exception attributeReplaced = " + e.getMessage() + " for player = "; // + player.idplayer; //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
 //   LCUtil.showMessageFatal(msg);
   // return null;
    }
  } // end dmethod
} // end class