package Controller.refact;

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

/**
 * Controller pour la gestion des joueurs
 * ✅ Délègue l'accès au Player à ApplicationContext
 * ✅ Les vues continuent à utiliser #{playerC.player}
 * ✅ handicapIndex délégué à ApplicationContext
 * ✅ Standards CDI : methodName + handleGenericException
 */
@Named("playerC")
@SessionScoped
public class PlayerController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private PlayerManager      playerManager;
    @Inject private CoordinatesService coordinatesService;
    @Inject private ApplicationContext appContext;
    @Inject private cache.CacheInvalidator cacheInvalidator;
    @Inject private find.FindLastLogin findLastLoginService; // migrated 2026-02-25
    @Inject private Controllers.LanguageController         languageController; // fix multi-user 2026-03-07
    @Inject private lists.PlayersList                     playersListService; // fix password cache bug 2026-03-07

    // ✅ Injections ajoutées — migration player methods 2026-02-25
    @Inject private Controllers.PasswordController                 passwordController;
    @Inject private update.UpdatePlayer                            updatePlayer;
    @Inject private update.UpdateClub                              updateClub;
    @Inject private lists.HandicapList                             handicapList;
    @Inject private read.LoadBlocking                              loadBlocking;         // migrated 2026-02-25
    @Inject private create.CreateBlocking                          createBlocking;       // migrated 2026-02-25
    @Inject private update.UpdateBlocking                          updateBlocking;       // migrated 2026-02-25
    @Inject private update.UpdatePassword                          updatePassword;       // migrated 2026-02-25
    @Inject private find.FindPassword                              findPassword;         // migrated 2026-02-25
    // @Inject @SessionMap sessionMap — removed 2026-02-28, migrated to appContext
    // @ApplicationMap removed 2026-03-22 — unused

    private EPlayerPassword selectedPlayerEPP = null;
    private Blocking blocking; // migrated from CourseController 2026-02-25
    private boolean nextPanelPlayer = false; // migrated from CourseController 2026-02-25
    private boolean showPlayerList = false;
    private String createModifyPlayer = "C"; // C=Create, M=Modify — migrated from CourseController 2026-02-25
    private int deletePlayer = 0; // migrated from CourseController 2026-02-25
    private Password password = new Password(); // migrated from CourseController 2026-02-25
