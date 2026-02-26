package lists;

import cachelist.CacheManager;
import cachelist.CacheManager.CacheConfig;
import cachelist.CacheRegistry;
import entite.Club;
import entite.Course;
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import entite.composite.ECourseList;
import utils.LCUtil;

/**
 * Service pour charger la liste des parcours actifs
 * Version 3.0 avec système de cache générique, pagination, filtrage et metrics
 *
 * Fonctionnalités :
 * - Cache générique thread-safe avec TTL configurable
 * - Pagination des résultats
 * - Filtrage avancé (par club, parcours, prédicat custom)
 * - Metrics détaillées (hit rate, temps de chargement, etc.)
 * - Try-with-resources pour gestion automatique des ressources
 * - Enregistrement dans le CacheRegistry global
 *
 * @author Votre nom
 * @version 3.0
 * @since 2025-01-18
 */
public class CourseList3 {

    private static final String CLASSNAME = LCUtil.getCurrentClassName();
    private static final String CACHE_NAME = "CourseList3";

    // Query SQL optimisée
    private static final String COURSE_QUERY = """
        SELECT *
        FROM club, course, tee
        WHERE club.idclub = course.club_idclub
            AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
            AND tee.course_idcourse = course.idcourse
        GROUP BY idcourse, idtee
        ORDER BY clubname, coursename, idtee, teestart
        """;

    // Cache manager générique
    private final CacheManager<ECourseList> cacheManager;

    // ========================================================================
    // CONSTRUCTEURS
    // ========================================================================

    /**
     * Constructeur avec configuration de cache personnalisée
     *
     * @param cacheConfig Configuration du cache (TTL, metrics, etc.)
     */
    public CourseList3(CacheConfig cacheConfig) {
        this.cacheManager = new CacheManager<>(CACHE_NAME, cacheConfig);
        CacheRegistry.register(CACHE_NAME, cacheManager);
        LOG.debug("CourseList3 initialized with custom cache config");
    }

    /**
     * Constructeur par défaut (TTL: 1 heure)
     */
    public CourseList3() {
        this(CacheConfig.defaultConfig());
        LOG.debug("CourseList3 initialized with default cache config (TTL: 1h)");
    }

    /**
     * Constructeur avec TTL personnalisé
     *
     * @param ttl Durée de vie du cache
     */
    public CourseList3(Duration ttl) {
        this(CacheConfig.withTTL(ttl));
        LOG.debug("CourseList3 initialized with TTL: {}", ttl);
    }

    // ========================================================================
    // CLASSES INTERNES - PAGINATION
    // ========================================================================

    /**
     * Classe pour les paramètres de pagination
     */
    public static class PageRequest {
        private final int pageNumber;
        private final int pageSize;

        /**
         * Crée une requête de pagination
         *
         * @param pageNumber Numéro de page (commence à 0)
         * @param pageSize Taille de la page (nombre d'éléments)
         */
        public PageRequest(int pageNumber, int pageSize) {
            if (pageNumber < 0) {
                throw new IllegalArgumentException("Page number must be >= 0");
            }
            if (pageSize <= 0) {
                throw new IllegalArgumentException("Page size must be > 0");
            }
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
        }

        public int getPageNumber() { return pageNumber; }
        public int getPageSize() { return pageSize; }
        public int getOffset() { return pageNumber * pageSize; }

        @Override
        public String toString() {
            return String.format("PageRequest[page=%d, size=%d]", pageNumber, pageSize);
        }
    }

    /**
     * Classe pour le résultat paginé
     */
    public static class PageResult<T> {
        private final List<T> content;
        private final int pageNumber;
        private final int pageSize;
        private final long totalElements;
        private final int totalPages;

        public PageResult(List<T> content, int pageNumber, int pageSize, long totalElements) {
            this.content = List.copyOf(content);
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        }

