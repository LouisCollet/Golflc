
package entite.utilities;
import static interfaces.Log.LOG;
import org.primefaces.event.SelectEvent;
// non utilisé
public class Handicap {
       // public void onRowSelect(SelectEvent event) {
    public void onrowSelect(SelectEvent<Object> event) {
        LOG.debug("onrowSelect Event fired !");
    }
    public void onrowUnselect(SelectEvent<Object> event) {
        LOG.debug("onrowUnselect Event fired !");
    }
    
    public void onrowSelectCheckbox(SelectEvent<Object> event) {
        LOG.debug("onrowSelectCheckbox Event fired !");
    }
    
    public void onrowUnselectCheckbox(SelectEvent<Object> event) {
        LOG.debug("onrowSelectCheckbox Event fired !" + event.getObject().toString());
    }
    
}