// ✅ Ajouté dans PlayerController
//    private ECourseList selectedHandicap = null;
    private List<ECourseList> filteredPlayedRound = null;

    // ✅ Session-level cache — avoid repeated DB queries on JSF re-render
    private List<ECourseList>    cachedHandicapWHS    = null;
    private List<ECourseList>    cachedPlayedRounds   = null;
    private List<EPlayerPassword> cachedPlayers       = null;
    private List<Professional>   cachedProfessionals  = null;
    @Inject private Controllers.MongoCalculationsController mongoCalculationsController;

    // ✅ Injections Activation/Password — migrated 2026-02-26 from CourseController
    @Inject private read.ReadActivation                             readActivation;
    @Inject private create.CreateActivationPassword                 createActivationPassword;
    @Inject private Controllers.ActivationController                activationController;
    @Inject private read.ReadClub                                   readClubService;      // added 2026-03-19 for homeClubName
    @Inject private lists.RoundPlayersList                          roundPlayersListService; // added 2026-03-26 for rowPlayerSelect limit

    // ✅ Injections logout/loginAPI/selectedPlayerFromDialog — migrated 2026-02-27
 //   @Inject private find.FindLastAudit                              findLastAudit;
 //   @Inject private update.UpdateAudit                              updateAudit;
    // externalContext injection removed — fix multi-user 2026-03-07 (request-scoped, must not be cached in @SessionScoped)
  //  @Inject private Controller.refact.NavigationController            navigationController; // renamed 2026-02-28
    // ========================================
    // DÉLÉGATION — Player (pour les vues)
    // ========================================

    /**
     * Les vues continuent à utiliser #{playerC.player}
     * Délègue à ApplicationContext
     */
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

    /**
     * Listener for player language change — fix multi-user 2026-03-07
     * Moved from Player POJO (cannot call CDI from POJO)
     */
    public void playerLanguageListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String newLanguage = e.getNewValue().toString();
        LOG.debug("playerLanguage NewValue = {}", newLanguage);
        appContext.getPlayer().setPlayerLanguage(newLanguage);
        languageController.setLanguage(newLanguage);
    } // end method

    public Player getPlayerPro()              { return appContext.getPlayerPro(); }
    public void   setPlayerPro(Player player) { appContext.setPlayerPro(player); }

    public Player getLocalAdmin()              { return appContext.getLocalAdmin(); }
    public void   setLocalAdmin(Player player) { appContext.setLocalAdmin(player); }

    public Player getPlayerTemp()              { return appContext.getPlayerTemp(); }
    public void   setPlayerTemp(Player player) { appContext.setPlayerTemp(player); }
    
    // ✅ État UI — dans PlayerController, pas dans l'entité HandicapIndex
    private List<ECourseList> filteredHandicaps = null;

    // ✅ State Activation/Password — migrated 2026-02-26 from CourseController
    private Activation activation = new Activation();
    private String uuid;

    // ========================================
    // CDI EVENT — ResetEvent observer — 2026-02-26
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} — source: {}", event.getSource());
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
    // ✅ courseC.handicapIndex → playerC.handicapIndex
    // ========================================

    public HandicapIndex getHandicapIndex() {
        return appContext.getHandicapIndex();
    }

    public void setHandicapIndex(HandicapIndex handicapIndex) {
        appContext.setHandicapIndex(handicapIndex);
    }

    // ========================================
    // MÉTHODES HELPER
    // ========================================

    public boolean hasPlayer() {
        Player p = appContext.getPlayer();
        return p != null && p.getIdplayer() != null;
    }

    public boolean isAdmin() {
        Player p = appContext.getPlayer();
        return p != null && "ADMIN".equals(p.getPlayerRole());
    }

    public boolean isLocalAdmin() {
        Player p = appContext.getPlayer();
        return p != null && "admin".equalsIgnoreCase(p.getPlayerRole());
    }

    public boolean isRegularPlayer() {
        Player p = appContext.getPlayer();
        return p != null && "PLAYER".equalsIgnoreCase(p.getPlayerRole());
    }

    public boolean isProfessional() {
        return !getProfessionals().isEmpty();
    }

    public int getProfessionalCount() {
        return getProfessionals().size();
    }

    public boolean canShowMenu() {
        Player p = appContext.getPlayer();
        return p != null && p.isShowMenu();
    }

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
            LOG.debug("player before creation = {}", player);

            SaveResult result = playerManager.createPlayer(
                player,
                appContext.getHandicapIndex()        // ✅ via appContext
            );

            if (result.isSuccess()) {
                LOG.debug("player created successfully");
                appContext.setPlayer(result.getPlayer());
                appContext.setNextPlayer(true);
        // dans playerManager        showMessageInfo(result.getMessage());
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
                LOG.debug(result.getMessage());
        //        showMessageInfo(result.getMessage());
            } else {
                 LOG.debug(result.getMessage());
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
            LOG.debug("returning cached list size = {}", cachedPlayers.size());
            return cachedPlayers;
        }
        try {
            cachedPlayers = playerManager.listPlayers();                     // ✅ via manager
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
            LOG.debug("returning cached list size = {}", cachedProfessionals.size());
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
/**
 * Liste les HandicapIndex WHS — appelé depuis le XHTML
 * #{playerC.listHandicapWHS()}
 */
public List<ECourseList> listHandicapWHS() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    if (cachedHandicapWHS != null) {
        LOG.debug("returning cached list size = {}", cachedHandicapWHS.size());
        return cachedHandicapWHS;
    }
    try {
        cachedHandicapWHS = playerManager.listHandicapWHS(appContext.getPlayer()); // ✅ via manager
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
        LOG.debug("returning cached list size = {}", cachedPlayedRounds.size());
        appContext.setPlayedRounds(cachedPlayedRounds);
        return cachedPlayedRounds;
    }
    try {
        cachedPlayedRounds = playerManager.listPlayedRounds(appContext.getPlayer()); // ✅ via manager
        appContext.setPlayedRounds(cachedPlayedRounds);          // ✅ synchronise ApplicationContext
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
        LOG.debug("returning cached list size = {}", cachedPlayedRounds.size());
        appContext.setPlayedRounds(cachedPlayedRounds);
        return cachedPlayedRounds;
    }
    try {
        cachedPlayedRounds = playerManager.listPlayedRounds(appContext.getPlayer()); // ✅ via manager
        appContext.setPlayedRounds(cachedPlayedRounds);              // ✅ synchronise ApplicationContext
        return cachedPlayedRounds;
    } catch (Exception e) {
        handleGenericException(e, methodName);
        return Collections.emptyList();                 // ✅ jamais null
    }
} // end method

