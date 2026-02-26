
package rowmappers;
import entite.Hole;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HoleRowMapper extends AbstractRowMapper<Hole> {

    @Override
    public Hole map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            var hole = new Hole();

            hole.setIdhole(getInteger(rs,"idhole"));
            hole.setHoleNumber(getShort(rs,"HoleNumber"));
            hole.setHolePar(getShort(rs,"HolePar"));
            hole.setHoleDistance(getShort(rs,"HoleDistance"));
            hole.setHoleStrokeIndex(getShort(rs,"HoleStrokeIndex"));
            hole.setTee_idtee(getInteger(rs,"tee_idtee"));
            hole.setTee_course_idcourse(getInteger(rs,"tee_course_idcourse"));

            return hole;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}


/*
import entite.Hole;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HoleRowMapper implements RowMapper<Hole> {
 
    @Override
   public Hole map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();  
   try{  
           //LOG.debug("entering map for method = " + methodName);
        Hole hole = new Hole();
        hole.setIdhole(rs.getInt("idhole"));
        hole.setHoleNumber(rs.getShort("HoleNumber") );
        hole.setHolePar(rs.getShort("HolePar") );
        hole.setHoleDistance(rs.getShort("HoleDistance") );
        hole.setHoleStrokeIndex(rs.getShort("HoleStrokeIndex") );
        hole.setTee_idtee(rs.getInt("tee_idtee"));
        hole.setTee_course_idcourse(rs.getInt("tee_course_idcourse"));
      return hole;
 }catch(Exception e){
        handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class
*/