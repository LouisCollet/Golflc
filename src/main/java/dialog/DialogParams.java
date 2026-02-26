
package dialog;

// ========================================

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DialogParams - Utilitaire pour paramètres
// ========================================

/**
 * Classe utilitaire pour la création de paramètres de dialogue.
 * <p>
 * Fournit des méthodes pratiques pour construire les maps de paramètres
 * attendues par le Dialog Framework de PrimeFaces.
 * </p>
 */
public final class DialogParams {
    
    private DialogParams() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }
    
    /**
     * Crée une map de paramètres avec une seule entrée.
     *
     * @param key la clé du paramètre
     * @param value la valeur du paramètre
     * @return une map immuable contenant le paramètre
     * @throws IllegalArgumentException si key ou value est null
     */
    public static Map<String, List<String>> single(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Parameter key cannot be null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Parameter value cannot be null");
        }
        
        return Collections.singletonMap(key, Collections.singletonList(value));
    }
    
    /**
     * Crée une map de paramètres avec plusieurs valeurs pour une clé.
     *
     * @param key la clé du paramètre
     * @param values les valeurs du paramètre
     * @return une map immuable contenant les paramètres
     * @throws IllegalArgumentException si key ou values est null
     */
    public static Map<String, List<String>> multiple(String key, List<String> values) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Parameter key cannot be null or empty");
        }
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Parameter values cannot be null or empty");
        }
        
        return Collections.singletonMap(key, Collections.unmodifiableList(values));
    }
    
    /**
     * Crée un builder pour construire des paramètres multiples.
     *
     * @return un nouveau builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder pour créer des maps de paramètres complexes.
     */
    public static class Builder {
        private final Map<String, List<String>> params = new HashMap<>();
        
        /**
         * Ajoute un paramètre avec une seule valeur.
         *
         * @param key la clé du paramètre
         * @param value la valeur du paramètre
         * @return ce builder
         */
        public Builder add(String key, String value) {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Parameter key cannot be null or empty");
            }
            if (value == null) {
                throw new IllegalArgumentException("Parameter value cannot be null");
            }
            
            params.put(key, Collections.singletonList(value));
            return this;
        }
        
        /**
         * Ajoute un paramètre avec plusieurs valeurs.
         *
         * @param key la clé du paramètre
         * @param values les valeurs du paramètre
         * @return ce builder
         */
        public Builder add(String key, List<String> values) {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Parameter key cannot be null or empty");
            }
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("Parameter values cannot be null or empty");
            }
            
            params.put(key, Collections.unmodifiableList(values));
            return this;
        }
        
        /**
         * Construit la map de paramètres.
         *
         * @return une map immuable contenant tous les paramètres
         */
        public Map<String, List<String>> build() {
            return Collections.unmodifiableMap(new HashMap<>(params));
        }
    }
}
