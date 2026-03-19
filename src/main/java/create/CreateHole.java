package create;

import entite.Hole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service de création de Hole
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Resource DataSource - Connection pooling
 */
@ApplicationScoped
public class CreateHole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * DataSource injecté par WildFly (connection pooling)
     */
    @Inject private dao.GenericDAO dao;

    public boolean create(final Hole hole) throws Exception {
        
        final String methodName = LCUtil.getCurrentMethodName();
        String msg;
        
        try (Connection conn = dao.getConnection()) {
            
            conn.setAutoCommit(false);
            
            // ✅ PARTIE NON MODIFIÉE - DÉBUT
            
            // Validation
            if (hole == null) {
                msg = "Hole cannot be null";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (hole.getTee_course_idcourse() == 0 || hole.getTee_course_idcourse() == 0) {
                msg = "Hole must be associated with a course";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (hole.getHoleNumber() == null || hole.getHoleNumber() < 1 || hole.getHoleNumber() > 18) {
                msg = "Hole number must be between 1 and 18";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            LOG.debug("Creating hole #{}: {}", hole.getHoleNumber(), hole);
            
            String query = LCUtil.generateInsertQuery(conn, "hole");
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdateHole.mapCreate(ps, hole); // MapCreate(ps, hole);
                LCUtil.logps(ps);
                
                int row = ps.executeUpdate();
                
                if (row == 0) {
                    msg = "Fatal Error: No row inserted";
                    LOG.error(msg);
                    throw new SQLException(msg);
                }
            }
            
            int generatedId = LCUtil.generatedKey(conn);
            hole.setIdhole(generatedId);
            
            msg = String.format("Hole #%d created (ID: %d)", 
                               hole.getHoleNumber(), 
                               hole.getIdhole());
            LOG.debug(msg);
            
            conn.commit();
            
            // ✅ PARTIE NON MODIFIÉE - FIN
            
            return true;
            
        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            LOG.error("SQLException in {}: {}", methodName, sqle.getMessage());
            throw sqle;
        } catch (Exception e) {
            LOG.error("Exception in {}: {}", methodName, e.getMessage());
            throw e;
        }
    }
}
/*
import entite.Club;
import entite.Course;
import entite.Hole;
import entite.Tee;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import connection_package.DBConnection;
import static utils.LCUtil.generateInsertQuery;
//import utils.LCUtil;
import static utils.LCUtil.generatedKey;
import static utils.LCUtil.logps;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class CreateHole implements Serializable,interfaces.Log{

 public boolean create(final Club club,
            final Course course,
            final Tee tee,
            final Hole hole,
            List<Integer> strokeIndex,
            final Connection conn) throws SQLException{
        PreparedStatement ps = null;
   try {
            LOG.debug("Course ID    = " + course.getIdcourse());
            LOG.debug("tee ID    = " + tee.getIdtee());
            LOG.debug("Club Name  = " + club.getClubName());
            LOG.debug("Course name  = " + course.getCourseName());
            LOG.debug("hole Number = " + hole.getHoleNumber());
            LOG.debug("hole Par = " + hole.getHolePar());
            LOG.debug("hole Distance = " + hole.getHoleDistance());
            LOG.debug("hole Index = " + hole.getHoleStrokeIndex());
            LOG.debug("strokeIndex = " + strokeIndex.toString() );

    // new 28/12/2012 You can temporary disable foreign key checks in MySQL to perform operations
            //that would fail if these checks were enabled:
// Disable foreign keys check
//Statement stmt = conn.createStatement();
//stmt.execute("SET FOREIGN_KEY_CHECKS=0");
//stmt.close();
// Do your stuff
// Enable foreign keys check
//Statement stmt = conn.createStatement();
//stmt.execute("SET FOREIGN_KEY_CHECKS=1");
//stmt.close();
//Note that this is a per connection setting so you have to do all
//your stuff using the same conn object.
            final String query = generateInsertQuery(conn, "hole"); // new 1/12/2012
            
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setShort(2, hole.getHoleNumber());
            ps.setShort(3, hole.getHolePar());
            ps.setInt(4, hole.getHoleDistance());
            ps.setShort(5, hole.getHoleStrokeIndex());
            ps.setInt(6, tee.getIdtee());
            ps.setInt(7, course.getIdcourse());
        //    ps.setTimestamp(8, LCUtil.getCurrentTimeStamp());
            ps.setTimestamp(8,Timestamp.from(Instant.now())); // mod 18-02-2020
             //    String p = ps.toString();
            logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0){
                 hole.setIdhole(generatedKey(conn));
                        LOG.debug("Hole created = " + hole.getIdhole());
                String msg = "<br/><br/>Successful insert for hole = " + hole.getIdhole()
                        + " </h1><br/> name club = " + club.getClubName()
                        + " <br/> name course = " + course.getCourseName()
                        + " <br/> Number = " + hole.getHoleNumber()
                        + " <br/> Par = " + hole.getHolePar()
                        + " <br/> Distance = " + hole.getHoleDistance()
                        + " <br/> Index = " + hole.getHoleStrokeIndex();
                LOG.debug(msg);
                showMessageInfo(msg);
        // remove dans "values" de indexNumbers : pour ne pas réutiliser le stroke index qui vient d'être utilisé
                int i = hole.getHoleStrokeIndex().intValue();
  ///                  LOG.debug("to be Removed stroke index value = " + i + " from strokeindex = " + strokeIndex.toString() );
                int t = strokeIndex.indexOf(i); // chercher index sur base valeur !
  ///                  LOG.debug("after indexOf = " + t);
                strokeIndex.remove(t);
  ///                  LOG.debug("Remaining stroke indexes = " + strokeIndex.toString());
                // incrémenter le hole number de 1
                short s = hole.getHoleNumber();
                s++;
                if (s > 18){ //new 15/01/2013
                    msg = "End inserting holes !!";
                    LOG.debug(msg);
                    showMessageInfo(msg);
                }
                hole.setHoleNumber(s);
                LOG.debug("Next hole number = " + hole.getHoleNumber());
     // réinitialise some fields
                hole.setHolePar((short) 4);
                hole.setHoleDistance((short) 0);
                hole.setHoleStrokeIndex((short) 0);
                return true;
            }else{
                String msg = "<br/><br/>NOT NOT inserting hole // 19 reached = " + hole.getIdhole()
                        + " <br/> name club = " + club.getClubName()
                        + " <br/> name course = " + course.getCourseName()
                        + " <br/> Number = " + hole.getHoleNumber()
                        + " <br/> Par = " + hole.getHolePar()
                        + " <br/> Distance = " + hole.getHoleDistance()
                        + " <br/> Index = " + hole.getHoleStrokeIndex();
                LOG.debug(msg);
                showMessageFatal(msg);
                //return null;
                return false;
            }
        } catch (SQLException sqle) {
            String msg = "SQL exception in Insert hole = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return false; //return null;
        } catch (Exception nfe) {
            String msg = "Exception in CreateHole = " + nfe.toString();
            LOG.error(msg);
            showMessageFatal(msg);
            return false; //return null;
        } finally {
             DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
    } //end create
} //end class
*/