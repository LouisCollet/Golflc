package sql.preparedstatement;

import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class psUpdateTarifGreenfee implements Serializable, interfaces.Log, interfaces.GolfInterface {

    /**
     * @param json        TarifGreenfee serialized as JSON
     * @param year        year from first season start date
     * @param startDate   first season start date (truncated to day)
     * @param endDate     last season end date (truncated to day)
     * @param tarifId     WHERE TarifId = ?
     */
    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final String json,
            final int year,
            final LocalDateTime startDate,
            final LocalDateTime endDate,
            final int tarifId) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setString   (1, json);
            ps.setInt      (2, year);
            ps.setTimestamp(3, Timestamp.valueOf(startDate));
            ps.setTimestamp(4, Timestamp.valueOf(endDate));
            ps.setInt      (5, tarifId);
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method
} // end class
