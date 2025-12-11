
package converter;

import entite.Player;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;


// non utilisé https://github.com/eldermoraes/javaee8-cookbook/blob/master/chapter01/ch01-jsf/src/main/java/com/eldermoraes/ch01/jsf/UserConverter.java
//  <h:inputText id="userNameEmail" value="#{userBean.user}" converter="userConverter" validator="userValidator"/>                         


@FacesConverter("playerConverter")
public class PlayerConverter implements Converter<Player> {

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Player player) {
        return player.getPlayerLastName()+ "|" + player.getPlayerEmail();
    }

//   @Override
//   public Player getAsObject(FacesContext fc, UIComponent uic, String string) {
//      //  return new Player(string.substring(0, string.indexOf("|")), string.substring(string.indexOf("|") + 1));
//        return new Player("louis");
 //   }

    @Override
    public Player getAsObject(FacesContext fc, UIComponent uic, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
