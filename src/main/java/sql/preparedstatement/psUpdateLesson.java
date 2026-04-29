package sql.preparedstatement;

import entite.Lesson;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class psUpdateLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapUpdate(PreparedStatement ps, Lesson before, Lesson after) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setTimestamp(1, Timestamp.valueOf(after.getEventStartDate()));
            ps.setTimestamp(2, Timestamp.valueOf(after.getEventEndDate()));
            ps.setInt(3, before.getEventProId());
            ps.setTimestamp(4, Timestamp.valueOf(before.getEventStartDate()));
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

} // end class
