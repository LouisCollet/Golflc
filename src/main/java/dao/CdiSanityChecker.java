package dao;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import java.util.Set;

/*
 * Utilitaire pour vérifier la disponibilité et la résolvabilité des beans CDI.
 * Utilisé principalement dans les tests d'intégration.
 */
@ApplicationScoped
public class CdiSanityChecker {

    @Inject
    BeanManager beanManager;

    /*
     * Vérifie si un bean CDI est disponible pour un type donné
      * Vérifie si au moins un bean CDI existe pour le type donné.
     * @param beanType le type de bean à rechercher
     * @return true si au moins un bean est trouvé
     */
    public boolean isBeanAvailable(Class<?> beanType) {
        Set<Bean<?>> beans = beanManager.getBeans(beanType);
        LOG.debug("Beans disponibles pour {}: {}", beanType.getSimpleName(), beans.size());
    //    return beans != null && !beans.isEmpty();
        return !beans.isEmpty(); // claude getBeans() ne retourne jamais null selon la spec CDI.
    }

    /*
     * Vérifie s'il existe exactement UN bean injectable
     * (évite les ambiguïtés CDI)
     * Vérifie si le bean est résoluble sans ambiguïté.
     * Un bean est résoluble si CDI peut déterminer une instance unique à injecter.
     * 
     * @param beanType le type de bean à vérifier
     * @return true si le bean est résoluble (non ambigu)
     */
    public boolean isBeanResolvable(Class<?> beanType) {
        
        Set<Bean<?>> beans = beanManager.getBeans(beanType);
        Bean<?> resolved = beanManager.resolve(beans);
         LOG.debug("Bean {} résoluble: {}", beanType.getSimpleName(), resolved != null);
        return resolved != null;
    }

    /*
     * Retourne le nombre de beans CDI pour un type
     * Compte le nombre de beans CDI disponibles pour un type.
     * 
     * @param beanType le type de bean à compter
     * @return le nombre de beans trouvés (0 si aucun)
     */

    public int countBeans(Class<?> beanType) {
        int count = beanManager.getBeans(beanType).size();
        LOG.debug("Nombre de beans pour {}: {}", beanType.getSimpleName(), count);
        return count;
    }

} // end 
