package manager;

import entite.HandicapIndex;
import entite.Player;
import entite.Professional;
import entite.composite.ECourseList;
import entite.composite.EPlayerPassword;
import static exceptions.LCException.handleGenericException;
import jakarta.enterprise.context.ApplicationScoped; // ✅ Changé
import jakarta.inject.Inject;
import java.util.List;
import static interfaces.Log.LOG;
import java.util.Collections;
import static utils.LCUtil.showMessageInfo;

/**
 * ✅ Service métier pour la gestion des joueurs
 * ✅ @ApplicationScoped - Stateless, partagé entre tous
 * ✅ PAS de dépendance à ApplicationContext
 */
@ApplicationScoped  // ✅ Changé de @SessionScoped
public class PlayerManager {

 //   @Inject @ProdDB
 //   private ConnectionProvider connectionProvider;

    @Inject private create.CreatePlayer createPlayerService;
    @Inject private update.UpdatePlayer updatePlayerService;
    @Inject private read.ReadPlayer readPlayerService;
    @Inject private lists.PlayersList playersListService;                    // ✅ plus de new
    @Inject private lists.FindCountListProfessional findCountListProfessional;  // ✅ injection
    @Inject private lists.HandicapIndexList handicapIndexList;
    @Inject private lists.PlayedList        playedList;
    // ========================================
    // AUCUNE modification ici !
    // ========================================
    /**
     * Crée un nouveau joueur
     * ✅ Reçoit TOUT en paramètres
     */
    public SaveResult createPlayer(Player player, HandicapIndex handicapIndex) {
        // Validation
        LOG.debug("entering Player Manager - createPlayer");
        LOG.debug("with player = " + player);
        LOG.debug("with handicapIndex = " + handicapIndex);
        if (player == null) {
            return SaveResult.failure("Player cannot be null");
        }
        if (player.getPlayerLastName() == null || player.getPlayerLastName().trim().isEmpty()) {
            return SaveResult.failure("Player last name is required");
        }
        
        try {
            LOG.debug("before createPlayerService");
            boolean success = createPlayerService.create(player, handicapIndex, "A");
            if (success) {
                String msg = String.format("Player created: %s %s (ID: %d)",
                    player.getPlayerFirstName(),
                    player.getPlayerLastName(),
                    player.getIdplayer());
                LOG.debug(msg);
                showMessageInfo(msg);
                return SaveResult.success(msg, player);
            } else {
                return SaveResult.failure("Player creation failed");
            }
        } catch (Exception e) {
            LOG.error("Exception creating player", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }
    
    /**
     * Modifie un joueur existant
     * ✅ Reçoit le player en paramètre
     */
    public SaveResult modifyPlayer(Player player) {
        if (player == null || player.getIdplayer() == null) {
            return SaveResult.failure("Invalid player");
        }
        try {
            boolean success = updatePlayerService.update(player);
            if (success) {
                String msg = "Player updated: " + player.getPlayerLastName();
                showMessageInfo(msg);
                return SaveResult.success(msg, player);
            } else {
                return SaveResult.failure("Update failed");
            }
            
        } catch (Exception e) {
            LOG.error("Exception updating player", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    }

    // ========================================
    // Lecture - AUCUNE modification
    // ========================================
    
    public Player readPlayer(int idplayer) throws Exception {
        if (idplayer <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + idplayer);
        }
        Player p = new Player();
        p.setIdplayer(idplayer);
        return readPlayerService.read(p);
    }
    public EPlayerPassword readPlayerWithPassword(int idplayer) throws Exception {
        if (idplayer <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + idplayer);
        }
        
        Player p = new Player();
        p.setIdplayer(idplayer);
        EPlayerPassword epp = new EPlayerPassword(p, null);
        return readPlayerService.read(epp);
    }

    // ========================================
    // Listes - AUCUNE modification
    // ========================================
    
  //  public List<EPlayerPassword> listPlayers() throws Exception {
  //      try (Connection conn = connectionProvider.getConnection()) {
   //         return new lists.PlayersList().list();
   //     }
   // }
/*
    public List<EPlayerPassword> listPlayers() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    try {
        return playersList.list();                              // ✅ plus de conn
    } catch (Exception e) {
        handleGenericException(e, methodName);
        return Collections.emptyList();
    }
}
    */
    public List<EPlayerPassword> listPlayers() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    try {
        return playersListService.list();
    } catch (Exception e) {
        handleGenericException(e, methodName);
        return Collections.emptyList();
    }
}
    

// ✅ NETTOYÉ - Plus de code commenté
    public List<Professional> findProfessionals(Player player) {
        try { //(Connection conn = connectionProvider.getConnection()) {
          //  List<Professional> listPro = new lists.FindCountListProfessional().list(player);
           List<Professional> listPro = findCountListProfessional.list(player); 
            return (listPro != null) ? listPro : Collections.emptyList();
        } catch (Exception e) {
            LOG.error("Exception in findProfessionals", e);
            return Collections.emptyList();
        }
    }    
/*
 * Liste les HandicapIndex WHS pour le joueur courant
 */
public List<ECourseList> listHandicapWHS(Player player) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    try {
        return handicapIndexList.list(player);          // ✅ via service injecté
    } catch (Exception e) {
        handleGenericException(e, methodName);
        return Collections.emptyList();                 // ✅ jamais null
    }
} // end method

    public List<ECourseList> listPlayedRounds(final Player player) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (player == null) {
            LOG.warn(methodName + " - player is null");
            return Collections.emptyList();
        }
        try {
            List<ECourseList> result = playedList.list(player);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // SaveResult - AUCUNE modification
    // ========================================
    
    public static class SaveResult {
        private final boolean success;
        private final String message;
        private final Player player;
        
        private SaveResult(boolean success, String message, Player player) {
            this.success = success;
            this.message = message;
            this.player = player;
        }
        
        public static SaveResult success(String message) {
            return new SaveResult(true, message, null);
        }
        
        public static SaveResult success(String message, Player player) {
            return new SaveResult(true, message, player);
        }
        
        public static SaveResult failure(String message) {
            return new SaveResult(false, message, null);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Player getPlayer() { return player; }
    }
}