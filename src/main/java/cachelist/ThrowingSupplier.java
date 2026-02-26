package cachelist;

/**
 * Interface fonctionnelle pour les loaders qui peuvent lancer des exceptions
 * Alternative à Supplier qui ne supporte que RuntimeException
 *
 * Cette interface permet de passer des lambdas qui lancent des checked exceptions
 * (comme SQLException, IOException, etc.) sans avoir à les wrapper manuellement.
 *
 * Exemple d'utilisation :
 * <pre>
 * {@code
 * ThrowingSupplier<List<Player>> loader = () -> loadFromDatabase(conn);
 * List<Player> result = cacheManager.get(loader);
 * }
 * </pre>
 *
 * @param <T> Type de retour
 * @author Votre nom
 * @version 1.0
 * @since 2025-01-18
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    
    /**
     * Obtient un résultat
     *
     * @return Le résultat
     * @throws Exception si une erreur survient pendant l'exécution
     */
    T get() throws Exception;
}
