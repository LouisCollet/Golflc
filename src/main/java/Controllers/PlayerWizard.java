
package Controllers;

import static interfaces.Log.LOG;
import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.event.FlowEvent;

@Named("playerWizard")
@ViewScoped
public class PlayerWizard implements Serializable {

      private boolean skip;
      private String step;

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public String onFlowProcess(FlowEvent event) {
        LOG.debug("entering onFlowProcess with oldstep = {}", event.getOldStep());
        LOG.debug("entering onFlowProcess with newstep = {}", event.getNewStep());
        step = event.getNewStep();
        if (skip) {
            skip = false; //reset in case user goes back
            return "confirm";
        }
        else {
            return event.getNewStep();
        }
    }
    // ne fonctionne pas !!
  public void validateStep1(FacesContext context, UIComponent component, Object value) throws ValidatorException  {
         LOG.debug("entering validateStep1  with FacesContext = {}", context.toString());
         LOG.debug("entering validateStep1  with YIComponent = {}", component.toString());
         LOG.debug("entering validateStep1  with Object = {}", value.toString());
         boolean condition = false;
        if (condition) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN, "Validation LC message", "Validation LC summary"));
        }else{
            LOG.debug("entering validateStep1  in else = {}", value.toString());
         //   return true;
     }
    }
} // end Class