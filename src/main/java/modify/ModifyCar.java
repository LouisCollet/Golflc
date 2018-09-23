
package modify;

//import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import entite.Car;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyCar implements interfaces.Log
{
    PreparedStatement ps = null;
////    static Savepoint savepoint1 = null;
    public boolean modifyCar(final Car car, final Connection conn) throws SQLException
    {
            LOG.info("connection = " + conn);
        PreparedStatement ps = null;
        try {
            LOG.info("...entering modifyCar");
            LOG.info("car ID  = " + car.getId() );
            LOG.info("new Year  = " + car.getYear() );
            LOG.info("new Brand  = " + car.getBrand() );
            LOG.info("newcColor  = " + car.getColor() );

      final String query =
          "UPDATE car "
           + " set CarYear=?, CarBrand=?, CarColor=? "
           + " WHERE idcar=? "
          ;
      ps = conn.prepareStatement(query);
//set a Savepoint
////    savepoint1 = conn.setSavepoint("Savepoint1");
            ps.setInt(1,car.getYear() );
            ps.setString(2, car.getBrand() );
            ps.setString(3, car.getColor() ); 
            ps.setString(4, car.getId() ); 
             // insérer dans l'ordre de la database : 1 = first db field
   //         ps.setTimestamp(6, LCUtil.getCurrentTimeStamp());
             //    String p = ps.toString();
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate(); // write into database

            if (row != 0) {
                String msg = "<br/><br/>Succesfull update for car : "
                        + car.getId()
                        + "<br/>car year = " + car.getYear()
                        + "<br/>car brand = " + car.getBrand()
                        + "<br/>car color = " + car.getColor();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                // If there is no error, commit the changes.
       //          conn.commit();

                return true;
            } else { // = 0
            //    The output will be in the form of int which denotes the number of rows affected by the query.
                String msg = "<br/><br/>NOT NOT Succesfull Update for car : "
                        + car.getId()
                        + "<br/>car year = " + car.getYear()
                        + "<br/>car brand = " + car.getBrand()
                        + "<br/>car color = " + car.getColor();
                LOG.info(msg);
                LCUtil.showMessageFatal(msg);
                // If there is any error.
  //              conn.rollback(savepoint1);

                return false;
            }
//        } catch (MySQLIntegrityConstraintViolationException cv) {
//            String msg = "££££ MySQLIntegrityConstraintViolationException in modify Car = " + cv.getMessage();
//            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
            // If there is any error.
   //         conn.rollback(savepoint1);

   //          return false;
        } catch (SQLException sqle) {
            //LOG.error("-- SQLException in Insert Course " + sqle.toString());
            String msg = "SQLException in Modify Car = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            // If there is any error.
   //         conn.rollback(savepoint1);

            return false;

        } catch (NumberFormatException nfe) {
            String msg = "££££ NumberFormatException in Modify Car = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            // If there is any error.
   //         conn.rollback(savepoint1);

             return false;
        } catch (Exception e) {
            String msg = "££££ Exception in Modify Car = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            // If there is any error.
    //        conn.rollback(savepoint1);

            return false;
        } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 10/12/2011
        }
    } //end method

}
