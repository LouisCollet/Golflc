package rowmappers;

import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeeRowMapper implements RowMapper<Tee> {
 
    @Override
   public Tee map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();
   try{
        Tee tee = new Tee();
        int idtee = rs.getInt("idtee");
        if (rs.wasNull()) {
            // LEFT JOIN — no tee for this course
            return tee;
        }
        tee.setIdtee(idtee);
        tee.setTeeGender(rs.getString("TeeGender"));
        tee.setTeeStart(rs.getString("TeeStart"));
        tee.setTeeSlope(rs.getShort("teeslope"));
        tee.setTeeRating(rs.getBigDecimal("teerating"));
        tee.setTeeClubHandicap(rs.getInt("TeeClubHandicap"));
        tee.setCourse_idcourse(rs.getInt("course_idcourse"));
        tee.setTeeHolesPlayed(rs.getString("TeeHolesPlayed"));
        tee.setTeePar(rs.getShort("TeePar"));
        tee.setTeeMasterTee(rs.getInt("TeeMasterTee"));
        tee.setTeeDistanceTee(rs.getInt("TeeDistanceTee"));
    return tee;
 }catch(Exception e){
        handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class