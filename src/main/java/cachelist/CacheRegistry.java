
package cachelist;

import static interfaces.Log.LOG;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire centralisé de tous les caches de l'application
 * Permet d'enregistrer, gérer et monitorer tous les caches depuis un point central
 *
 * @author Votre nom
 * @version 1.0
 * @since 2025-01-18
 */
public class CacheRegistry {

    private static final ConcurrentHashMap<String, CacheManager<?>> caches = 
        new ConcurrentHashMap<>();

    /**
     * Constructeur privé pour empêcher l'instanciation
     */
    private CacheRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Enregistre un cache dans le registre
     *
     * @param name Nom unique du cache
     * @param cache Instance du cache manager
     * @param <T> Type des éléments du cache
     */
    public static <T> void register(String name, CacheManager<T> cache) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Cache name cannot be null or blank");
        }
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null");
        }

        CacheManager<?> existing = caches.putIfAbsent(name, cache);
        if (existing != null) {
            LOG.warn("Cache '{}' already registered, keeping existing instance", name);
        } else {
            LOG.info("Cache registered: {}", name);
        }
    }

    /**
     * Récupère un cache par son nom
     *
     * @param name Nom du cache
     * @param <T> Type des éléments du cache
     * @return Optional contenant le cache si trouvé
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<CacheManager<T>> get(String name) {
        return Optional.ofNullable((CacheManager<T>) caches.get(name));
    }

    /**
     * Désenregistre un cache
     *
     * @param name Nom du cache à supprimer
     * @return true si le cache existait et a été supprimé
     */
    public static boolean unregister(String name) {
        boolean removed = caches.remove(name) != null;
        if (removed) {
            LOG.info("Cache unregistered: {}", name);
        }
        return removed;
    }

    /**
     * Invalide tous les caches enregistrés
     */
    public static void invalidateAll() {
        LOG.info("Invalidating all {} caches", caches.size());
        caches.values().forEach(CacheManager::invalidate);
    }

    /**
     * Réinitialise tous les caches (invalide + reset metrics)
     */
    public static void resetAll() {
        LOG.info("Resetting all {} caches", caches.size());
        caches.values().forEach(CacheManager::reset);
    }

    /**
     * Affiche les statistiques de tous les caches
     */
    public static void printAllStats() {
        if (caches.isEmpty()) {
            LOG.info("No caches registered");
            return;
        }

        LOG.info("=== Cache Statistics ({} caches) ===", caches.size());
        caches.forEach((name, cache) -> 
            LOG.info("[{}] {}", name, cache.getMetrics())
        );
    }

    /**
     * Récupère les statistiques sous forme de rapport textuel
     *
     * @return Rapport formaté
     */
    public static String getStatsReport() {
        if (caches.isEmpty()) {
            return "No caches registered";
        }

        StringBuilder report = new StringBuilder();
        report.append(String.format("=== Cache Statistics (%d caches) ===\n", caches.size()));

        caches.forEach((name, cache) -> {
            CacheManager.CacheMetrics metrics = cache.getMetrics();
            report.append(String.format("[%s] Size: %d, %s\n",
                name, cache.size(), metrics));
        });

        return report.toString();
    }

    /**
     * Récupère tous les noms de caches enregistrés
     *
     * @return Liste immutable des noms de caches
     */
    public static List<String> getCacheNames() {
        return List.copyOf(caches.keySet());
    }

    /**
     * Nombre de caches enregistrés
     *
     * @return Nombre de caches
     */
    public static int size() {
        return caches.size();
    }

    /**
     * Vérifie si un cache est enregistré
     *
     * @param name Nom du cache
     * @return true si le cache existe
     */
    public static boolean contains(String name) {
        return caches.containsKey(name);
    }

    /**
     * Vide complètement le registre (supprime tous les caches)
     * ATTENTION: À utiliser avec précaution
     */
    public static void clear() {
        LOG.warn("Clearing all caches from registry");
        caches.clear();
    }

    /**
     * Récupère la taille totale de tous les caches
     *
     * @return Nombre total d'éléments cachés
     */
    public static int getTotalCachedElements() {
        return caches.values().stream()
            .mapToInt(CacheManager::size)
            .sum();
    }

    /**
     * Vérifie la santé globale des caches
     * Un cache est considéré sain si son hit rate > 50% après plusieurs utilisations
     *
     * @return true si tous les caches sont sains
     */
    public static boolean areAllCachesHealthy() {
        return caches.values().stream()
            .map(CacheManager::getMetrics)
            .allMatch(metrics -> {
                // Pas encore utilisé = OK
                if (metrics.getLoadCount() == 0) return true;
                // Hit rate > 50% = sain
                return metrics.getHitRate() > 50.0;
            });
    }

    /**
     * Récupère un résumé de la santé des caches
     *
     * @return Rapport de santé
     */
    public static String getHealthReport() {
        if (caches.isEmpty()) {
            return "No caches to check";
        }

        long healthyCaches = caches.values().stream()
            .map(CacheManager::getMetrics)
            .filter(m -> m.getLoadCount() == 0 || m.getHitRate() > 50.0)
            .count();

        return String.format("Cache Health: %d/%d healthy (%.1f%%)",
            healthyCaches, caches.size(),
            (double) healthyCaches / caches.size() * 100);
    }

    /**
     * Récupère les caches ayant un hit rate faible (< 30%)
     *
     * @return Liste des noms de caches problématiques
     */
    public static List<String> getLowPerformanceCaches() {
        return caches.entrySet().stream()
            .filter(entry -> {
                CacheManager.CacheMetrics metrics = entry.getValue().getMetrics();
                return metrics.getLoadCount() > 0 && metrics.getHitRate() < 30.0;
            })
            .map(entry -> entry.getKey())
            .toList();
    }
}
