package lists;

import entite.Club;
import entite.composite.EUnavailable;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import utils.DBConnection;
import utils.LCUtil;

public class UnavailableListForDate implements interfaces.Log{
    
 //   static List<EUnavailable> liste = null;
    static List<EUnavailable> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
// public List<EUnavailable> list(LocalDateTime ldt, Club club, final @NotNull Connection conn) throws SQLException{
   public EUnavailable list(LocalDateTime ldt, Club club, final @NotNull Connection conn) throws SQLException{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    
if(liste == null){
      LOG.debug("entering method : " + methodName); 
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
  String query = """
          SELECT *
          FROM club, unavailable_periods
          WHERE club.idclub = ?
          AND UnavailableIdClub = club.idclub
          AND ? BETWEEN UnavailableStartDate AND UnavailableEndDate
          ORDER BY unavailable_periods.UnavailableModificationDate desc
          LIMIT 1
          """ ;
     ps = conn.prepareStatement(query);
     ps.setInt(1,club.getIdclub() ); // search key
     ps.setTimestamp(2,java.sql.Timestamp.valueOf(ldt)); // new 17/06/2022
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
    liste = new ArrayList<>();
	while(rs.next()){
		EUnavailable u = new EUnavailable();
                u.setStructure(entite.UnavailableStructure.map(rs));
                u.setPeriod(entite.UnavailablePeriod.map(rs));
	 liste.add(u);
	} // end while
     if(liste.isEmpty()){
         String msg = "Il n'y a pas aujourd'hui un état du terrain pour ce club : " + club.getIdclub();
         LOG.info(msg);
         LCUtil.showMessageInfo(msg);
         return null; // non
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
         String msg = "Etat du terrain pour ce club : " + liste.toString();
         LOG.info(msg);
         LCUtil.showMessageInfo(msg);
         return liste.getFirst();   // on prend le premier qui est unique car LIMIT 1
     }
//      liste.forEach(item -> LOG.debug("Unavailable list " + item + "/"));  // java 8 lambda                   
 //   return liste.get(0); 
}catch (SQLException e){ 
        String msg = "SQL Exception in " + methodName + " / " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
    LOG.debug("escaped to CourseListlist repetition thanks to lazy loading");
  //  return liste;  //plusieurs fois ??
    return liste.getFirst(); //get(0);  //plusieurs fois ??
}
} //end method

    public static List<EUnavailable> getListe() {
        return liste;
    }

    public static void setListe(List<EUnavailable> liste) {
        UnavailableListForDate.liste = liste;
    }
    
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
  Club club = new Club();
  club.setIdclub(1075); // la cala
  Round round = new Round();
      round.setIdround(698);  // 19/05/2022 16:01
      round = new read.ReadRound().read(round, conn);
  //    List<EUnavailable> eu = new UnavailableListForDate().list(LocalDateTime.now(),club,conn);
  EUnavailable eu = new UnavailableListForDate().list(round.getRoundDate(),club,conn);
        LOG.debug("from main, is Unavailable = " + eu);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
 }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
 } // end main//
} //end class