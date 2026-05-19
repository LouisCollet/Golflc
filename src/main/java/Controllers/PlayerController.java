package Controllers;

import context.ApplicationContext;
import entite.Activation;
import entite.Audit;
import dialog.DialogResult;
import entite.Blocking;
import entite.Club;
import entite.HandicapIndex;
import entite.LoggingUser;
import entite.Password;
import entite.Player;
import entite.Professional;
import entite.composite.ECourseList;
import entite.composite.EPlayerPassword;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import manager.PlayerManager;
import manager.PlayerManager.SaveResult;
import org.primefaces.event.SelectEvent;
import service.CoordinatesService;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("playerC")
@SessionScoped
public class PlayerController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private PlayerManager      playerManager;
    @Inject private CoordinatesService coordinatesService;
    @Inject private ApplicationContext appContext;
    @Inject private cache.CacheInvalidator cacheInvalidator;
    @Inject private find.FindLastLogin findLastLoginService;
    @Inject private Controllers.LanguageController languageController;
    @Inject private lists.PlayersList playersListService;

    @Inject private Controllers.PasswordController passwordController;
    @Inject private update.UpdatePlayer updatePlayer;
    @Inject private update.UpdateClub updateClub;
    @Inject private lists.HandicapList handicapList;
    @Inject private read.LoadBlocking loadBlocking;
    @Inject private create.CreateBlocking createBlocking;
    @Inject private update.UpdateBlocking updateBlocking;
    @Inject private update.UpdatePassword updatePassword;
    @Inject private find.FindPassword findPassword;

    private EPlayerPassword selectedPlayerEPP = null;
    private Blocking blocking;
    private boolean nextPanelPlayer = false;
    private boolean showPlayerList = false;
    private String createModifyPlayer = "C"; // C=Create, M=Modify
    private int deletePlayer = 0;
    private Password password = new Password();
    private List<ECourseList> filteredPlayedRound = null;

    // Session-level cache — avoid repeated DB queries on JSF re-render
    private List<ECourseList>    cachedHandicapWHS    = null;
    private List<ECourseList>    cachedPlayedRounds   = null;
    private List<EPlayerPassword> cachedPlayers       = null;
    private List<Professional>   cachedProfessionals  = null;

    @Inject private Controllers.MongoCalculationsController mongoCalculationsController;
    @Inject private read.ReadActivation readActivation;
    @Inject private create.CreateActivationPassword createActivationPassword;
    @Inject private Controllers.ActivationController activationController;
    @Inject private read.ReadClub readClubService;
    @Inject private lists.RoundPlayersList roundPlayersListService;

    // ========================================
    // DÉLÉGATION — Player (pour les vues)
    // ========================================

    public Player getPlayer()              { return appContext.getPlayer(); }
    public void   setPlayer(Player player) { appContext.setPlayer(player); }

    public Integer getCurrentPlayerAge() {
        Player p = appContext.getPlayer();
        if (p == null || p.getPlayerBirthDate() == null) return null;
        return calculateAgeFirstJanuary(p.getPlayerBirthDate());
    } // end method

    public int calculateAgeFirstJanuary(LocalDateTime birthDate) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (birthDate == null) return 99;
        LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
        return (int) ChronoUnit.YEARS.between(birthDate.toLocalDate(), firstDayOfYear);
    } // end method

    public void playerLanguageListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String newLanguage = e.getNewValue().toString();
        LOG.debug("playerLanguage newValue={}", newLanguage);
        appContext.getPlayer().setPlayerLanguage(newLanguage);
        languageController.setLanguage(newLanguage);
    } // end method

    public Player getPlayerPro()              { return appContext.getPlayerPro(); }
    public void   setPlayerPro(Player player) { appContext.setPlayerPro(player); }

    public Player getLocalAdmin()              { return appContext.getLocalAdmin(); }
    public void   setLocalAdmin(Player player) { appContext.setLocalAdmin(player); }

    public Player getPlayerTemp()              { return appContext.getPlayerTemp(); }
    public void   setPlayerTemp(Player player) { appContext.setPlayerTemp(player); }

    private List<ECourseList> filteredHandicaps = null;

    private Activation activation = new Activation();
    private String uuid;

    // ========================================
    // CDI EVENT — ResetEvent observer
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} source={}", methodName, event.getSource());
        selectedPlayerEPP    = null;
        blocking             = null;
        nextPanelPlayer      = false;
        createModifyPlayer   = "C";
        deletePlayer         = 0;
        password             = new Password();
        filteredPlayedRound  = null;
        filteredHandicaps    = null;
        cachedHandicapWHS    = null;
        cachedPlayedRounds   = null;
        cachedPlayers        = null;
        LOG.debug("PlayerController reset done");
    } // end method

    // ========================================
    // DÉLÉGATION — HandicapIndex (pour les vues)
    // ========================================

    public HandicapIndex getHandicapIndex() { return appContext.getHandicapIndex(); }
    public void setHandicapIndex(HandicapIndex handicapIndex) { appContext.setHandicapIndex(handicapIndex); }

    // ========================================
    // MÉTHODES HELPER
    // ========================================

    public boolean hasPlayer() {
        Player p = appContext.getPlayer();
        return p != null && p.getIdplayer() != null;
    } // end method

    public boolean isAdmin() {
        Player p = appContext.getPlayer();
        return p != null && "ADMIN".equals(p.getPlayerRole());
    } // end method

    public boolean isLocalAdmin() {
        Player p = appContext.getPlayer();
        return p != null && "admin".equalsIgnoreCase(p.getPlayerRole());
    } // end method

    public boolean isRegularPlayer() {
        Player p = appContext.getPlayer();
        return p != null && "PLAYER".equalsIgnoreCase(p.getPlayerRole());
    } // end method

    public boolean isProfessional() { return !getProfessionals().isEmpty(); }
    public int getProfessionalCount() { return getProfessionals().size(); }

    public boolean canShowMenu() {
        Player p = appContext.getPlayer();
        return p != null && p.isShowMenu();
    } // end method

    // ========================================
    // ACTIONS JSF
    // ========================================

    public void createPlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Integer clubId = appContext.getClub().getIdclub();

            if (clubId == null || clubId == 0) {
                showMessageFatal("Please select a club first");
                appContext.setNextPlayer(false);
                return;
            }

            Player player = getPlayer();
            player.setPlayerHomeClub(clubId);
            LOG.debug("player before creation={}", player);

            SaveResult result = playerManager.createPlayer(player, appContext.getHandicapIndex());

            if (result.isSuccess()) {
                LOG.debug("player created successfully");
                appContext.setPlayer(result.getPlayer());
                appContext.setNextPlayer(true);
            } else {
                LOG.error("player creation failed");
                showMessageFatal(result.getMessage());
                appContext.setNextPlayer(false);
            }

        } catch (Exception e) {
            handleGenericException(e, methodName);
            appContext.setNextPlayer(false);
        }
    } // end method

    public void updatePlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            SaveResult result = playerManager.modifyPlayer(appContext.getPlayer());
            if (result.isSuccess()) {
                LOG.debug("{}", result.getMessage());
            } else {
                LOG.debug("{}", result.getMessage());
                showMessageFatal(result.getMessage());
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void updateCoordinates() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            coordinatesService.updateCoordinates(appContext.getPlayer());
            showMessageInfo("Coordinates updated");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // LISTES
    // ========================================

    public List<EPlayerPassword> listPlayers() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (cachedPlayers != null) {
            LOG.debug("returning cached list size={}", cachedPlayers.size());
            return cachedPlayers;
        }
        try {
            cachedPlayers = playerManager.listPlayers();
            return cachedPlayers;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<Professional> getProfessionals() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (cachedProfessionals != null) {
            LOG.debug("returning cached list size={}", cachedProfessionals.size());
            return cachedProfessionals;
        }
        try {
            cachedProfessionals = playerManager.findProfessionals(appContext.getPlayer());
            return cachedProfessionals;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listHandicapWHS() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (cachedHandicapWHS != null) {
            LOG.debug("returning cached list size={}", cachedHandicapWHS.size());
            return cachedHandicapWHS;
        }
        try {
            cachedHandicapWHS = playerManager.listHandicapWHS(appContext.getPlayer());
            return cachedHandicapWHS;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listPlayedRound() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (cachedPlayedRounds != null) {
            LOG.debug("returning cached list size={}", cachedPlayedRounds.size());
            appContext.setPlayedRounds(cachedPlayedRounds);
            return cachedPlayedRounds;
        }
        try {
            cachedPlayedRounds = playerManager.listPlayedRounds(appContext.getPlayer());
            appContext.setPlayedRounds(cachedPlayedRounds);
            return cachedPlayedRounds;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listPlayedRounds() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (cachedPlayedRounds != null) {
            LOG.debug("returning cached list size={}", cachedPlayedRounds.size());
            appContext.setPlayedRounds(cachedPlayedRounds);
            return cachedPlayedRounds;
        }
        try {
            cachedPlayedRounds = playerManager.listPlayedRounds(appContext.getPlayer());
            appContext.setPlayedRounds(cachedPlayedRounds);
            return cachedPlayedRounds;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public void invalidatePlayerCaches() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.cachedHandicapWHS   = null;
        this.cachedPlayedRounds  = null;
        this.cachedPlayers       = null;
        this.cachedProfessionals = null;
        LOG.debug("player session caches invalidated");
    } // end method

    public void textCalculationIndex() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ECourseList selected = appContext.getSelectedHandicap();
            if (selected == null) {
                LOG.warn("selectedHandicap is null — action skipped");
                return;
            }
            LOG.debug("handicapIndex={}", appContext.getHandicapIndex());
            LOG.debug("roundId={}", selected.round().getIdround());
            LOG.debug("playerId={}", appContext.getPlayer().getIdplayer());

            LoggingUser logging = new LoggingUser();
            logging.setLoggingIdPlayer(appContext.getPlayer().getIdplayer());
            logging.setLoggingIdRound(selected.round().getIdround());
            logging.setLoggingType("H");

            appContext.getHandicapIndex().setCalculations(mongoCalculationsController.read(logging));
            LOG.debug("calculations loaded");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public ECourseList getSelectedHandicap() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return appContext.getSelectedHandicap();
    } // end method

    public void setSelectedHandicap(ECourseList selectedHandicap) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        appContext.setSelectedHandicap(selectedHandicap);
        LOG.debug("selectedHandicap={}", selectedHandicap);
    } // end method

    public ECourseList getSelectedPlayedRound() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return appContext.getSelectedPlayedRound();
    } // end method

    public void setSelectedPlayedRound(ECourseList selectedPlayedRound) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        appContext.setSelectedPlayedRound(selectedPlayedRound);
        LOG.debug("selectedPlayedRound={}", selectedPlayedRound);
    } // end method

    public List<ECourseList> getFilteredHandicaps() { return filteredHandicaps; }
    public void setFilteredHandicaps(List<ECourseList> filteredHandicaps) { this.filteredHandicaps = filteredHandicaps; }

    // ========================================
    // SELECTION — dataTable
    // ========================================

    public EPlayerPassword getSelectedPlayerEPP() { return selectedPlayerEPP; }

    public void setSelectedPlayerEPP(EPlayerPassword selectedPlayerEPP) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.selectedPlayerEPP = selectedPlayerEPP;
        if (selectedPlayerEPP != null) {
            appContext.setPlayer(selectedPlayerEPP.getPlayer());
            LOG.debug("selectedPlayerEPP={}", selectedPlayerEPP);
        }
    } // end method

    public List<ECourseList> getFilteredPlayedRound() { return filteredPlayedRound; }
    public void setFilteredPlayedRound(List<ECourseList> filteredPlayedRound) { this.filteredPlayedRound = filteredPlayedRound; }

    // ========================================
    // CHARGEMENT
    // ========================================

    public void loadPlayer(int idplayer) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (idplayer <= 0) {
                LOG.warn("invalid player ID={}", idplayer);
                showMessageFatal("Invalid player ID: " + idplayer);
                return;
            }
            Player player = playerManager.readPlayer(idplayer);
            appContext.setPlayer(player);
            LOG.debug("player loaded={}", player.getPlayerLastName());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void loadPlayerWithPassword(int idplayer) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (idplayer <= 0) {
                LOG.warn("invalid player ID={}", idplayer);
                showMessageFatal("Invalid player ID: " + idplayer);
                return;
            }
            EPlayerPassword epp = playerManager.readPlayerWithPassword(idplayer);
            appContext.setPlayer(epp.player());
            LOG.debug("player with password loaded");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void resetPlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        appContext.setPlayer(new Player());
        LOG.debug("player reset in context");
    } // end method

    // ========================================
    // MÉTHODES SIMPLES
    // ========================================

    public void validatePlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        setNextPanelPlayer(true);
    } // end method

    public void to_player_modify(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} s={}", methodName, s);
        createModifyPlayer = s;
    } // end method

    public String lastSession(int idplayer) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} idplayer={}", methodName, idplayer);
        try {
            Audit audit = findLastLoginService.find(appContext.getPlayer());
            if (audit != null && audit.getAuditStartDate() != null) {
                LOG.debug("date last connection={}", audit.getAuditStartDate().format(ZDF_TIME_HHmm));
                return audit.getAuditStartDate().format(ZDF_TIME_HHmm);
            } else {
                return "First Connection for this player";
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "Error loading last session";
        }
    } // end method

    public void deleteCascadingPlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.warn("not yet implemented");
            showMessageFatal("Delete cascading player not yet implemented");
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    public void onPlayerIdChanged() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} localAdmin={}", methodName, appContext.getLocalAdmin());
        if (appContext.getLocalAdmin().getIdplayer() == null || appContext.getLocalAdmin().getIdplayer() <= 0) {
            LOG.debug("localAdmin null or 0");
            return;
        }
        try {
            Player loadedPlayer = playerManager.readPlayer(appContext.getLocalAdmin().getIdplayer());
            LOG.debug("loadedPlayer={}", loadedPlayer);
            if (loadedPlayer != null) {
                appContext.setLocalAdmin(loadedPlayer);
                showMessageInfo("Player loaded: " + appContext.getLocalAdmin().getPlayerLastName());
            } else {
                showMessageFatal("Player not found");
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // max 4 players (or 2 for MP_SINGLE) minus already inscribed
    public void rowPlayerSelect(SelectEvent<Object> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            entite.Round round = appContext.getRound();
            int max = (round.getRoundGame() != null
                    && round.getRoundGame().equals(entite.Round.GameType.MP_SINGLE.toString())) ? 2 : 4;
            int inscribed = roundPlayersListService.list(round).size();
            int remaining = max - inscribed;
            java.util.List<entite.composite.EPlayerPassword> selected = appContext.getPlayer().getDraggedPlayers();
            LOG.debug("selected={} inscribed={} remaining={}", selected.size(), inscribed, remaining);
            if (selected.size() > remaining) {
                selected.remove(selected.size() - 1);
                String msg = "Maximum " + remaining + " joueur(s) supplémentaire(s) autorisé(s)";
                LOG.warn("{}", msg);
                showMessageFatal(msg);
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void rowPlayerSelectAll() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            entite.Round round = appContext.getRound();
            int max = (round.getRoundGame() != null
                    && round.getRoundGame().equals(entite.Round.GameType.MP_SINGLE.toString())) ? 2 : 4;
            int inscribed = roundPlayersListService.list(round).size();
            int remaining = max - inscribed;
            java.util.List<entite.composite.EPlayerPassword> selected = appContext.getPlayer().getDraggedPlayers();
            LOG.debug("selectAll size={} remaining={}", selected.size(), remaining);
            if (remaining <= 0) {
                selected.clear();
                showMessageFatal("Aucune place disponible — " + inscribed + " joueur(s) déjà inscrit(s)");
            } else if (selected.size() > remaining) {
                selected.subList(remaining, selected.size()).clear();
                String msg = "Sélection limitée à " + remaining + " joueur(s) supplémentaire(s)";
                LOG.warn("{}", msg);
                showMessageFatal(msg);
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void playerPasswordListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("oldValue={}", e.getOldValue());
        LOG.debug("newValue={}", e.getNewValue());
        password.setCurrentPassword(e.getNewValue().toString());
        LOG.debug("playerPassword set");
    } // end method

    public void playerConfirmPasswordListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("oldValue={}", e.getOldValue());
        LOG.debug("newValue={}", e.getNewValue());
        password.setWrkconfirmpassword(e.getNewValue().toString());
    } // end method

    // ========================================
    // GETTERS / SETTERS
    // ========================================

    public boolean isNextPanelPlayer() { return nextPanelPlayer; }
    public void setNextPanelPlayer(boolean nextPanelPlayer) { this.nextPanelPlayer = nextPanelPlayer; }

    public boolean isShowPlayerList() { return showPlayerList; }
    public void togglePlayerList() { this.showPlayerList = !this.showPlayerList; }

    public String getCreateModifyPlayer() { return createModifyPlayer; }
    public void setCreateModifyPlayer(String createModifyPlayer) { this.createModifyPlayer = createModifyPlayer; }

    public int getDeletePlayer() { return deletePlayer; }
    public void setDeletePlayer(int deletePlayer) { this.deletePlayer = deletePlayer; }

    public Password getPassword() { return password; }
    public void setPassword(Password password) { this.password = password; }

    public boolean isNextPlayer() { return appContext.isNextPlayer(); }
    public void setNextPlayer(boolean b) { appContext.setNextPlayer(b); }

    public boolean isConnected() { return appContext.isConnected(); }
    public void setConnected(boolean b) { appContext.setConnected(b); }

    // ========================================
    // MÉTHODES MIGRÉES
    // ========================================

    public String registereIDPlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.setPlayer(new smartCard.SmartcardBelgium().initClient());
            LOG.debug("back from eID card player={}", appContext.getPlayer());
            if (appContext.getPlayer() == null) {
                LOG.error("eID card Belgium not found");
                showMessageFatal("eID Card Belgium not found");
                return null;
            }
            return "player.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            LOG.error("exception in registereIDPlayer", ex);
            showMessageFatal("eID card registration failed: " + ex.getMessage());
            return null;
        }
    } // end method

    public String createLocalAdministrator() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Club club = appContext.getClub();
            LOG.debug("club={}", club);
            LOG.debug("playerTemp={}", appContext.getPlayerTemp());

            Player localAdmin = playerManager.readPlayer(appContext.getPlayerTemp().getIdplayer());
            LOG.debug("player role={}", localAdmin.getPlayerRole());

            if (localAdmin.getPlayerRole().equals("PLAYER")) {
                localAdmin.setPlayerRole("admin");
                if (updatePlayer.update(localAdmin)) {
                    LOG.info("localAdmin role updated player={}", localAdmin);
                    showMessageInfo("Local administrator role set — " + localAdmin);
                } else {
                    LOG.error("FAILURE updating player role player={}", localAdmin);
                    showMessageFatal("FAILURE Update player for localAdmin! — " + localAdmin);
                    return null;
                }
            } else {
                LOG.debug("player already has role={}, no role change needed", localAdmin.getPlayerRole());
            }

            club.setClubLocalAdmin(localAdmin.getIdplayer());
            if (updateClub.update(club)) {
                LOG.info("club localAdmin updated club={} player={}", club.getIdclub(), localAdmin.getIdplayer());
                showMessageInfo("Club updated — local administrator: "
                        + localAdmin.getIdplayer() + " / " + localAdmin.getPlayerLastName()
                        + " for club " + club.getIdclub() + " / " + club.getClubName());
            } else {
                LOG.error("FAILURE modify club localAdmin club={}", club);
                showMessageFatal("FAILURE modify club localAdmin! — " + club);
            }
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public List<ECourseList> listHandicaps() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return handicapList.list(appContext.getPlayer());
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    public void savePlayer(ActionEvent actionEvent) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        FacesMessage msg = new FacesMessage("Successful", "Welcome :" + appContext.getPlayer().getPlayerFirstName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    } // end method

    public void onPlayerSelected(SelectEvent<?> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Object obj = event.getObject();
        switch (obj) {
            case DialogResult<?> dr -> {
                Player p = (Player) dr.data();
                appContext.setLocalAdmin(p);
            }
            case String s -> LOG.warn("dialog returned String: {}", s);
            default -> LOG.error("unexpected dialog return type: {}", obj.getClass());
        }
    } // end method

    // ========================================
    // MÉTHODES PASSWORD
    // ========================================

    public String validateExistingPassword() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("player={}", appContext.getPlayer());
            LOG.debug("currentPassword provided={}", password.getCurrentPassword() != null);
            Password passwordtrf = password;

            EPlayerPassword epp = new EPlayerPassword(appContext.getPlayer(), password);
            LOG.debug("password transferred to ReadPlayer={}", epp.password());

            epp = playerManager.readPlayerWithPassword(epp.getPlayer().getIdplayer());
            LOG.debug("epp returned from LoadPlayer={}", epp);

            password = epp.password();
            password.setCurrentPassword(passwordtrf.getCurrentPassword());
            epp = epp.withPassword(password);

            if (findPassword.passwordMatch(epp)) {
                LOG.debug("existing password correct");
                passwordVerification("OK");
                return "password_create.xhtml?faces-redirect=true";
            } else {
                LOG.error("old password NOT correct");
                passwordVerification("KO");
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public String passwordVerification(String OK_KO) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} ok_ko={}", methodName, OK_KO);
        try {
            LOG.debug("for player={}", appContext.getPlayer());
            if ("OK".equals(OK_KO)) {
                LOG.debug("password correct");
                return null;
            }
            if ("KO".equals(OK_KO)) {
                String msg = LCUtil.prepareMessageBean("connection.failed");
                LOG.info(msg);
                showMessageInfo(msg);
                blocking = loadBlocking.load(appContext.getPlayer());
                LOG.debug("returned blocking={}", blocking);
                if (blocking == null) {
                    LOG.debug("no blocking record — creating");
                    boolean b = createBlocking.create(appContext.getPlayer());
                    LOG.debug("blocking record written={}", b);
                    return "selectPlayer.xhtml?faces-redirect=true";
                }
                if (blocking.getBlockingAttempts() > 2) {
                    msg = LCUtil.prepareMessageBean("connection.blocked");
                    LOG.info(msg);
                    showMessageInfo(msg);
                } else {
                    short s = blocking.getBlockingAttempts();
                    blocking.setBlockingAttempts(s += 1);
                    updateBlocking.update(blocking);
                    return "selectPlayer.xhtml?faces-redirect=true";
                }
            }
            return null;
        } catch (Exception e) {
            LOG.error("exception in passwordVerification player={}", appContext.getPlayer().getPlayerLastName(), e);
            showMessageFatal("Exception in passwordVerification: " + e.getMessage());
            return null;
        }
    } // end method

    public String modifyPassword() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("password={}", password);
            EPlayerPassword epp = new EPlayerPassword(appContext.getPlayer(), password);
            if (updatePassword.update(epp)) {
                LOG.debug("modifyPassword returned true");
                cacheInvalidator.invalidatePlayerCaches();
                return "login.xhtml?faces-redirect=true";
            } else {
                LOG.debug("modifyPassword returned false");
                return null;
            }
        } catch (Exception ex) {
            LOG.error("exception in modifyPassword", ex);
            showMessageFatal("modify Password Exception: " + ex.getMessage());
            return null;
        }
    } // end method

    // ========================================
    // GETTERS / SETTERS — blocking
    // ========================================

    public Blocking getBlocking() { return blocking; }
    public void setBlocking(Blocking blocking) { this.blocking = blocking; }

    // ========================================
    // ACTIVATION / PASSWORD
    // ========================================

    public Activation getActivation() { return activation; }
    public void setActivation(Activation activation) {
        this.activation = activation;
        LOG.debug("activation set={}", activation);
    } // end method

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String forgetPassword() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            createActivationPassword.create(appContext.getPlayer());
            utils.LCUtil.showDialogInfo("Nous venons de vous envoyer un mail", "vous devez y répondre dans les 10 minutes !");
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String resetPassword() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("uuid={}", uuid);
            LOG.debug("current player={}", appContext.getPlayer());
            LOG.debug("activation={}", activation);
            Duration difference = Duration.between(activation.getActivationCreationDate(), LocalDateTime.now());
            long differenceInMinutes = difference.toMinutes();
            LOG.debug("difference in minutes={}", differenceInMinutes);
            if (differenceInMinutes < 10) {
                String msg = LCUtil.prepareMessageBean("password.reset.ok") + (10 - differenceInMinutes) + " minutes";
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
            } else {
                LOG.debug("too late for password reinitialisation");
                appContext.getPlayer().setIdplayer(null);
                LOG.error("password reset too late player={} minutes={}", activation.getActivationPlayerId(), differenceInMinutes);
                LCUtil.showMessageFatal("You are " + differenceInMinutes + " minutes too late for the reset of your Password.");
                return "login.xhtml?faces-redirect=true";
            }
            var v = passwordController.resetPassword(activation);
            appContext.setPlayer(v.getPlayer());
            if (appContext.getPlayer() != null) {
                LOG.info("password reset requested by player={}", appContext.getPlayer().getIdplayer());
                showMessageInfo("The password reset was asked by " + appContext.getPlayer().getIdplayer());
                return "login.xhtml?faces-redirect=true";
            } else {
                LOG.error("activation record not found nonce={}", activation);
                showMessageFatal("Activation record not found : you already had done this work in a recent past!");
                return null;
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public void completeActivation(String UUID) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} UUID={}", methodName, UUID);
        try {
            activation.setActivationKey(UUID);
            activation = readActivation.read(activation);
            if (activation == null) {
                LOG.debug("activation not found");
            } else {
                LOG.debug("activation found={}", activation);
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public String activateNewPlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} activation={}", methodName, activation);
        try {
            if (activation == null) {
                LOG.debug("activation is null — return");
                return null;
            }
            Duration difference = Duration.between(activation.getActivationCreationDate(), LocalDateTime.now());
            long differenceInMinutes = difference.toMinutes();
            LOG.debug("difference in minutes={}", differenceInMinutes);
            if (differenceInMinutes < 10) {
                String msg = "Votre Enregistrement à GolfLC est activé !!! <br/> You Respected the deadline of 10 minutes :"
                        + " it was remaining = " + (10 - differenceInMinutes);
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
            } else {
                appContext.getPlayer().setIdplayer(null);
                LOG.error("activation too late player={} minutes={}", activation, differenceInMinutes);
                LCUtil.showMessageFatal("You are " + differenceInMinutes + " minutes too late for your Registration activation.");
                return "login.xhtml?faces-redirect=true";
            }
            String s = activationController.check(activation);
            LOG.debug("activation check result={}", s);
            if (appContext.getPlayer() != null) {
                String msg = LCUtil.prepareMessageBean("player.welcome" + appContext.getPlayer().getPlayerFirstName());
                LOG.info(msg);
                showMessageInfo(msg);
                return "login.xhtml?faces-redirect=true";
            } else {
                LOG.error("activation record not found — already used activation={}", activation);
                showMessageFatal("Activation record not found : you already had done this work in a recent past!");
                return null;
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String getHomeClubName() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Integer homeClubId = appContext.getPlayer().getPlayerHomeClub();
            if (homeClubId == null || homeClubId <= 0) {
                return "";
            }
            entite.Club club = new entite.Club();
            club.setIdclub(homeClubId);
            club = readClubService.read(club);
            return club != null ? club.getClubName() : "";
        } catch (Exception e) {
            LOG.debug("could not resolve home club name");
            return "";
        }
    } // end method

} // end class
