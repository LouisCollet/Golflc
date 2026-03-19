package lc.golfnew;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Named;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class TradeOrderView implements Serializable{

  public void placeOrder(ActionEvent ae) {
      //simulating order placement
      LOG.debug("actionevent = " + ae.toString());
      try {
          TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
          LOG.debug("Exception interrupted : " + e);
      }
      LOG.debug("the order is placed");
      // take action here
      // communiquer avec CourseController
   ///   CourseController.setShowButtonCreditCard(true);
      
      
      utils.LCUtil.showMessageInfo("LC : Trade Order Status", "LC : Order has been successfully placed.");
      FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
            "Trade Order Status", "Order has been successfully placed.");
      PrimeFaces.current().dialog().showMessageDynamic(message);
  }
}