        public List<T> getContent() { return content; }
        public int getPageNumber() { return pageNumber; }
        public int getPageSize() { return pageSize; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public boolean hasNext() { return pageNumber < totalPages - 1; }
        public boolean hasPrevious() { return pageNumber > 0; }
        public boolean isEmpty() { return content.isEmpty(); }
        public boolean isFirst() { return pageNumber == 0; }
        public boolean isLast() { return pageNumber == totalPages - 1; }

        @Override
        public String toString() {
            return String.format("Page %d/%d (%d items, %d total)",
                pageNumber + 1, totalPages, content.size(), totalElements);
        }
    }

    // ========================================================================
    // MÉTHODES PRINCIPALES - RÉCUPÉRATION DE DONNÉES
    // ========================================================================

    /**
     * Récupère la liste complète des parcours avec cache
     *
     * @param conn Connexion à la base de données (non-null)
     * @return Liste immutable des parcours (jamais null)
     * @throws SQLException si erreur de base de données
     * @throws IllegalArgumentException si conn est null
     */
    public List<ECourseList> list(@NotNull Connection conn) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }

        try {
            // Délégation au cache manager - supporte maintenant les checked exceptions!
            return cacheManager.get(() -> loadFromDatabase(conn));

        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error loading course list", e);
        }
    }

    /**
     * Charge les données depuis la base de données
     * Cette méthode est appelée par le cache manager uniquement en cas de cache miss
     *
     * @param conn Connexion à la base de données
     * @return Liste des parcours chargés depuis la DB
     * @throws SQLException si erreur SQL
     */
    private List<ECourseList> loadFromDatabase(Connection conn) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();

        try (PreparedStatement ps = conn.prepareStatement(COURSE_QUERY);
             ResultSet rs = ps.executeQuery()) {

            LCUtil.logps(ps);

            List<ECourseList> tempList = mapResultSet(rs);

            if (tempList.isEmpty()) {
                handleEmptyResult(methodName);
                return List.of();
            }

            LOG.debug("Loaded {} courses from database", tempList.size());
            return tempList;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            throw e;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            throw new RuntimeException("Database error", e);
        }
    }

    /**
     * Map le ResultSet vers une liste d'ECourseList2
     * Extraction de méthode pour respecter le principe de responsabilité unique
     *
     * @param rs ResultSet à mapper
     * @return Liste des objets mappés
     * @throws SQLException si erreur de mapping
     */
    private List<ECourseList> mapResultSet(ResultSet rs) throws SQLException {
        List<ECourseList> list = new ArrayList<>();

        // Initialisation des mappers (réutilisables pour toutes les lignes)
        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapper<Tee> teeMapper = new TeeRowMapper();

        while (rs.next()) {
            ECourseList ecl = ECourseList.builder()
                .club(clubMapper.map(rs))
                .course(courseMapper.map(rs))
                .tee(teeMapper.map(rs))
                .build();

            list.add(ecl);
        }

        return list;
    }

    /**
     * Gère le cas d'un résultat vide
     *
     * @param methodName Nom de la méthode appelante (pour logging)
     */
    private void handleEmptyResult(String methodName) {
        String msg = "Empty Result List in " + methodName;
        LOG.warn(msg);
        LCUtil.showMessageFatal(msg);
    }

    // ========================================================================
    // PAGINATION
    // ========================================================================

    /**
     * Récupère une page de parcours
     *
     * @param conn Connexion à la base de données
     * @param pageRequest Paramètres de pagination
     * @return Page de résultats avec métadonnées
     * @throws SQLException si erreur de base de données
     */
    public PageResult<ECourseList> listPaginated(@NotNull Connection conn, PageRequest pageRequest)
            throws SQLException {

        List<ECourseList> allCourses = list(conn);

        int offset = pageRequest.getOffset();
        int limit = pageRequest.getPageSize();

        List<ECourseList> pageContent = allCourses.stream()
            .skip(offset)
            .limit(limit)
            .collect(Collectors.toList());

        return new PageResult<>(pageContent, pageRequest.getPageNumber(),
            pageRequest.getPageSize(), allCourses.size());
    }

    // ========================================================================
    // FILTRAGE
    // ========================================================================

    /**
     * Filtre les parcours selon un prédicat personnalisé
     *
     * @param conn Connexion à la base de données
     * @param filter Prédicat de filtrage
     * @return Liste filtrée des parcours
     * @throws SQLException si erreur de base de données
     */
    public List<ECourseList> listFiltered(@NotNull Connection conn,
            Predicate<ECourseList> filter) throws SQLException {

        return list(conn).stream()
            .filter(filter)
            .collect(Collectors.toList());
    }

    /**
     * Filtre les parcours par nom de club (recherche insensible à la casse)
     *
     * @param conn Connexion à la base de données
     * @param clubName Nom du club (ou partie du nom)
     * @return Liste des parcours correspondants
     * @throws SQLException si erreur de base de données
     */
    public List<ECourseList> listByClubName(@NotNull Connection conn, String clubName)
            throws SQLException {

        if (clubName == null || clubName.isBlank()) {
            return List.of();
        }

        return listFiltered(conn, course ->
            course.club() != null &&
            course.club().getClubName() != null &&
            course.club().getClubName().toLowerCase().contains(clubName.toLowerCase())
        );
    }

    /**
     * Filtre les parcours par nom de parcours (recherche insensible à la casse)
     *
     * @param conn Connexion à la base de données
     * @param courseName Nom du parcours (ou partie du nom)
     * @return Liste des parcours correspondants
     * @throws SQLException si erreur de base de données
     */
    public List<ECourseList> listByCourseName(@NotNull Connection conn, String courseName)
            throws SQLException {

        if (courseName == null || courseName.isBlank()) {
            return List.of();
        }

        return listFiltered(conn, course ->
            course.course() != null &&
            course.course().getCourseName() != null &&
            course.course().getCourseName().toLowerCase().contains(courseName.toLowerCase())
        );
    }

    /**
     * Filtre les parcours qui ont toutes les données essentielles
     *
     * @param conn Connexion à la base de données
     * @return Liste des parcours complets
     * @throws SQLException si erreur de base de données
     */
    public List<ECourseList> listWithEssentialData(@NotNull Connection conn) throws SQLException {
        return listFiltered(conn, ECourseList::hasEssentialData);
    }

    // ========================================================================
    // MÉTHODES DÉLÉGUÉES AU CACHE MANAGER
    // ========================================================================

    /**
     * Invalide le cache (force le rechargement au prochain appel)
     */
    public void invalidateCache() {
        cacheManager.invalidate();
        LOG.debug("Cache invalidated for {}", CACHE_NAME);
    }

    /**
     * Réinitialise le cache et les metrics
     */
    public void resetCache() {
        cacheManager.reset();
        LOG.debug("Cache reset for {}", CACHE_NAME);
    }

    /**
     * Récupère la liste en cache sans recharger
     *
     * @return Optional contenant la liste si en cache, sinon empty
     */
    public Optional<List<ECourseList>> getCachedList() {
        return cacheManager.getCached();
    }

    /**
     * Vérifie si le cache est chargé et valide
     *
     * @return true si le cache contient des données valides
     */
    public boolean isCacheLoaded() {
        return cacheManager.isLoaded();
    }

    /**
     * Récupère la taille du cache (nombre d'éléments)
     *
     * @return Nombre d'éléments en cache, ou 0 si vide
     */
    public int getCacheSize() {
        return cacheManager.size();
    }

    /**
     * Récupère les metrics du cache
     *
     * @return Objet CacheMetrics avec statistiques détaillées
     */
    public CacheManager.CacheMetrics getMetrics() {
        return cacheManager.getMetrics();
    }

    /**
     * Récupère le nom du cache
     *
     * @return Nom du cache
     */
    public String getCacheName() {
        return CACHE_NAME;
    }

    // ========================================================================
    // MÉTHODE DE TEST
    // ========================================================================

    /**
     * Méthode de test principale avec démonstration complète des fonctionnalités
     */
    public static void main(String[] args) {
        LOG.info("=== CourseList3 - Test Suite ===\n");

     try (Connection conn = connection_package.DBConnection2.getConnection()) {
            // Créer un service avec cache de 30 minutes
            CourseList3 courseList = new CourseList3(Duration.ofMinutes(30));
            LOG.info("Service created with 30-minute cache TTL\n");

            // === TEST 1: Chargement initial ===
            LOG.info("=== TEST 1: Initial Load ===");
            List<ECourseList> courses = courseList.list(conn);
            LOG.info("Loaded {} courses", courses.size());
            LOG.info("Metrics: {}\n", courseList.getMetrics());

            // === TEST 2: Cache hit ===
            LOG.info("=== TEST 2: Cache Hit ===");
            List<ECourseList> cachedCourses = courseList.list(conn);
            LOG.info("Retrieved {} courses from cache", cachedCourses.size());
            LOG.info("Metrics: {}\n", courseList.getMetrics());

            // === TEST 3: Pagination ===
            LOG.info("=== TEST 3: Pagination ===");
            PageRequest pageRequest = new PageRequest(0, 10);
            PageResult<ECourseList> page = courseList.listPaginated(conn, pageRequest);
            LOG.info("Page result: {}", page);
            LOG.info("Has next: {}, Has previous: {}", page.hasNext(), page.hasPrevious());

            page.getContent().stream()
                .limit(3)
                .forEach(course ->
                    LOG.debug("  - Course: {}", course.course() != null ?
                        course.course().getCourseName() : "N/A")
                );
            LOG.info("");

            // === TEST 4: Filtrage par club ===
            LOG.info("=== TEST 4: Filter by Club Name ===");
            List<ECourseList> filteredByClub = courseList.listByClubName(conn, "golf");
            LOG.info("Found {} courses matching 'golf'\n", filteredByClub.size());

            // === TEST 5: Filtrage par parcours ===
            LOG.info("=== TEST 5: Filter by Course Name ===");
            List<ECourseList> filteredByCourse = courseList.listByCourseName(conn, "blue");
            LOG.info("Found {} courses matching 'blue'\n", filteredByCourse.size());

            // === TEST 6: Filtrage données essentielles ===
            LOG.info("=== TEST 6: Filter by Essential Data ===");
            List<ECourseList> withEssentialData = courseList.listWithEssentialData(conn);
            LOG.info("Found {} courses with essential data\n", withEssentialData.size());

            // === TEST 7: Filtrage custom ===
            LOG.info("=== TEST 7: Custom Filter ===");
            List<ECourseList> customFiltered = courseList.listFiltered(conn,
                course -> course.hasEssentialData() && course.tee() != null
            );
            LOG.info("Found {} courses with essential data and tee\n", customFiltered.size());

            // === TEST 8: Statistiques globales ===
            LOG.info("=== TEST 8: Global Cache Statistics ===");
            CacheRegistry.printAllStats();
            LOG.info("");

            // === TEST 9: Invalidation et rechargement ===
            LOG.info("=== TEST 9: Cache Invalidation ===");
            courseList.invalidateCache();
            LOG.info("Cache invalidated");
            List<ECourseList> reloaded = courseList.list(conn);
            LOG.info("Reloaded {} courses", reloaded.size());
            LOG.info("Metrics after reload: {}\n", courseList.getMetrics());

            // === TEST 10: Statistiques finales ===
            LOG.info("=== TEST 10: Final Statistics ===");
            LOG.info("Cache name: {}", courseList.getCacheName());
            LOG.info("Cache size: {}", courseList.getCacheSize());
            LOG.info("Cache loaded: {}", courseList.isCacheLoaded());
            LOG.info("Metrics: {}", courseList.getMetrics());

            LOG.info("\n=== Global Registry Info ===");
            LOG.info("Registered caches: {}", CacheRegistry.getCacheNames());
            LOG.info("Total caches: {}", CacheRegistry.size());
            LOG.info("Total cached elements: {}", CacheRegistry.getTotalCachedElements());
            LOG.info(CacheRegistry.getHealthReport());

        } catch (SQLException e) {
            LOG.error("SQL Error: {}", e.getMessage(), e);
            System.exit(1);
        } catch (Exception e) {
            LOG.error("Error: {}", e.getMessage(), e);
            System.exit(1);
        }

        LOG.info("\n=== Test Suite Completed ===");
    }
}