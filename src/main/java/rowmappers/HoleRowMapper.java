
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
} // end class
