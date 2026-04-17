package Controllers;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DialogFrameworkOptions;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("dialogC")
@ApplicationScoped
public class DialogController implements Serializable {

    /* ==========================================================
       INTERNAL HELPERS
       ========================================================== */

    private DialogFrameworkOptions modal(
            String width,
            String height,
            boolean draggable,
            boolean resizable) {
        return modal(width, height, draggable, resizable, null);
    }

    private DialogFrameworkOptions modal(
            String width,
            String height,
            boolean draggable,
            boolean resizable,
            String styleClass) {

        var builder = DialogFrameworkOptions.builder()
                .modal(true)
                .draggable(draggable)
                .resizable(resizable)
                .width(width)
                .height(height)
                .contentWidth("100%")
                .contentHeight("100%")
                .closeOnEscape(true)
                .includeViewParams(true);
        if (styleClass != null) {
            builder.styleClass(styleClass);
        }
        return builder.build();
    }

    private Map<String, List<String>> singleParam(String key, String value) {
        return Map.of(key, List.of(value));
    }

    /* ==========================================================
       OPEN DIALOGS
       ========================================================== */

    public void showUnavailable() {
        LOG.debug("showUnavailable");
        PrimeFaces.current().dialog()
                .openDynamic("dialogUnavailable.xhtml",
                        modal("500", "650", false, true, "dlg-action"),
                        null);
    }

    public void showHandicapIndex() {
        LOG.debug("showHandicapIndex");
        PrimeFaces.current().dialog()
                .openDynamic("dialog_handicap_index.xhtml",
                        modal("50%", "60%", false, false, "dlg-info"),
                        null);
    }

    public void showFlight() {
        LOG.debug("showFlight");
        PrimeFaces.current().dialog()
                .openDynamic("dialogFlight.xhtml",
                        modal("40%", "70%", false, true, "dlg-action"),
                        null);
    }

    public void showClubDetail(entite.Club club) {
        Objects.requireNonNull(club, "Club must not be null");

        PrimeFaces.current().dialog()
                .openDynamic("dialogClubDetail.xhtml",
                    modal("900", "800", false, true, "dlg-info"),
                    singleParam("IdClub", String.valueOf(club.getIdclub())));
    }

    public void showSelectClub(String purpose) {
        LOG.debug("entering showSelectClub purpose={}", purpose);
    /*        LOG.debug("=== BEFORE showSelectClub ===");
        FacesContext ctx = FacesContext.getCurrentInstance();
    
        // Afficher les messages actuels
        List<FacesMessage> messages = ctx.getMessageList();
            LOG.debug("Messages count: {}", messages.size());
        for (FacesMessage msg : messages) {
            LOG.debug("Message: {}", msg.getSummary());
        }
        // Effacer les messages
        ctx.getMessageList().clear();
        LOG.debug("Messages cleared");
        
 //       FacesContext.getCurrentInstance().getMessageList().clear(); // new 13-02-2026
 //       LOG.debug("messages cleared");
        */
        PrimeFaces.current().dialog()
                .openDynamic("dialogClub.xhtml",
                        modal("50%", "70%", false, false, "dlg-select"),
                        singleParam("type_club", purpose));
    }
 // new 02-02-2026
    public void showSelectCourse(String from, String clubId) {
    LOG.debug("entering DialogController showSelectCourse");
    LOG.debug("from={}, clubId={}", from, clubId);

    if (clubId == null || clubId.isBlank()) {
        showMessageFatal("Please first select a club!");
        return;
    }

    PrimeFaces.current().dialog()
            .openDynamic(
                    "dialogCourse.xhtml",
                    modal("50%", "70%", true, true, "dlg-select"),
                    Map.of(
                        "clubId", List.of(clubId),
                        "from",   List.of(from)
                    )
            );
}

    public void showSelectCourse(String clubId) throws IOException {
        if (clubId == null) {
            showMessageFatal("Please first select a club!");
            return;
        }

        PrimeFaces.current().dialog()
                .openDynamic("dialogCourse.xhtml",
                        modal("50%", "70%", true, true, "dlg-select"),
                        singleParam("clubId", clubId));
    }

    public void showSelectPlayer(String param) {
        LOG.debug("entering showSelectPlayer with param = {}", param);
        PrimeFaces.current().dialog()
                .openDynamic("dialogPlayer.xhtml",
                        modal("900", "600", true, true, "dlg-select"),
                        singleParam("param_player", param));
    }

    public void showWeather() {
        PrimeFaces.current().dialog()
                .openDynamic("dialogWeather.xhtml",
                        modal("900", "600", true, true, "dlg-action"),
                        null);
    }

    public void showSelectRound(String purpose) {
        PrimeFaces.current().dialog()
                .openDynamic("dialogRound.xhtml",
                        modal("50%", "70%", false, false, "dlg-select"),
                        singleParam("type_club", purpose));
    }

    public void showRound() {
        PrimeFaces.current().dialog()
                .openDynamic("dialog_played_rounds.xhtml",
                        modal("840", "640", false, true, "dlg-info"),
                        null);
    }

    public void showLessonDialog() {
        LOG.debug("showLessonDialog");
        PrimeFaces.current().executeScript("PF('eventDialog').show();");
    }

    public void hideLessonDialog() {
        LOG.debug("hideLessonDialog");
        PrimeFaces.current().executeScript("PF('eventDialog').hide(); PF('myschedule').update();");
    }

    /* ==========================================================
       DIALOG RETURNS
       ========================================================== */

    public void closeDialog(Object result) {
      //  LOG.debug("closeDialog result={}", result);
        PrimeFaces.current().dialog().closeDynamic(result);
    }

    public void onDialogReturn(SelectEvent<?> event) {
        Object obj = event.getObject();
        LOG.debug("Dialog returned: {}", obj);
        showMessageInfo("Dialog closed successfully");
    }

    public void onFlightChosen(SelectEvent<entite.Flight> event) {
        entite.Flight flight = event.getObject();
        LOG.debug("Flight selected: {}", flight);
        PrimeFaces.current().ajax().update("form_round:idworkhour");
    }
}
