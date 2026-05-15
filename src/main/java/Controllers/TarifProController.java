package Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import context.ApplicationContext;
import entite.Professional;
import entite.ProTarif;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("tarifProC")
@ViewScoped
public class TarifProController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private ApplicationContext appContext;
    @Inject private update.UpdateProfessional updateProfessional;
    @Inject private cache.CacheInvalidator cacheInvalidator;

    private Professional professional;
    private ProTarif proTarif = new ProTarif();

    public TarifProController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        professional = appContext.getProfessional();
        if (professional != null) {
            ProTarif existing = professional.getProTarifObject();
            if (existing != null) {
                proTarif = existing;
            }
            LOG.debug("professional={} proTarif={}", professional.getProId(), proTarif);
        } else {
            LOG.warn("no professional in appContext");
        }
    } // end method

    public String onFlowProcess(org.primefaces.event.FlowEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} oldStep={} newStep={}", methodName, event.getOldStep(), event.getNewStep());
        return event.getNewStep();
    } // end method

    public String saveTarif() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (professional == null || professional.getProId() == null) {
                showMessageFatal(utils.LCUtil.prepareMessageBean("professional.not.found"));
                return null;
            }
            if (proTarif.getWeekdayPrice() == null || proTarif.getWeekdayPrice() <= 0) {
                showMessageFatal(utils.LCUtil.prepareMessageBean("professional.tarif.weekday.required"));
                return null;
            }
            if (proTarif.getWeekendPrice() == null || proTarif.getWeekendPrice() <= 0) {
                showMessageFatal(utils.LCUtil.prepareMessageBean("professional.tarif.weekend.required"));
                return null;
            }
            String json = new ObjectMapper().writeValueAsString(proTarif);
            boolean ok = updateProfessional.updateTarif(professional.getProId(), json);
            if (ok) {
                professional.setProTarif(json);
                appContext.setProfessional(professional);
                cacheInvalidator.invalidateProfessionalCaches();
                showMessageInfo(utils.LCUtil.prepareMessageBean("professional.tarif.saved"));
                return "local_administrator_professionals.xhtml?faces-redirect=true";
            } else {
                showMessageFatal(utils.LCUtil.prepareMessageBean("professional.tarif.save.failed"));
                return null;
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public Professional getProfessional() { return professional; }
    public ProTarif    getProTarif()      { return proTarif; }
    public void        setProTarif(ProTarif t) { this.proTarif = t; }

} // end class
