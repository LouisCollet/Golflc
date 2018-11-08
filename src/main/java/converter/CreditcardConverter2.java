
package converter;

import entite.Creditcard;
import static interfaces.Log.LOG;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

// notworking !!
// non utilisé https://github.com/eldermoraes/javaee8-cookbook/blob/master/chapter01/ch01-jsf/src/main/java/com/eldermoraes/ch01/jsf/UserConverter.java
//  <h:inputText id="userNameEmail" value="#{userBean.user}" converter="userConverter" validator="userValidator"/>                         
// https://docs.oracle.com/javaee/6/tutorial/doc/bnaus.html

@FacesConverter("creditcardConverter")
public class CreditcardConverter2 implements Converter<Creditcard> {

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Creditcard creditcard) throws ConverterException{
        LOG.info("entering creditCardconverter asString");
        creditcard.setCreditCardNumber(creditcard.getCreditCardNumber().replaceAll(" ", ""));  // enlève les blancs pour la présentation
        return creditcard.getCreditCardNumber();
    }

   @Override
   public Creditcard getAsObject(FacesContext fc, UIComponent uic, String string) {
      //  sert à quoi ??  return new Player(string.substring(0, string.indexOf("|")), string.substring(string.indexOf("|") + 1));
        return new Creditcard();
    }

    

} // end class
