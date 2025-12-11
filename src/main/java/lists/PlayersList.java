package lists;

import entite.composite.EPlayerPassword;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class PlayersList{
    private static List<EPlayerPassword> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public List<EPlayerPassword> list(final Connection conn) throws SQLException{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
  //  LOG.debug("starting listAllPlayers() with conn = " + conn );
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
    final String query = """
          \n /* lists.PlayersList.list */
          SELECT *
          FROM Player
          WHERE PlayerActivation='1'
          ORDER BY idplayer
       """;
     ps = conn.prepareStatement(query);
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
    liste = new ArrayList<>();
                while(rs.next()){
                     EPlayerPassword epp = new EPlayerPassword(); // liste pour sélectionner un round player = entite.Player.mapPlayer(rs);
                     epp.setPlayer(entite.Player.map(rs));
                     epp.setPassword(entite.Password.map(rs));
                     liste.add(epp);
                } // end while
     if(liste.isEmpty()){
         String error = "££ Empty Result Table in " + methodName;
         LOG.error(error);
         LCUtil.showMessageFatal(error);
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }

  // liste.forEach(item -> LOG.debug("Players list with Players and passwords " + item));  // java 8 lambda
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
    

    public static List<EPlayerPassword> getListe() {
        return liste;
    }

  public static void setListe(List<EPlayerPassword> liste) {
        PlayersList.liste = liste;
    }
    
 // void main() throws SQLException, Exception {// testing purposes
 void main() throws SQLException, Exception {// testing purposes    
      LOG.debug("starting main");
    Connection conn = new utils.DBConnection().getConnection();
    LOG.debug("after connexion");
    if(conn  == null){
      LOG.debug("conn is null");
  }
    List<EPlayerPassword> p1 = new PlayersList().list(conn);
        LOG.debug("Inscription list = " + p1.size());
    utils.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class