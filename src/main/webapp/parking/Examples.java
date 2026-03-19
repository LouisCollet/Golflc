
package dialog;

/**
 *
 * @author Louis Collet
 */
public class Examples {
  /*  <!-- ============================================ -->
<!-- Exemples d'intégration avec les vues XHTML -->
<!-- ============================================ -->

<!-- ===== Exemple 1 : Ouverture simple d'un dialogue ===== -->

<!-- AVANT (mauvaise pratique - appel statique) -->
<p:commandButton value="Show Unavailable" 
                 onclick="Controllers.DialogController.showUnavailable();"/>

<!-- APRÈS (bonne pratique - injection CDI) -->
<p:commandButton value="Show Unavailable" 
                 action="#{dialogC.showUnavailable()}"
                 update="@form"
                 process="@this"/>


<!-- ===== Exemple 2 : Dialogue avec paramètre simple ===== -->

<!-- AVANT -->
<p:commandButton value="Select Club" 
                 onclick="Controllers.DialogController.showSelectClub('#{roundController.clubType}');"/>

<!-- APRÈS -->
<p:commandButton value="Select Club" 
                 action="#{dialogC.showSelectClub(roundController.clubType)}"
                 update="@form"
                 process="@this">
    <p:ajax event="dialogReturn" 
            listener="#{roundController.onClubSelected}" 
            update="clubPanel"/>
</p:commandButton>


<!-- ===== Exemple 3 : Dialogue avec objet complexe ===== -->

<!-- AVANT -->
<p:commandButton value="Club Details" 
                 onclick="#{dialogC.showClubDetail(club)}"/>

<!-- APRÈS -->
<p:dataTable var="club" value="#{clubController.clubs}">
    <p:column headerText="Actions">
        <p:commandButton value="Details" 
                         action="#{dialogC.showClubDetail(club)}"
                         update="@form"
                         process="@this">
            <p:ajax event="dialogReturn" 
                    listener="#{clubController.onDetailsViewed}" 
                    update="clubForm"/>
        </p:commandButton>
    </p:column>
</p:dataTable>


<!-- ===== Exemple 4 : Dialogue avec gestion de retour ===== -->

<h:form id="courseForm">
    <p:panel header="Course Selection">
        <p:outputLabel value="Selected Course: "/>
        <p:outputText value="#{courseController.selectedCourse.courseName}"/>
        
        <p:commandButton value="Select Course" 
                         action="#{dialogC.showSelectCourse('main', courseController.clubId)}"
                         update="@form"
                         process="@this">
            <!-- Gestion du retour du dialogue -->
            <p:ajax event="dialogReturn" 
                    listener="#{courseController.onCourseSelected}" 
                    update="courseForm"/>
        </p:commandButton>
    </p:panel>
</h:form>


<!-- ===== Exemple 5 : Dialogue dans une DataTable ===== -->

<h:form id="playerForm">
    <p:dataTable var="player" 
                 value="#{playerController.players}"
                 id="playerTable">
        <p:column headerText="Name">
            <h:outputText value="#{player.playerLastName}"/>
        </p:column>
        
        <p:column headerText="Actions">
            <!-- Bouton pour ouvrir le dialogue de détails -->
            <p:commandButton value="View" 
                             icon="pi pi-eye"
                             action="#{dialogC.showSelectPlayer(player.idplayer)}"
                             update="playerForm"
                             process="@this"/>
        </p:column>
    </p:dataTable>
</h:form>


<!-- ===== Exemple 6 : Dialogue conditionnel ===== -->

<p:commandButton value="Select Course" 
                 action="#{dialogC.showSelectCourse('main', roundController.clubId)}"
                 disabled="#{empty roundController.clubId}"
                 update="@form"
                 process="@this">
    <p:ajax event="dialogReturn" 
            listener="#{roundController.onCourseSelected}" 
            update="coursePanel"/>
</p:commandButton>

<!-- Message si le club n'est pas sélectionné -->
<p:message for="selectCourseBtn" 
           rendered="#{empty roundController.clubId}">
    <f:facet name="summary">Please select a club first</f:facet>
</p:message>


<!-- ===== Exemple 7 : Multiple dialogues avec callbacks différents ===== -->

<h:form id="competitionForm">
    <!-- Sélection du club -->
    <p:commandButton value="Select Club" 
                     action="#{dialogC.showSelectClub('competition')}"
                     update="@form"
                     process="@this">
        <p:ajax event="dialogReturn" 
                listener="#{competitionController.onClubSelected}" 
                update="clubPanel coursePanel"/>
    </p:commandButton>
    
    <!-- Sélection du parcours (activé après sélection du club) -->
    <p:commandButton value="Select Course" 
                     action="#{dialogC.showSelectCourse('competition', competitionController.selectedClubId)}"
                     disabled="#{empty competitionController.selectedClubId}"
                     update="@form"
                     process="@this">
        <p:ajax event="dialogReturn" 
                listener="#{competitionController.onCourseSelected}" 
                update="coursePanel playersPanel"/>
    </p:commandButton>
    
    <!-- Sélection des joueurs (activé après sélection du parcours) -->
    <p:commandButton value="Add Players" 
                     action="#{dialogC.showSelectPlayer('competition')}"
                     disabled="#{empty competitionController.selectedCourseId}"
                     update="@form"
                     process="@this">
        <p:ajax event="dialogReturn" 
                listener="#{competitionController.onPlayersSelected}" 
                update="playersPanel"/>
    </p:commandButton>
</h:form>


<!-- ===== Exemple 8 : Dialogue avec confirmation ===== -->

<p:commandButton value="Delete Round" 
                 icon="pi pi-trash"
                 styleClass="ui-button-danger">
    <p:confirm header="Confirmation" 
               message="Are you sure you want to delete this round?"
               icon="pi pi-exclamation-triangle"/>
</p:commandButton>

<p:confirmDialog global="true" showEffect="fade" hideEffect="fade">
    <p:commandButton value="Yes" 
                     type="button" 
                     styleClass="ui-confirmdialog-yes" 
                     icon="pi pi-check"
                     action="#{roundController.deleteRound()}"/>
    <p:commandButton value="No" 
                     type="button" 
                     styleClass="ui-confirmdialog-no" 
                     icon="pi pi-times"/>
</p:confirmDialog>


<!-- ===== Exemple 9 : Dialogue avec mise à jour ciblée ===== -->

<h:form id="roundForm">
    <p:panel id="flightPanel" header="Flight Information">
        <p:outputLabel value="Selected Flight: "/>
        <p:outputText value="#{roundController.selectedFlight.flightName}"/>
        
        <p:commandButton value="Change Flight" 
                         action="#{dialogC.showFlight()}"
                         update="roundForm"
                         process="@this">
            <!-- Mise à jour uniquement du panel flight -->
            <p:ajax event="dialogReturn" 
                    listener="#{roundController.onFlightChosen}" 
                    update="flightPanel teeTimePanel"/>
        </p:commandButton>
    </p:panel>
    
    <p:panel id="teeTimePanel" header="Tee Time">
        <p:outputText value="#{roundController.teeTime}"/>
    </p:panel>
</h:form>


<!-- ===== Exemple 10 : Dialogue avec validation avant ouverture ===== -->

<h:form id="validatedForm">
    <p:inputText id="clubId" 
                 value="#{dialogController.clubId}" 
                 required="true"
                 requiredMessage="Please enter a club ID"/>
    
    <p:commandButton value="Open Course Selection" 
                     action="#{dialogC.showSelectCourse('main', dialogController.clubId)}"
                     validateClient="true"
                      
                     update="validatedForm"
                     process="@form">
        <p:ajax event="dialogReturn" 
                listener="#{dialogController.onCourseSelected}" 
                update="coursePanel"/>
    </p:commandButton>
    
    <p:messages id="messages" showDetail="true"/>
</h:form>


<!-- ===== Exemple 11 : Template de page dialogue ===== -->

<!-- dialogClub.xhtml - Exemple de structure d'un dialogue -->
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:p="http://primefaces.org/ui"
      xmlns:f="http://xmlns.jcp.org/jsf/core">

<h:head>
    <title>Select Club</title>
</h:head>

<h:body>
    <h:form id="clubDialogForm">
        <p:panel header="Club Selection">
            <p:dataTable var="club" 
                         value="#{clubDialogController.clubs}"
                         selectionMode="single"
                         selection="#{clubDialogController.selectedClub}"
                         rowKey="#{club.idclub}">
                
                <p:column headerText="Name">
                    <h:outputText value="#{club.clubName}"/>
                </p:column>
                
                <p:column headerText="City">
                    <h:outputText value="#{club.address.city}"/>
                </p:column>
            </p:dataTable>
            
            <f:facet name="footer">
                <p:commandButton value="Select" 
                                 icon="pi pi-check"
                                 action="#{clubDialogController.selectClub()}"
                                 process="@form"
                                 update="@form"/>
                
                <p:commandButton value="Cancel" 
                                 icon="pi pi-times"
                                 action="#{dialogC.closeDialog(null)}"
                                 immediate="true"
                                 process="@this"/>
            </f:facet>
        </p:panel>
    </h:form>
</h:body>
</html>


<!-- ===== Exemple 12 : Contrôleur du dialogue (Managed Bean) ===== -->

<!--
package Controllers;

import entite.Club;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("clubDialogController")
@RequestScoped
public class ClubDialogController implements Serializable {
    
    @Inject
    private DialogController dialogController;
    
    @Inject
    private dao.ClubDAO clubDAO;
    
    private List<Club> clubs;
    private Club selectedClub;
    
    // Méthode d'initialisation
    @PostConstruct
    public void init() {
        loadClubs();
    }
    
    private void loadClubs() {
        try {
            clubs = clubDAO.findAll();
        } catch (Exception e) {
            LOG.error("Error loading clubs", e);
            utils.LCUtil.showMessageError("Unable to load clubs");
        }
    }
    
    public void selectClub() {
        if (selectedClub == null) {
            utils.LCUtil.showMessageWarn("Please select a club");
            return;
        }
        
        // Créer un résultat typé
        DialogResult<Club> result = DialogResult.success(
            selectedClub, 
            "Club selected: " + selectedClub.getClubName()
        );
        
        // Fermer le dialogue avec le résultat
        dialogController.closeDialog(result);
    }
    
    // Getters/Setters
    public List<Club> getClubs() {
        return clubs;
    }
    
    public Club getSelectedClub() {
        return selectedClub;
    }
    
    public void setSelectedClub(Club selectedClub) {
        this.selectedClub = selectedClub;
    }
}
-->


<!-- ===== Exemple 13 : Gestion du retour avec type fort ===== -->

<!--
// Dans le contrôleur parent qui ouvre le dialogue

@Named("roundController")
@ViewScoped
public class RoundController implements Serializable {
    
    private Club selectedClub;
    
    public void onClubSelected(SelectEvent<DialogResult<Club>> event) {
        DialogResult<Club> result = event.getObject();
        
        if (result.success()) {
            selectedClub = result.data();
            
            LOG.info("Club selected: {}", selectedClub.getClubName());
            utils.LCUtil.showMessageInfo(result.message());
            
            // Déclencher d'autres actions
            loadCoursesForClub(selectedClub);
        } else {
            LOG.warn("Club selection failed: {}", result.message());
            utils.LCUtil.showMessageWarn(result.message());
        }
    }
    
    private void loadCoursesForClub(Club club) {
        // Charger les parcours du club sélectionné
    }
    
    // Getters
    public Club getSelectedClub() {
        return selectedClub;
    }
}
-->


<!-- ===== Exemple 14 : Dialogue avec plusieurs actions ===== -->

<h:form id="actionForm">
    <p:toolbar>
        <p:toolbarGroup align="left">
            <p:commandButton value="New Round" 
                             icon="pi pi-plus"
                             action="#{dialogC.showSelectRound('new')}"
                             update="@form"/>
            
            <p:commandButton value="View History" 
                             icon="pi pi-list"
                             action="#{dialogC.showPlayedRounds()}"
                             update="@form"/>
            
            <p:commandButton value="Weather" 
                             icon="pi pi-cloud"
                             action="#{dialogC.showWeather()}"
                             update="@form"/>
        </p:toolbarGroup>
        
        <p:toolbarGroup align="right">
            <p:commandButton value="Help" 
                             icon="pi pi-question-circle"
                             action="#{dialogC.showHandicapIndex()}"
                             update="@form"/>
        </p:toolbarGroup>
    </p:toolbar>
</h:form>


<!-- ===== Exemple 15 : Dialogue responsive ===== -->

<h:form id="responsiveForm">
    <!-- Dialogue adapté aux mobiles -->
    <p:commandButton value="Select Player" 
                     action="#{dialogC.showSelectPlayer('mobile')}"
                     styleClass="ui-button-raised"
                     update="@form">
        <p:ajax event="dialogReturn" 
                listener="#{playerController.onPlayerSelected}" 
                update="playerList"/>
    </p:commandButton>
    
    <p:dataList id="playerList" 
                value="#{playerController.selectedPlayers}" 
                var="player"
                type="ordered"
                styleClass="ui-responsive">
        <h:outputText value="#{player.playerFullName}"/>
    </p:dataList>
</h:form>

<!-- Configuration CSS pour responsive -->
<style>
    @media (max-width: 768px) {
        .ui-dialog {
            width: 95% !important;
            height: 90% !important;
        }
    }
</style>
*/
}
