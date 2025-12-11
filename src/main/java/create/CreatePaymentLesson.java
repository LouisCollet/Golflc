package create;

import entite.Creditcard;
import entite.Lesson;
import entite.Professional;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;

public class CreatePaymentLesson implements interfaces.Log, interfaces.GolfInterface{
    // creditcard ?
    private static int generatedKey;
 public boolean create(
         final Lesson lesson,
         final Creditcard creditcard, final Professional professional, final Connection conn) throws SQLException, InstantiationException{
      PreparedStatement ps = null;
 try {
                LOG.debug("...entering createPaymentLesson");
                LOG.debug("lesson  = " + lesson);
                LOG.debug("creditcard  = " + creditcard);
                LOG.debug("professional  = " + professional);
            final String query = LCUtil.generateInsertQuery(conn, "payments_lesson"); 
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER); // AUTO-INCREMENT
            ps.setInt(2, professional.getProId());
            ps.setInt(3, professional.getProClubId());
            // à vérifier !!
            ps.setInt(4, creditcard.getCreditCardIdPlayer()); // mod 31-01-202 3getIdplayer()); // aussi dans event !
            ps.setTimestamp(5,Timestamp.valueOf(lesson.getEventStartDate()));
            ps.setTimestamp(6,Timestamp.valueOf(lesson.getEventEndDate()));
            ps.setString(7, creditcard.getCreditcardPaymentReference()); 
            ps.setString(8, creditcard.getCommunication()); 
            ps.setDouble(9, creditcard.getTotalPrice());
            
            ps.setTimestamp(10, Timestamp.from(Instant.now())); 
            
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0){
                int genratedKey = LCUtil.generatedKey(conn);
                    LOG.debug("payment lesson, genrated key = " + generatedKey);

                String msg = "Payment Lesson  created = </h1>" + lesson; // + cotisation.getPrice(); 
                LOG.debug(msg);
         //       LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/><br/>ERROR insert for lesson : " ;
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
        }catch (SQLException sqle) {
            String msg = "";
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 ){
                 msg = LCUtil.prepareMessageBean("create.lesson.duplicate") + lesson;
       //                  + NEW_LINE + cotisation
        //         + " player = " + cotisation.getIdplayer() + " club = " + cotisation.getIdclub();
            }else{
             msg = "SQLException in createPaymentLesson = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            }
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in createPaymentLesson = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
   //     return true;
    } //end method

    public static int getGeneratedKey() {
        return generatedKey;
    }

  //  public static void setGeneratedKey(int generatedKey) {
 //       CreatePaymentLesson.generatedKey = generatedKey;
  //  }

} //end Class