/**
 * Invalide les caches session pour les listes per-user.
 * Appelé quand les données handicap/rounds changent.
 */
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
        // ✅ Null check — selectedHandicap peut être null
        ECourseList selected = appContext.getSelectedHandicap();
        if (selected == null) {
            LOG.warn("selectedHandicap is null — action skipped");
            return;
        }

        LOG.debug("handicapIndex  = {}", appContext.getHandicapIndex());
        LOG.debug("roundId        = {}", selected.round().getIdround());
        LOG.debug("playerId       = {}", appContext.getPlayer().getIdplayer());

        // ✅ LoggingUser est un POJO — new correct ici
        LoggingUser logging = new LoggingUser();
        logging.setLoggingIdPlayer(appContext.getPlayer().getIdplayer());
        logging.setLoggingIdRound(selected.round().getIdround());  // ✅ record accessor
        logging.setLoggingType("H");

        // ✅ Via injection — plus de new MongoCalculationsController()
        appContext.getHandicapIndex().setCalculations(mongoCalculationsController.read(logging));

        LOG.debug("calculations loaded");

    } catch (Exception e) {
        handleGenericException(e, methodName);
    }
} // end method


/*/ ✅ Getters / Setters état UI
public ECourseList2 getSelectedHandicap() {
    return selectedHandicap;
}

public void setSelectedHandicap(ECourseList2 selectedHandicap) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    this.selectedHandicap = selectedHandicap;
    if (selectedHandicap != null) {
        // ✅ Synchronise ApplicationContext si nécessaire
        appContext.setHandicapIndex(selectedHandicap.handicapIndex());
        LOG.debug("selectedHandicap = {}", selectedHandicap);
    }
} // end method
*/
// ✅ Dans PlayerController — délégation pure, pas de champ local
public ECourseList getSelectedHandicap() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    return appContext.getSelectedHandicap();
} // end method

public void setSelectedHandicap(ECourseList selectedHandicap) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    appContext.setSelectedHandicap(selectedHandicap);
    LOG.debug("selectedHandicap = {}", selectedHandicap);
} // end method

// ✅ Dans PlayerController — délégation pure, pas de champ local
public ECourseList getSelectedPlayedRound() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    return appContext.getSelectedPlayedRound();
} // end method

public void setSelectedPlayedRound(ECourseList selectedPlayedRound) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    appContext.setSelectedPlayedRound(selectedPlayedRound);
    LOG.debug("selectedPlayedRound = {}", selectedPlayedRound);
} // end method

public List<ECourseList> getFilteredHandicaps() {
    return filteredHandicaps;
}

