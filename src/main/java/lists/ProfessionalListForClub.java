package lists;

import entite.composite.EClubPro;
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

// vérifier si un player est aussi un pro : retourner les liste des clubs où il est pro

@Named()
@ViewScoped // ?? nécessaire !! pour faire le total dans local_administrator_cotisations.xhtml

public class ProfessionalListForClub implements Serializable{
    private static List<EClubPro> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public List<EClubPro> list(final Connection conn) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
       LOG.debug("entering " + methodName);
   //    LOG.debug("with Player " + player);
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
 /*  mod 26-01-2023 final String query = """
           SELECT *
           FROM professional, club, player
           WHERE professional.ProClubId = club.idclub
           AND player.idplayer = ProplayerId
           AND DATE(NOW()) BETWEEN DATE(ProClubStartDate) AND DATE(ProClubEndDate)
           ORDER BY club.ClubName, player.PlayerLastName;
     """;
*/
 // liste de tous les pro
        final String query = """
           SELECT *
           FROM professional, club, player
           WHERE professional.ProClubId = club.idclub
            AND player.idplayer = ProplayerId
            AND NOW() BETWEEN ProClubStartDate AND ProClubEndDate
           ORDER BY club.ClubName, player.PlayerLastName;
     """;
    
     ps = conn.prepareStatement(query);
  //   ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
     liste = new ArrayList<>();
     while(rs.next()){
         EClubPro ecp = new EClubPro();
   //      Professional pro = Professional.map(rs);
         ecp.setClub(entite.Club.dtoMapper(rs));
         ecp.setProfessional(entite.Professional.map(rs));
         ecp.setPlayer(entite.Player.map(rs));
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
 //  liste.forEach(item -> LOG.debug("Players list with Players and passwords " + item));  // java 8 lambda
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

    public static List<EClubPro> getListe() {
        return liste;
    }

    public static void setListe(List<EClubPro> liste) {
        ProfessionalListForClub.liste = liste;
    }
    

  void main() throws SQLException, Exception {
    Connection conn = new utils.DBConnection().getConnection();
//   Player player = new Player();
 //   player.setIdplayer(324720);
    
    List<EClubPro> prof = new ProfessionalListForClub().list(conn);
        LOG.debug("list Pro for Clubs = " + prof.size());
    prof.forEach(item -> LOG.debug("Club(s) list for a Pro " + item));
    utils.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class