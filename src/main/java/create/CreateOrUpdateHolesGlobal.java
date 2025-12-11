
package create;

import entite.Course;
import entite.Distance;
import entite.HolesGlobal;
import entite.Player;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import utils.LCUtil;

public class CreateOrUpdateHolesGlobal implements interfaces.Log{
  public boolean status(final HolesGlobal holesGlobal, final Tee tee, final Course course, final Player player,
            final Connection conn) throws SQLException{
 try{
         LOG.debug(" ... entering CreateOrUpdateHolesGlobal() ...");
         LOG.debug(" ... for type = ..." + holesGlobal.getType());
        boolean b = false;  
    if(holesGlobal.getType().equals("distance")){  // MAJ array distance only
                LOG.debug("handling distance");
                return handleDistance(tee, holesGlobal, conn);
      /*       Distance distance = new Distance();
             distance.setIdTee(tee.getIdtee());
                LOG.debug("input dataHoles for distance = " + Arrays.deepToString(holesGlobal.getDataHoles()));
             var v = utils.LCUtil.extractFrom2D(holesGlobal.getDataHoles(),3); //// 18 trous, 4 données : number, par, strokeindex, distance
        // https://stackoverflow.com/questions/7070576/get-one-dimensionial-array-from-a-mutlidimensional-array-in-java/7070683#7070683
          //    var v = new ArrayList<int[]>(Arrays.asList(holesGlobal.getDataHoles())).get(3);  non il extrait le 4e élément !!
                LOG.debug("input extracted from dataholes = " + Arrays.toString(v));
             distance.setDistanceArray(v);
             return new create.CreateDistances().create(distance, conn);
          // la modification UpdateDistances est lancée via catch(SQLException...) duplicate entry dans CreateDistances!!
          */
    }else{  // type = global MAJ toutes les array
         handleDistance(tee, holesGlobal, conn);
       int rows = new find.FindCountHoles().find(tee, conn);
         LOG.info("numbers of rows = " + rows);
       if(rows == 0){
             LOG.info("This is an Insert " + rows);
             return new create.CreateHolesGlobal().create(holesGlobal, tee, course, conn); // mod 15/04/2022
          //  return b;
       }else{
             LOG.info("This is a Modify " + rows);
           b = new update.UpdateHolesGlobal().update(holesGlobal, tee, conn);
           return b;
       }
         }
 //   return b;
} catch (SQLException sqle) {
            String msg = "£££ SQLException in CreateOrModifyScoreStableford = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
            
 // } catch(LCException e) {
 //      LOG.error(" -- LC Exception in " + methodName + e.getMessage());
 //   return false;              
            
 } catch (Exception e) {
            String msg = "£££ Exception in CreateOrModifyScoreStableford = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 } finally { }
} //end method
  
  private static boolean handleDistance(Tee tee, HolesGlobal holesGlobal, Connection conn){
   try{
          Distance distance = new Distance();
          distance.setIdTee(tee.getIdtee());
                LOG.debug("input dataHoles for distance = " + Arrays.deepToString(holesGlobal.getDataHoles()));
             var v = utils.LCUtil.extractFrom2D(holesGlobal.getDataHoles(),3); //// 18 trous, 4 données : number, par, strokeindex, distance
        // https://stackoverflow.com/questions/7070576/get-one-dimensionial-array-from-a-mutlidimensional-array-in-java/7070683#7070683
          //    var v = new ArrayList<int[]>(Arrays.asList(holesGlobal.getDataHoles())).get(3);  non il extrait le 4e élément !!
                LOG.debug("input extracted from dataholes = " + Arrays.toString(v));
             distance.setDistanceArray(v);
             return new create.CreateDistances().create(distance, conn);
          // la modification UpdateDistances est lancée via catch(SQLException...) duplicate entry dans CreateDistances!!

   } catch (SQLException sqle) {
            String msg = "£££ SQLException in handle distance = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
            
 // } catch(LCException e) {
 //      LOG.error(" -- LC Exception in " + methodName + e.getMessage());
 //   return false;              
            
 } catch (Exception e) {
            String msg = "£££ Exception in CreateOrModifyScoreStableford = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 } finally { }
      }

  
} //end class