public void setFilteredHandicaps(List<ECourseList> filteredHandicaps) {
    this.filteredHandicaps = filteredHandicaps;
} // end method

    // ========================================
    // SELECTION — dataTable
    // ========================================

    public EPlayerPassword getSelectedPlayerEPP() {
        return selectedPlayerEPP;
    }

    public void setSelectedPlayerEPP(EPlayerPassword selectedPlayerEPP) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.selectedPlayerEPP = selectedPlayerEPP;
        if (selectedPlayerEPP != null) {
            appContext.setPlayer(selectedPlayerEPP.getPlayer()); // ✅ synchronise ApplicationContext
            LOG.debug("selectedPlayerEPP = {}", selectedPlayerEPP);
        }
    } // end method

    // ========================================
    // SELECTION — filtering
    // ========================================

    public List<ECourseList> getFilteredPlayedRound() {
        return filteredPlayedRound;
    }

    public void setFilteredPlayedRound(List<ECourseList> filteredPlayedRound) {
        this.filteredPlayedRound = filteredPlayedRound;
    }
    
    
    // ========================================
    // CHARGEMENT
    // ========================================

    public void loadPlayer(int idplayer) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (idplayer <= 0) {
                showMessageFatal(methodName + " - Invalid player ID = " + idplayer);
                return;
            }

            Player player = playerManager.readPlayer(idplayer);
            appContext.setPlayer(player);
            LOG.debug("player loaded = {}", player.getPlayerLastName());

        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void loadPlayerWithPassword(int idplayer) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (idplayer <= 0) {
                showMessageFatal(methodName + " - Invalid player ID = " + idplayer);
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
    // MÉTHODES SIMPLES — migrées de CourseController 2026-02-25
    // ========================================

    /**
     * Valide le joueur et affiche le 2e panelGrid (player_modify.xhtml)
     */
    public void validatePlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        setNextPanelPlayer(true);
    } // end method

    /**
     * Prépare le mode modification du joueur (f:viewAction dans player_modify.xhtml)
     */
    public void to_player_modify(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with string = {}", s);
        createModifyPlayer = s;
    } // end method

    /**
     * Dernière session du joueur (welcome.xhtml)
     */
    public String lastSession(int idplayer) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with idplayer = {}", idplayer);
        try {
            Audit audit = findLastLoginService.find(appContext.getPlayer());
            if (audit != null && audit.getAuditStartDate() != null) {
                LOG.debug("date last connection = {}", audit.getAuditStartDate().format(ZDF_TIME_HHmm));
                return audit.getAuditStartDate().format(ZDF_TIME_HHmm);
            } else {
                return "First Connection for this player";
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "Error loading last session";
        }
    } // end method

    /**
     * Delete cascading player — stub (delete_cascading_player.xhtml)
     */
    public void deleteCascadingPlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            // TODO: implémenter la suppression cascadée via PlayerManager
            LOG.warn("not yet implemented");
            showMessageFatal("Delete cascading player not yet implemented");
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    /**
     * Auto-load player quand l'ID change (local_administrator.xhtml)
     */
    public void onPlayerIdChanged() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with localAdmin = {}", appContext.getLocalAdmin());
        if (appContext.getLocalAdmin().getIdplayer() == null || appContext.getLocalAdmin().getIdplayer() <= 0) {
            LOG.debug("localAdmin null or 0");
            return;
        }
        try {
            Player loadedPlayer = playerManager.readPlayer(appContext.getLocalAdmin().getIdplayer());
            LOG.debug("loadedPlayer = {}", loadedPlayer);
            if (loadedPlayer != null) {
                appContext.setLocalAdmin(loadedPlayer);
                showMessageInfo("Player loaded: " + appContext.getLocalAdmin().getPlayerLastName());
            } else {
                showMessageFatal("Player not found");
            }
        } catch (Exception e) {
            LOG.error("Error loading player", e);
            showMessageFatal("Error loading player " + e);
        }
    } // end method

    /**
     * Validation sélection multi-joueurs (select_other_players.xhtml)
     * Limite = 4 (ou 2 pour MP_SINGLE) moins les joueurs déjà inscrits.
     */
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
                selected.remove(selected.size() - 1); // retire le dernier cochés
                String msg = "Maximum " + remaining + " joueur(s) supplémentaire(s) autorisé(s)";
                LOG.warn("{}", msg);
                showMessageFatal(msg);
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /**
     * Validation "Select All" (select_other_players.xhtml)
     * Tronque la sélection au nombre de places restantes.
     */
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

    /**
     * Listener mot de passe joueur (password_modify.xhtml)
     */
    public void playerPasswordListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("OldValue = {}", e.getOldValue());
        LOG.debug("NewValue = {}", e.getNewValue());
        password.setCurrentPassword(e.getNewValue().toString());
        LOG.debug("playerPassword set");
    } // end method

    /**
     * Listener confirmation mot de passe joueur (password_modify.xhtml)
     */
    public void playerConfirmPasswordListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("OldValue = {}", e.getOldValue());
        LOG.debug("NewValue = {}", e.getNewValue());
        password.setWrkconfirmpassword(e.getNewValue().toString());
    } // end method

    // ========================================
    // GETTERS / SETTERS — migrés de CourseController 2026-02-25
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

    // ✅ Délégation — nextPlayer (pour player_modify.xhtml #{playerC.nextPlayer})
    public boolean isNextPlayer() { return appContext.isNextPlayer(); }
    public void setNextPlayer(boolean b) { appContext.setNextPlayer(b); }

    // ✅ Délégation — connected (pour header.xhtml si migration future)
    public boolean isConnected() { return appContext.isConnected(); }
    public void setConnected(boolean b) { appContext.setConnected(b); }

    // ========================================
    // MÉTHODES MIGRÉES DE CourseController — 2026-02-25
    // ========================================


    /**
     * Enregistre un joueur via eID belge (register_eID_player.xhtml)
     */
    public String registereIDPlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.setPlayer(new smartCard.SmartcardBelgium().initClient());
            LOG.debug("back from external resource with cardBelgium = {}", appContext.getPlayer());
            if (appContext.getPlayer() == null) {
                String msg = "eid Card Belgium not found";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }
            return "player.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            String msg = "Exception in registereIDPlayer " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    /**
     * Crée un administrateur local pour un club (local_administrator.xhtml)
     */
    public String createLocalAdministrator() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Club club = appContext.getClub();
            LOG.debug("for club = {}", club);
            LOG.debug("with playerTemp = {}", appContext.getPlayerTemp());

            Player localAdmin = playerManager.readPlayer(appContext.getPlayerTemp().getIdplayer());
            LOG.debug("player role = {}", localAdmin.getPlayerRole());

            if (localAdmin.getPlayerRole().equals("PLAYER")) {
                localAdmin.setPlayerRole("admin"); // Local Administrator
                if (updatePlayer.update(localAdmin)) {
                    String msg = "Update Player with localAdmin = OK " + localAdmin;
                    LOG.info(msg);
                    showMessageInfo(msg);
                } else {
                    String msg = "FAILURE Update player for localAdmin ! = " + localAdmin;
                    LOG.error(msg);
                    showMessageFatal(msg);
                    return null;
                }
            } else {
                LOG.debug("player already has role={}, no role change needed", localAdmin.getPlayerRole());
            }

            club.setClubLocalAdmin(localAdmin.getIdplayer());
            if (updateClub.update(club)) {
                String msg = "Club updated local administrator created <br> = "
                        + localAdmin.getIdplayer() + " / " + localAdmin.getPlayerLastName()
                        + "<br> for club = " + club.getIdclub() + " / " + club.getClubName();
                LOG.info(msg);
                showMessageInfo(msg);
            } else {
                String msg = "FAILURE modify club localAdmin ! = " + club;
                LOG.error(msg);
                showMessageFatal(msg);
            }
            return null;
        } catch (Exception ex) {
            String msg = "Exception in createLocalAdministrator " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    /**
     * Liste handicaps pour un joueur (appelé depuis Java)
     */
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

    /**
     * Callback wizard player (player.xhtml)
     */
    public void savePlayer(ActionEvent actionEvent) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        FacesMessage msg = new FacesMessage("Successful", "Welcome :" + appContext.getPlayer().getPlayerFirstName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    } // end method

    /**
     * Dialog callback — sélection d'un joueur via dialog (local_administrator.xhtml)
     */
    public void onPlayerSelected(SelectEvent<?> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Object obj = event.getObject();
        switch (obj) {
            case DialogResult<?> dr -> {
                Player p = (Player) dr.data();
                appContext.setLocalAdmin(p);
            }
            case String s -> LOG.warn("Dialog returned String: {}", s);
            default -> LOG.error("Unexpected dialog return type: {}", obj.getClass());
        }
    } // end method

    // ========================================
    // MÉTHODES PASSWORD — migrées de CourseController 2026-02-25
    // ========================================

    /**
     * Valide le mot de passe existant avant modification (password_modify.xhtml)
     */
    public String validateExistingPassword() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("player = {}", appContext.getPlayer());
            LOG.debug("password = {}", password);
            LOG.debug("currentPassword provided = {}", password.getCurrentPassword() != null);
            Password passwordtrf = password;

            EPlayerPassword epp = new EPlayerPassword(appContext.getPlayer(), password);
            LOG.debug("password transferred to ReadPlayer = {}", epp.password());

            epp = playerManager.readPlayerWithPassword(epp.getPlayer().getIdplayer());
            LOG.debug("epp returned from LoadPlayer = {}", epp);

            password = epp.password();
            password.setCurrentPassword(passwordtrf.getCurrentPassword());
            epp = epp.withPassword(password);

            if (findPassword.passwordMatch(epp)) {
                LOG.debug("existing password correct !");
                passwordVerification("OK");
                return "password_create.xhtml?faces-redirect=true";
            } else {
                LOG.error("old password NOT correct !");
                passwordVerification("KO");
                return null;
            }
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Vérifie le résultat de la saisie du mot de passe et gère le blocking (appelé par validateExistingPassword)
     */
    public String passwordVerification(String OK_KO) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with = {}", OK_KO);
        try {
            LOG.debug("for player = {}", appContext.getPlayer());
            if ("OK".equals(OK_KO)) {
                LOG.debug("Password Correct");
                return null;
            }
            if ("KO".equals(OK_KO)) {
                String msg = LCUtil.prepareMessageBean("connection.failed");
                LOG.info(msg);
                showMessageInfo(msg);
                blocking = loadBlocking.load(appContext.getPlayer());
                LOG.debug("returned blocking = {}", blocking);
                if (blocking == null) {
                    LOG.debug("il n'existe pas de record blocage — création");
                    boolean b = createBlocking.create(appContext.getPlayer());
                    LOG.debug("record blocking written ? = {}", b);
                    return "selectPlayer.xhtml?faces-redirect=true";
                }
                if (blocking.getBlockingAttempts() > 2) {
                    msg = LCUtil.prepareMessageBean("connection.blocked");
                    LOG.info(msg);
                    showMessageInfo(msg);
                } else {
                    short s = blocking.getBlockingAttempts();
                    blocking.setBlockingAttempts(s += 1);
                    boolean b = updateBlocking.update(blocking);
                    return "selectPlayer.xhtml?faces-redirect=true";
                }
            }
            return null;
        } catch (Exception e) {
            LOG.error("exception for player = {}: {}", appContext.getPlayer().getPlayerLastName(), e.getMessage(), e);
            showMessageFatal("Exception in passwordVerification: " + e.getMessage());
            return null;
        }
    } // end method

    /**
     * Modifie le mot de passe (include_password.xhtml)
     */
    public String modifyPassword() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with entite Password = {}", password);
            EPlayerPassword epp = new EPlayerPassword(appContext.getPlayer(), password);
            if (updatePassword.update(epp)) {
                LOG.debug("boolean returned from modifyPassword is 'true'");
                cacheInvalidator.invalidatePlayerCaches(); // centralized 2026-03-22
                return "login.xhtml?faces-redirect=true";
            } else {
                LOG.debug("boolean returned from modifyPassword is 'false'");
                return null;
            }
        } catch (Exception ex) {
            String msg = "modify Password Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    // ========================================
    // GETTERS / SETTERS — blocking
    // ========================================

    public Blocking getBlocking() { return blocking; }
    public void setBlocking(Blocking blocking) { this.blocking = blocking; }

    // ========================================
    // ACTIVATION / PASSWORD — migrated 2026-02-26 from CourseController
    // ========================================

    public Activation getActivation() { return activation; }
    public void setActivation(Activation activation) {
        this.activation = activation;
        LOG.debug("activation set to {}", "setActivation", activation);
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
            LOG.debug("uuid = {}", uuid);
            LOG.debug("current player = {}", appContext.getPlayer());
            LOG.debug("Activation resetPassword = {}", activation);
            Duration difference = Duration.between(activation.getActivationCreationDate(), LocalDateTime.now());
            long differenceInMinutes = difference.toMinutes();
            LOG.debug("difference in minutes = {}", differenceInMinutes);
            if (differenceInMinutes < 10) {
                String msg = LCUtil.prepareMessageBean("password.reset.ok") + (10 - differenceInMinutes) + " minutes";
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
            } else {
                LOG.debug("too late for reinitialisation password");
                appContext.getPlayer().setIdplayer(null);
                String msg = "You are " + differenceInMinutes + " minutes too late for the reset of your Password "
                        + activation.getActivationPlayerId();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return "login.xhtml?faces-redirect=true";
            }
            var v = passwordController.resetPassword(activation);
            appContext.setPlayer(v.getPlayer());
            if (appContext.getPlayer() != null) {
                String msg = ("The password reset was asked by " + appContext.getPlayer().getIdplayer());
                LOG.info(msg);
                showMessageInfo(msg);
                return "login.xhtml?faces-redirect=true";
            } else {
                String msg = "Activation record not found : you already had done this work in a recent past !";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public void completeActivation(String UUID) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with UUID = {}", UUID);
        try {
            activation.setActivationKey(UUID);
            activation = readActivation.read(activation);
            if (activation == null) {
                LOG.debug("Activation is null, not found !");
            } else {
                LOG.debug("Activation found = {}", activation);
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public String activateNewPlayer() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with activation = {}", activation);
        try {
            if (activation == null) {
                LOG.debug("activation is null - return");
                return null;
            }
            Duration difference = Duration.between(activation.getActivationCreationDate(), LocalDateTime.now());
            long differenceInMinutes = difference.toMinutes();
            LOG.debug("difference in minutes = {}", differenceInMinutes);
            if (differenceInMinutes < 10) {
                String msg = "Votre Enregistrement à GolfLC est activé !!! <br/> You Respected the deadline of 10 minutes :"
                        + " it was remaining = " + (10 - differenceInMinutes);
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
            } else {
                appContext.getPlayer().setIdplayer(null);
                String msg = "You are " + differenceInMinutes + " minutes too late for your Registration activation or the reset of your Password "
                        + activation;
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return "login.xhtml?faces-redirect=true";
            }
            String s = activationController.check(activation);
            LOG.debug("string s = {}", s);
            if (appContext.getPlayer() != null) {
                String msg = LCUtil.prepareMessageBean("player.welcome" + appContext.getPlayer().getPlayerFirstName());
                LOG.info(msg);
                showMessageInfo(msg);
                return "login.xhtml?faces-redirect=true";
            } else {
                String msg = "Activation record not found : you already had done this work in a recent past !";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // logout() moved to NavigationController 2026-03-07 — session lifecycle action
    // login flow moved to LoginController 2026-04-03

    /**
     * Retourne le nom du home club du joueur connecté.
     * Utilisé dans welcome.xhtml.
     */
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