package lists;

import entite.EPlayerPassword;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class PlayersList implements interfaces.Log{
    private static List<EPlayerPassword> liste = null;
    
public List<EPlayerPassword> list(final Connection conn) throws Exception{
    
if(liste == null){
  //  LOG.debug("starting listAllPlayers() with conn = " + conn );
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
     //   liste = new ArrayList<>();
        String p = utils.DBMeta.listMetaColumnsLoad(conn, "player");  // fields list, comma separated
        final String query =
            "SELECT " + p 
            + " FROM Player"
            + " WHERE PlayerActivation = '1' "
            + " ORDER BY idplayer"
          ;
            ps = conn.prepareStatement(query);
             utils.LCUtil.logps(ps);
            rs = ps.executeQuery();
            liste = new ArrayList<>();
                	while(rs.next()){
                     EPlayerPassword epp = new EPlayerPassword(); // liste pour sélectionner un round player = entite.Player.mapPlayer(rs);
                     epp.setPlayer(entite.Player.mapPlayer(rs));
                     epp.setPassword(entite.Password.mapPassword(rs));
                     liste.add(epp);
                 } // end while
  // liste.forEach(item -> LOG.info("Players list with Players and passwords " + item));  // java 8 lambda
return liste;

} catch(SQLException sqle){
    String msg = "£££ SQL exception in ListAllPlayers = " + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception e){
    String msg = "£££ Exception in ListAllPlayers = " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
           DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
  ////   LOG.debug("escaped to listallplayers repetition thanks to lazy loading");
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
    
  public static void main(String[] args) throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
  //  Player player = new Player();
  //  player.setIdplayer(324713);
  // Round round = new Round(); 
  // round.setIdround(414);
  //  Club club = new Club();
  //  club.setIdclub(1006);
    List<EPlayerPassword> p1 = new PlayersList().list(conn);
        LOG.info("Inscription list = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
    
    
    
    
} //end Class