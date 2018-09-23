package create;

import entite.Club;
import entite.Course;
import entite.Hole;
import entite.Tee;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class CreateHole implements Serializable,interfaces.Log
{

    public String createHole(final Club club, final Course course,
        final Tee tee, final Hole hole, List<Integer> strokeIndex, final Connection conn) throws SQLException
    {
        PreparedStatement ps = null;
        try {
            LOG.info("Course ID    = " + course.getIdcourse());
            LOG.info("tee ID    = " + tee.getIdtee());
            LOG.info("Club Name  = " + club.getClubName());
            LOG.info("ourse name  = " + course.getCourseName());
            LOG.info("hole Number = " + hole.getHoleNumber());
            LOG.info("hole Par = " + hole.getHolePar());
            LOG.info("hole Distance = " + hole.getHoleDistance());
            LOG.info("hole Index = " + hole.getHoleStrokeIndex());
            LOG.info("strokeIndex = " + strokeIndex.toString() );

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
            final String query = LCUtil.generateInsertQuery(conn, "hole"); // new 1/12/2012
            
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setShort(2, hole.getHoleNumber());
            ps.setShort(3, hole.getHolePar());
            ps.setInt(4, hole.getHoleDistance());
            ps.setShort(5, hole.getHoleStrokeIndex());
            ps.setInt(6, tee.getIdtee());
            ps.setInt(7, course.getIdcourse());
            ps.setTimestamp(8, LCUtil.getCurrentTimeStamp());
             //    String p = ps.toString();
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0)
            {
                int key = LCUtil.generatedKey(conn);
                LOG.info("Hole created = " + key);
                hole.setIdhole(key);
                String msg = "<br/><br/>Successful insert for hole = " + hole.getIdhole()
                        + " <br/> name club = " + club.getClubName()
                        + " <br/> name course = " + course.getCourseName()
                        + " <br/> Number = " + hole.getHoleNumber()
                        + " <br/> Par = " + hole.getHolePar()
                        + " <br/> Distance = " + hole.getHoleDistance()
                        + " <br/> Index = " + hole.getHoleStrokeIndex();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
        // remove dans "values" de indexNumbers : pour ne pas réutiliser le stroke index qui vient
                // d'être utilisé
                int i = hole.getHoleStrokeIndex().intValue();
  ///                  LOG.info("to be Removed stroke index value = " + i + " from strokeindex = " + strokeIndex.toString() );
                int t = strokeIndex.indexOf(i); // chercher index sur base valeur !
  ///                  LOG.info("after indexOf = " + t);
                strokeIndex.remove(t);
  ///                  LOG.debug("Remaining stroke indexes = " + strokeIndex.toString());
                // incrémenter le hole number de 1
                short s = hole.getHoleNumber();
                s++;
                if (s > 18) //new 15/01/2013
                {
                    msg = "End inserting holes !!";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
          //         CourseController.setHolesGlobal(load.LoadHoles.LoadHolesArray(conn, tee.getIdtee()));
                }
                hole.setHoleNumber(s);
                LOG.info("Next hole number = " + hole.getHoleNumber());
                // réinitialise some fields
                hole.setHolePar((short) 4);
                hole.setHoleDistance((short) 0);
                hole.setHoleStrokeIndex((short) 0);
                return "hole.xhtml?faces-redirect=true";   // pourquoi ce return ??
            }else{
                String msg = "<br/><br/>NOT NOT inserting hole // 19 reached = " + hole.getIdhole()
                        + " <br/> name club = " + club.getClubName()
                        + " <br/> name course = " + course.getCourseName()
                        + " <br/> Number = " + hole.getHoleNumber()
                        + " <br/> Par = " + hole.getHolePar()
                        + " <br/> Distance = " + hole.getHoleDistance()
                        + " <br/> Index = " + hole.getHoleStrokeIndex();
                LOG.info(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            }
        } catch (ArrayIndexOutOfBoundsException ob) {
            String msg = "£££ index out of bounds = if from <0 or from > original.length() " + ob.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } catch (NullPointerException npe) {
            String msg = "£££ NullPointerException in insert hole = " + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } catch (SQLException sqle) {
            String msg = "SQL exception in Insert hole = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } catch (NumberFormatException nfe) {
            String msg = "NumberFormatException in Insert hole = " + nfe.toString();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
            
          //  DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
             DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
    } //end createHole
} //end class