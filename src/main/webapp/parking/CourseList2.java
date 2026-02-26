
package lists;

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
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import connection_package.DBConnection;
import entite.composite.ECourseList;
import utils.LCUtil;

/**
 * Service pour charger la liste des parcours actifs
 * Refactorisé pour utiliser ECourseList2 avec Builder
 */
public class CourseList2 {
    
  //  private static final String CLASSNAME = LCUtil.getCurrentClassName();
    
    // Cache statique - considérer un vrai système de cache si nécessaire
    private static List<ECourseList> cachedList = null;
    
    /**
     * Récupère la liste des parcours avec cache
     * @param conn Connexion à la base de données
     * @return Liste immutable des parcours
     */
    public List<ECourseList> list(@NotNull Connection conn) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        
        // Retourner le cache si disponible
        if (cachedList != null) {
            LOG.debug("Returning cached course list ({} items)", cachedList.size());
            return List.copyOf(cachedList);
        }
        
        LOG.debug("Loading course list from database - {}", methodName);
        LOG.debug("Loading course list method detailed = {}"  , LCUtil.getCurrentMethodNameDetailed());
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String query = """
                SELECT *
                FROM club, course, tee
                WHERE club.idclub = course.club_idclub
                    AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
                    AND tee.course_idcourse = course.idcourse
                GROUP BY idcourse, idtee
                ORDER BY clubname, coursename, idtee, teestart
                """;
            
            ps = conn.prepareStatement(query);
            LCUtil.logps(ps);
            rs = ps.executeQuery();
            
            List<ECourseList> tempList = new ArrayList<>();
            
            // Row Mappers
            RowMapper<Club> clubMapper = new ClubRowMapper();
            RowMapper<Course> courseMapper = new CourseRowMapper();
            RowMapper<Tee> teeMapper = new TeeRowMapper();
            
            // ✅ Mapping des résultats avec Builder - PLUS DE NULL !
            while (rs.next()) {
                ECourseList ecl = ECourseList.builder()
                    .club(clubMapper.map(rs))
                    .course(courseMapper.map(rs))
                    .tee(teeMapper.map(rs))
                .build();
                
                tempList.add(ecl);
            }
            
            if (tempList.isEmpty()) {
                String msg = "Empty Result List in " + methodName;
                LOG.warn(msg);
                LCUtil.showMessageFatal(msg);
                return List.of(); // Liste vide immutable
            }
            
            LOG.info("Loaded {} courses from database", tempList.size());
            
            // Mise en cache
            cachedList = tempList;
            
            return List.copyOf(cachedList);
            
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            throw e; // Re-throw pour que l'appelant gère l'erreur
            
        } catch (Exception e) {
            handleGenericException(e, methodName);
            throw new RuntimeException("Error loading course list", e);
            
        } finally {
            DBConnection.closeQuietly(null, null, rs, ps);
        }
    }
    
    /**
     * Force le rechargement du cache
     */
    public void invalidateCache() {
        LOG.debug("Invalidating course list cache");
        cachedList = null;
    }
    
    /**
     * Récupère la liste en cache (peut être null si pas encore chargée)
     */
    public static List<ECourseList> getCachedList() {
        return cachedList != null ? List.copyOf(cachedList) : null;
    }
    
    /**
     * Méthode de test principale
     */
    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        try {
            conn = new DBConnection().getConnection();
            
            CourseList2 courseList = new CourseList2();
            List<ECourseList> courses = courseList.list(conn);
            
            LOG.info("Number of courses loaded: {}", courses.size());
            
            // Affichage avec le nouveau DTO
            courses.forEach(course -> {
//                LOG.debug("Course: {}", course.getShortDescription());
//                LOG.trace("Details: {}", course.toDisplayString());
            });
            
        } catch (SQLException e) {
            LOG.error("SQL Error in main: {}", e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("Error in main: {}", e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    }
}

/*
package lists;

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
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import connection_package.DBConnection;
import entite.composite.ECourseList2;
import utils.LCUtil;


public class CourseList2 {
    
    private static final String CLASSNAME = LCUtil.getCurrentClassName();
    
    // Cache statique - considérer un vrai système de cache si nécessaire
    private static List<ECourseList2> cachedList = null;
    

    public List<ECourseList2> list(@NotNull Connection conn) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        
        // Retourner le cache si disponible
        if (cachedList != null) {
            LOG.debug("Returning cached course list ({} items)", cachedList.size());
            return List.copyOf(cachedList);
        }
        
        LOG.debug("Loading course list from database - {}", methodName);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String query = """
                SELECT *
                FROM club, course, tee
                WHERE club.idclub = course.club_idclub
                    AND course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
                    AND tee.course_idcourse = course.idcourse
                GROUP BY idcourse, idtee
                ORDER BY clubname, coursename, idtee, teestart
                """;
            
            ps = conn.prepareStatement(query);
            LCUtil.logps(ps);
            rs = ps.executeQuery();
            
            List<ECourseList2> tempList = new ArrayList<>();
            
            // Row Mappers
            RowMapper<Club> clubMapper = new ClubRowMapper();
            RowMapper<Course> courseMapper = new CourseRowMapper();
            RowMapper<Tee> teeMapper = new TeeRowMapper();
            
            // ✅ Mapping des résultats avec Builder - PLUS DE NULL !
            while (rs.next()) {
                ECourseList2 ecl = ECourseList2.builder()
                    .club(clubMapper.map(rs))
                    .course(courseMapper.map(rs))
                    .tee(teeMapper.map(rs))
                .build();
                
                tempList.add(ecl);
            }
            
            if (tempList.isEmpty()) {
                String msg = "Empty Result List in " + methodName;
                LOG.warn(msg);
                LCUtil.showMessageFatal(msg);
                return List.of(); // Liste vide immutable
            }
            
            LOG.info("Loaded {} courses from database", tempList.size());
            
            // Mise en cache
            cachedList = tempList;
            
            return List.copyOf(cachedList);
            
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            throw e; // Re-throw pour que l'appelant gère l'erreur
            
        } catch (Exception e) {
            handleGenericException(e, methodName);
            throw new RuntimeException("Error loading course list", e);
            
        } finally {
            DBConnection.closeQuietly(null, null, rs, ps);
        }
    }
    

    public void invalidateCache() {
        LOG.debug("Invalidating course list cache");
        cachedList = null;
    }
    

    public static List<ECourseList2> getCachedList() {
        return cachedList != null ? List.copyOf(cachedList) : null;
    }
    

    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        try {
            conn = new DBConnection().getConnection();
            
            CourseList2 courseList = new CourseList2();
            List<ECourseList2> courses = courseList.list(conn);
            
            LOG.info("Number of courses loaded: {}", courses.size());
            
            // Affichage avec le nouveau DTO
            courses.forEach(course -> {
//                LOG.debug("Course: {}", course.getShortDescription());
//                LOG.trace("Details: {}", course.toDisplayString());
            });
            
        } catch (SQLException e) {
            LOG.error("SQL Error in main: {}", e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("Error in main: {}", e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    }
}*/