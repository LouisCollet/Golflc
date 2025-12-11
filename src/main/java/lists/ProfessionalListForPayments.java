package lists;

import entite.Club;
import entite.composite.EPaymentPro;
import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

// ??vérifier si un player est aussi un pro : retourner les liste des clubs où il est pro

@Named("ProPayment")
@ViewScoped // nécessaire !! pour faire le total dans local_administrator_cotisations.xhtml

public class ProfessionalListForPayments implements Serializable{
    private static List<EPaymentPro> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public List<EPaymentPro> list(final Player pro, final Connection conn) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
       LOG.debug("entering " + methodName);
       LOG.debug("with Professional = " + pro);
    PreparedStatement ps = null;
    ResultSet rs = null;
 try{
 /*    final String query = """
        SELECT *
        FROM payments_lesson, professional
        WHERE professional.ProplayerId = ?
	GROU P BY idLesson
	ORDER BY LessonIdClub, LessonStartDate DESC;
""";
 next = 10-10-2024*/  
  final String query = """
    WITH selection AS (
        SELECT *
        FROM professional
        WHERE professional.ProplayerId = ?
    )
    SELECT * FROM selection
    JOIN payments_lesson
        ON payments_lesson.LessonIdPro = selection.ProId
    ORDER BY payments_lesson.LessonIdClub, payments_lesson.LessonStartDate DESC;
   """;
  
     
   /* 	      SELECT *
         FROM payments_lesson, professional
         WHERE professional.ProplayerId = 324715
 			GROU P BY idLesson
			ORDER BY LessonIdClub,LessonStartDate DESC;
     AND club.idclub = professional.ProClubId
     GROU P BY idLesson
			ORDER BY LessonIdClub,LessonStartDate DESC;
                 SELECT *
            FROM payments_cotisation, club, player
            WHERE club.ClubLocalAdmin = ?
              AND payments_cotisation.CotisationIdClub = club.idclub
              AND player.idplayer = cotisationIdPlayer
            ORDER BY cotisationIdclub, playerlastname
   */  

     ps = conn.prepareStatement(query);
     ps.setInt(1, pro.getIdplayer());
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
     liste = new ArrayList<>();
     while(rs.next()){
         EPaymentPro ecp = new EPaymentPro();
         ecp.setProfessional(entite.Professional.map(rs));
         ecp.setLessonPayment(entite.LessonPayment.map(rs));
      // filling club
         Club club = new Club();
         club.setIdclub(ecp.getLessonPayment().getPaymentIdClub()); // from payment
         ecp.setClub(new read.ReadClub().read(club, conn));
      // filling student  
         Player student = new Player();
         student.setIdplayer(ecp.getLessonPayment().getPaymentIdStudent()); // from lesson
      //   student = new read.ReadPlayer().read(student, conn);
         ecp.setStudent(new read.ReadPlayer().read(student, conn));
         liste.add(ecp);
     } // end while
     if(liste.isEmpty()){
         String error = "££ Empty Result Table in " + methodName;
         LOG.error(error);
    //     LCUtil.showMessageFatal(error);
  //       return liste;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
   liste.forEach(item -> LOG.debug("Players list with student name = " + item.getStudent().getPlayerLastName()
                    + " /paymentIdClub " + item.getLessonPayment().getPaymentIdClub()
                    + " /idclub =  " + item.getClub().getIdclub()
   ));
return liste;
} catch(SQLException sqle){
    String msg = "£££ SQL exception in " + methodName + "/" + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception e){
    String msg = "£££ Exception in " + methodName + " / " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
  //   LOG.debug("escaped to " + methodName + " repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
   //then you should introduce lazy loading inside the getter method. I.e. if the property is null,
    //then load and assign it to the property, else return it.
} //end if
} //end method

    public static List<EPaymentPro> getListe() {
        return liste;
    }

    public static void setListe(List<EPaymentPro> liste) {
        ProfessionalListForPayments.liste = liste;
    }
    
 // void main() throws SQLException, Exception {
  public static void main(String args[])throws SQLException, Exception{        
    Connection conn = new utils.DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324715);
    
    var prof = new ProfessionalListForPayments().list(player,conn);
        LOG.debug("list Pro for payments size = " + prof.size());
  //  prof.forEach(item -> LOG.debug("Club(s) list for a Pro " + item));
    utils.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class