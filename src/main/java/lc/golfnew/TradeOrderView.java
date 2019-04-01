
package lc.golfnew;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import org.omnifaces.cdi.ViewScoped;


@Named
@ViewScoped
public class TradeOrderView implements Serializable{

  public void placeOrder(ActionEvent ae) {
      //simulating order placement
      LOG.info("actionevent = " + ae.toString());
      try {
          TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
          LOG.info("Exception interrupted");
      }
      LOG.info("the order is placed");
      // take action here
      utils.LCUtil.showMessageFatal2("Trade Order Status", "Order has been successfully placed.");
   //   FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
   //           "Trade Order Status", "Order has been successfully placed.");
   //   PrimeFaces.current().dialog().showMessageDynamic(message);
  }
}