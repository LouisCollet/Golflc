
package Controller.refact;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.FilterMeta;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;

/**
 * Controller JSF pour les fonctionnalités techniques : filtres, debug, admin tools.
 * Migré progressivement depuis CourseController.
 */
@Named("techC")
@SessionScoped
public class TechnicalController implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<FilterMeta> filterMeta = new ArrayList<>();

    public TechnicalController() { } // constructeur public obligatoire

    // ========================================
    // FILTER META
    // ========================================

    public List<FilterMeta> getFilterMeta() {
        return filterMeta;
    } // end method

    public void setFilterMeta(List<FilterMeta> filterMeta) {
        this.filterMeta = filterMeta;
    } // end method

    // ========================================
    // DEBUG / TEST (2 méthodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public void checkMail(String ini) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("starting checkMail with : " + ini);
            // utils.CheckingMails.main(ini); // argument bidon !!
            LOG.debug("ending checkMail with : " + ini);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void newMessageFatal(String ini) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("starting newMessageFatal with : " + ini);
            LOG.debug(ini);
            LOG.debug("ending newMessageFatal with : " + ini);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

} // end class