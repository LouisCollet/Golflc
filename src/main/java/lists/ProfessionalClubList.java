package lists;

import entite.Player;
import entite.Professional;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

// vérifier si un player est aussi un pro : retourner les liste des clubs où il est pro
public class ProfessionalClubList implements interfaces.Log{
    private static List<Professional> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public List<Professional> list(final Player player, final Connection conn) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
       LOG.debug("entering " + methodName);
       LOG.debug("with Player " + player);
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
    final String query =
     """
        SELECT *
        FROM professional
        WHERE professional.ProPlayerId = ?
        AND DATE(NOW()) BETWEEN DATE(ProClubStartDate) AND DATE(ProClubEndDate)
     """ ;
//    SELECT * FROM professional
//WHERE professional.ProPlayerId = 324720;
    
    
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
     liste = new ArrayList<>();
     while(rs.next()){
         Professional pro = Professional.map(rs);
         liste.add(pro);
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
    

    public static List<Professional> getListe() {
        return liste;
    }

    public static void setListe(List<Professional> liste) {
        ProfessionalClubList.liste = liste;
    }

 
  void main() throws SQLException, Exception {
    Connection conn = new utils.DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324715);
    
    List<Professional> prof = new ProfessionalClubList().list(player, conn);
        LOG.debug("schedule list  for a Pro = " + prof.size());
    prof.forEach(item -> LOG.debug("Club(s) list for a Pro " + item));
    utils.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class