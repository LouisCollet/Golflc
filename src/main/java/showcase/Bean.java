package showcase;

import static interfaces.Log.LOG;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;

@Named("bean")
@RequestScoped
public class Bean {
    private String text;

    public List<String> complete(String query) {
         LOG.debug("entering complete with query = " + query);
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            results.add(query + i);
        LOG.debug("returning list String results = " + results.toString());
        return results;
    }
public void handleSelect(SelectEvent<Object> event) {
      //  Object item = event.getObject();
      //  FacesMessage msg = new FacesMessage("Selected", "Item:" + item);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}