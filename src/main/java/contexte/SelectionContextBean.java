package contexte;

import enumeration.SelectionPurpose;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * CDI session context for SelectionPurpose.
 * Renamed from ClubSelectionContextBean 2026-03-23 — now handles clubs, courses, rounds, playing hcp.
 */
@Named
@SessionScoped
public class SelectionContextBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private SelectionPurpose purpose;

    public SelectionContextBean() { } // end constructor

    public void open(SelectionPurpose purpose) {
        this.purpose = purpose;
    } // end method

    public SelectionPurpose getPurpose() {
        return purpose;
    } // end method

    public void clear() {
        purpose = null;
    } // end method

} // end class
