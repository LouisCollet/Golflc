
package rowmappers;

import entite.Classment;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class ClassmentRowMapper extends AbstractRowMapper<Classment> {

 //   private static final String CLASSNAME = utils.LCUtil.getCurrentClassName();

    @Override
    public Classment map(ResultSet rs) throws SQLException {
  //      final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
              Classment c = new Classment();
              c.setTotalExtraStrokes(getInteger(rs,"TotalExtraStrokes"));
              c.setTotalPoints(getInteger(rs,"TotalScore")); 
              c.setLast9(getInteger(rs,"Last9"));
              c.setLast6(getInteger(rs,"Last6"));
              c.setLast3(getInteger(rs,"Last3"));
              c.setLast1(getInteger(rs,"Last1"));
   return c;

    } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
    }
  }
} //end class