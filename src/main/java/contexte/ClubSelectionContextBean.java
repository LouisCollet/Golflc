
package contexte;

import enumeration.SelectionPurpose;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class ClubSelectionContextBean implements Serializable {

    private SelectionPurpose purpose;

    public void open(SelectionPurpose purpose) {
        this.purpose = purpose;
    }

    public SelectionPurpose getPurpose() {
        return purpose;
    }

    public void clear() {
        purpose = null;
    }
}
