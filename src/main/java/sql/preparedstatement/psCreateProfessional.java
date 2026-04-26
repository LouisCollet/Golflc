package sql.preparedstatement;

import entite.Professional;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class psCreateProfessional implements Serializable, interfaces.Log, interfaces.GolfInterface{
    
 public static PreparedStatement psMapCreate(PreparedStatement ps, Professional professional) throws Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
        // ps.setInt(1, 0);  // auto generated — MySQL treats 0 as auto-increment claude code
        ps.setNull(1, java.sql.Types.INTEGER); // mod lc
        ps.setInt(2, professional.getProClubId());
        ps.setTimestamp(3,Timestamp.valueOf(professional.getProStartDate()));
        ps.setTimestamp(4,Timestamp.valueOf(professional.getProEndDate()));
        ps.setInt(5, professional.getProPlayerId());
        ps.setDouble(6, professional.getProAmount()); // new 06-06-2021
        ps.setInt(7, professional.getProWorkDays());
        ps.setTimestamp(8, Timestamp.from(Instant.now()));
        sql.PrintWarnings.print(ps.getWarnings(), methodName);
        utils.LCUtil.logps(ps);
 return ps;
}catch(Exception e){
     handleGenericException(e, methodName);
     return null;
  }
} //end method
 public static PreparedStatement psMapUpdateFull(PreparedStatement ps, Professional professional) throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    try {
        ps.setDouble(1, professional.getProAmount());
        ps.setTimestamp(2, java.sql.Timestamp.valueOf(professional.getProStartDate()));
        ps.setTimestamp(3, java.sql.Timestamp.valueOf(professional.getProEndDate()));
        ps.setInt(4, professional.getProWorkDays());
        ps.setInt(5, professional.getProId());
        ps.getWarnings();
        return ps;
    } catch (Exception e) {
        handleGenericException(e, methodName);
        return null;
    }
 } // end method

 public static PreparedStatement psMapUpdate(PreparedStatement ps, Professional professional) throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    try {
        ps.setDouble(1, professional.getProAmount());
        ps.setInt(2, professional.getProId());
        ps.getWarnings();
        return ps;
    } catch (Exception e) {
        handleGenericException(e, methodName);
        return null;
    }
 } // end method
} //end class