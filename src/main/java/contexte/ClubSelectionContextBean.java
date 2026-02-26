
package contexte;

import enumeration.ClubSelectionPurpose;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class ClubSelectionContextBean implements Serializable {

    private ClubSelectionPurpose purpose;

    public void open(ClubSelectionPurpose purpose) {
        this.purpose = purpose;
    }

    public ClubSelectionPurpose getPurpose() {
        return purpose;
    }

    public void clear() {
        purpose = null;
    }
}
