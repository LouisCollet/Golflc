
package rowmappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.UnavailableStructure;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnavailableStructureRowMapper extends AbstractRowMapper<UnavailableStructure> {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public UnavailableStructure map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

     try {
         String struct = getString(rs,"GroundCondition");
      //      LOG.debug("String structure from DB = " + struct);
         if(rs.getString("GroundCondition") == null){
      //      LOG.debug("map - Unavailable Structure is null !! Null returned");
            return null;
         }
   //      ObjectMapper om = new ObjectMapper();
         UnavailableStructure structure = OBJECT_MAPPER.readValue(struct, UnavailableStructure.class);
         
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
} // end class
