
package connection_package;

import Controller.refact.PlayerController;
import Controllers.CourseController;
import context.ApplicationContext;
import entite.Player;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("loginBean")
@RequestScoped
public class LoginBean implements Serializable {
// ✅ Injection du contexte de session
    @Inject
    private ApplicationContext appContext;
    // à vérifier si pas meilleure solution ??
//    @Inject
//    private CourseController courseC;

    @Inject
    private PlayerController playerC;

  //  @PostConstruct
      // ❌ RETIRER @PostConstruct pour éviter double appel
    // La méthode sera appelée UNIQUEMENT par <f:viewAction>
    public void prepareLogin() {
        // --- Initialisation de la session ---
        LOG.debug("LoginBean.prepareLogin() called");
        // Reset CourseController pour la session, depuis login
        LOG.debug("starting reset of coursecontroller from LoginBean");
    //    courseC.reset("from LoginBean - prepareLogin login"); /////////////////////  à vérifier ici !!! se fait avec Postconstruct ??

        // Initialise ApplicationCopntext si nécessaire
        if (appContext.getPlayer() == null) {
            appContext.setPlayer(new Player());
            LOG.debug("PlayerController player initialized in LoginBean");
        }
        appContext.setPlayerPro(new Player());  // nécessaire ? je crois que non 
        appContext.setLocalAdmin(new Player());
        appContext.setPlayerTemp(new Player());
    }

    // Méthode pour le login effectif (formulaire)
 // public String doLogin() {
//       // login logic ici
 //       return "selectPlayer.xhtml?faces-redirect=true";
 // }
} // end class