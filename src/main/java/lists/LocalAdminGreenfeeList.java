package lists;

import entite.composite.EGreenfee;
import entite.Player;
import static interfaces.Log.LOG;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

@Named("LAGreenfee")
@ViewScoped // nécessaire !! pour faire le total dans local_administrator_greenfee.xhtml

public class LocalAdminGreenfeeList implements Serializable{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static List<EGreenfee> liste = null;
public List<EGreenfee> list(final Player localAdmin ,Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
if(liste == null){
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    // error = il faut trouver le nom du player à partir de payments_greenfee.GreenfeeIdPlayer
    final String query = """
           \n   /* lists.LocalAdminGreenfeeList.list  */
      SELECT *
      FROM payments_greenfee, club
      WHERE club.ClubLocalAdmin = ?
        AND payments_greenfee.GreenfeeIdClub = club.idclub
      GROUP BY idgreenfee
      ORDER BY GreenfeeIdClub
    """;
     /*       AND player.idplayer = payments_greenfee.GreenfeeIdPlayer
     SELECT *
 FROM payments_greenfee, club, player
 WHERE club.ClubLocalAdmin = 324715
   AND payments_greenfee.GreenfeeIdClub = club.idclub
--    AND player.idplayer = payments_greenfee.GreenfeeIdPlayer
GROU P BY idgreenfee
 ORDER BY GreenfeeIdClub, playerlastname
    */
    
     ps = conn.prepareStatement(query);
     ps.setInt(1, localAdmin.getIdplayer());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     liste = new ArrayList<>();
   //  Player p = new Player();
	while(rs.next()){
           EGreenfee eg = new EGreenfee();
           eg.setClub(entite.Club.dtoMapper(rs));
           Player p = new Player();
           LOG.debug("line 01");
       //     LOG.debug("line 01b " + rs.getInt("GreenfeeIdPlayer)"));
            int i = rs.getInt("GreenfeeIdPlayer");
            LOG.debug("payments_greenfee.GreenfeeIdPlayer = " + i);
            p.setIdplayer(rs.getInt("GreenfeeIdPlayer"));
  //         LOG.debug("line 02 p = " + p);
          p = new read.ReadPlayer().read(p,conn);
         LOG.debug("p = " + p);
         //   eg.setPlayer(new read.ReadPlayer().read(p,conn));
        //   eg.setPlayer(entite.Player.map(rs));
        if(p != null){
            eg.setPlayer(p);
        }else{
            eg.setPlayer(null);
        }
           
           eg.setGreenfee(entite.Greenfee.map(rs));
            liste.add(eg);
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
         liste.forEach(item -> LOG.debug("Players list with greenfee paid = " + item.getGreenfee().getPrice()
                              + " /IdClub = " + item.getClub().getIdclub())
                        //      + " /playerLastName = " + item.getPlayer().getPlayerLastName() )
                      );
                 //   + " /idclub =  " + item.getClub().getIdclub()
     }
 return liste;
}catch (SQLException e){ 
        String error = "SQL Exception in " + methodName + " + e.toString() " + ", SQLState = " + e.getSQLState() + ", ErrorCode = " + e.getErrorCode();
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

    public static List<EGreenfee> getListe() {
        return liste;
    }

    public static void setListe(List<EGreenfee> liste) {
        LocalAdminGreenfeeList.liste = liste;
    }

 void main() throws SQLException, Exception{
  //    OptionSet args = parseOptions(argv);
     Connection conn = new DBConnection().getConnection();
  try{
      Player player = new Player();
      player.setIdplayer(324715);
    var lp = new LocalAdminGreenfeeList().list(player, conn);
        LOG.debug("from main, result size= " + lp.size());
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
 }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class