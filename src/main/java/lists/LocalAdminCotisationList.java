package lists;

import entite.composite.ECotisation;
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

@Named("LACotisation")
@ViewScoped // nécessaire !! pour faire le total dans local_administrator_cotisations.xhtml

public class LocalAdminCotisationList implements Serializable{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static List<ECotisation> liste = null;
public List<ECotisation> list(final Player localAdmin ,Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
if(liste == null){
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     String query = """
            SELECT *
            FROM payments_cotisation, club, player
            WHERE club.ClubLocalAdmin = ?
              AND payments_cotisation.CotisationIdClub = club.idclub
              AND player.idplayer = cotisationIdPlayer
            ORDER BY cotisationIdclub, playerlastname
            """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, localAdmin.getIdplayer());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     liste = new ArrayList<>();
	while(rs.next()){
           ECotisation ec = new ECotisation();
           ec.setClub(entite.Club.dtoMapper(rs));
           ec.setPlayer(entite.Player.map(rs));
           ec.setCotisation(entite.Cotisation.map(rs)); // mod 26/09/2022
            liste.add(ec);
	} // end while
  //     LOG.debug(" -- before forEach " );
 //      liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda
      if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 return liste;
}catch (SQLException e){ 
        String error = "SQL Exception in LocalAdminCotisationList = " + e.toString() + ", SQLState = " + e.getSQLState() + ", ErrorCode = " + e.getErrorCode();
	LOG.error(error);
        LCUtil.showMessageFatal(error);
        return null;
}catch (Exception ex){
    String error = "Exception in " + methodName + " / " + ex;
    LOG.error(error);
    LCUtil.showMessageFatal(error);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
 //   LOG.debug("escaped to " + methodName + "repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECotisation> getListe() {
        return liste;
    }

    public static void setListe(List<ECotisation> liste) {
        LocalAdminCotisationList.liste = liste;
    }

 void main() throws SQLException, Exception{
  //    OptionSet args = parseOptions(argv);
     Connection conn = new DBConnection().getConnection();
  try{
      Player player = new Player();
      player.setIdplayer(324715);
    var lp = new LocalAdminCotisationList().list(player, conn);
        LOG.debug("from main, result = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
 }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class