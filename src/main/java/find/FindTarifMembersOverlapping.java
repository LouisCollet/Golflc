package find;

import com.github.mawippel.validator.OverlappingVerificator;
import entite.TarifMember;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

public class FindTarifMembersOverlapping implements interfaces.Log, interfaces.GolfInterface{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 @Inject private TarifMember tarif;
 private static List<TarifMember> liste = null;
 
 public boolean find(final TarifMember tarif_new, final Connection conn) throws SQLException{   
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug("...entering " + methodName);
        LOG.debug(" ... for tarifMember = " + tarif_new);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
      final String query = """
            SELECT *
            FROM tarif_members
            WHERE tarif_members.TarifMemberIdClub = ?
         """;

    ps = conn.prepareStatement(query);
    ps.setInt(1, tarif_new.getTarifMemberIdClub());
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    int i = 0;
    liste = new ArrayList<>();
    while(rs.next()){
    //        tarif = entite.TarifMember.map(rs);
            liste.add(entite.TarifMember.map(rs));
            i++;
    }
       LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
     if(i == 0){
         String msg = "ok because i = 0 : first tarif for this club !";
      //      String err =  LCUtil.prepareMessageBean("tarif.notfound") + tarif_in; 
 //                   + club.getClubName() + " / " + club.getIdclub();
               LOG.debug(msg);
         return false;
     }
     // la liste contient toutes les lignes tarifs pour le club
        liste.forEach(item -> LOG.debug("list of TarifMember =" + item));  // java 8 lambda
    // on vérifie le overlapping pour chaque ligne de la table 
    
 //   LocalDateTime comparableStart = tarif_new.getMemberStartDate(); 
 //      LOG.debug("comparableStart = " + comparableStart);
 //   LocalDateTime comparableEnd  = tarif_new.getMemberEndDate();
 //      LOG.debug("comparableEnd =   " + comparableEnd);
            for (i = 0; i < liste.size(); i++) {
              // existing periods in table DB
   //             LocalDateTime toCompareStart = liste.get(i).getMemberStartDate(); 
   //                LOG.debug("toCompareStart = " + toCompareStart);
   //             LocalDateTime toCompareEnd  = liste.get(i).getMemberEndDate();
   //                LOG.debug("toCompareEnd =  " + toCompareEnd);
        //        boolean isOverlap = OverlappingVerificator.isOverlap(
        //                comparableStart, comparableEnd,
        //                toCompareStart, toCompareEnd);
                boolean isOverlap = OverlappingVerificator.isOverlap(
                        tarif_new.getStartDate(), tarif_new.getEndDate(),
                        liste.get(i).getStartDate(), liste.get(i).getEndDate());
                  LOG.debug(" isOverlap ? = " + isOverlap);
                if(isOverlap){
                   String msg =  LCUtil.prepareMessageBean("tarif.overlapping" )
                     + ZDF_DAY.format(tarif_new.getStartDate()) + " - " + ZDF_DAY.format(tarif_new.getEndDate())
                     + " against <br>"
                     + ZDF_DAY.format(liste.get(i).getStartDate()) + " - " + ZDF_DAY.format(liste.get(i).getEndDate());
                   LOG.error(msg);
                   showMessageFatal(msg);
                   return true;
                } // end isOverlap
            } //end for
     return false;
}catch (SQLException e){
    String msg = "SQL Exception for " + methodName + " " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return false;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

void main() throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();

    TarifMember tm = new TarifMember();
    tm.setStartDate(LocalDateTime.of(2021,Month.JANUARY,01,0,0));
    tm.setEndDate(LocalDateTime.of(2021,Month.DECEMBER,31,0,0));
    tm.setTarifMemberIdClub(1122);
    boolean b = new FindTarifMembersOverlapping().find(tm, conn);
     LOG.debug("result overlapping = " + b);
DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class