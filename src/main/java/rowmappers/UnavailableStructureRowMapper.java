
package rowmappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import entite.UnavailableStructure;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnavailableStructureRowMapper extends AbstractRowMapper<UnavailableStructure> {

    @Override
    public UnavailableStructure map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

     try {
         String struct = getString(rs,"ClubUnavailableStructure");
      //      LOG.debug("String structure from DB = " + struct);
         if(rs.getString("ClubUnavailableStructure") == null){
      //      LOG.debug("map - Unavailable Structure is null !! Null returned");
            return null;
         }
   //      ObjectMapper om = new ObjectMapper();
         UnavailableStructure structure =  new ObjectMapper().readValue(struct,UnavailableStructure.class);
         
     //       LOG.debug("UnavailableStructure extracted from database = "  + struct);
     //       LOG.debug("nombre d'items structure = " + structure.getStructureList().size());
 //           LOG.debug("array items structure = " + Arrays.deepToString(str.getItemStructure()));
         structure.setIdclub(getInteger(rs,"IdClub"));
  //          LOG.debug("idclub setted = " + structure.getIdclub());
   return structure;
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