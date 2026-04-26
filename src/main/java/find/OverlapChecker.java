package find;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import rowmappers.RowMapper;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

@ApplicationScoped
public class OverlapChecker implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public OverlapChecker() { }

    @FunctionalInterface
    public interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    public <T> boolean check(
            LocalDateTime newStart,
            LocalDateTime newEnd,
            String query,
            ParamSetter params,
            RowMapper<T> mapper,
            Function<T, LocalDateTime> getStart,
            Function<T, LocalDateTime> getEnd) throws SQLException {

        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            params.set(ps);
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<T> existing = new ArrayList<>();
                while (rs.next()) {
                    T item = mapper.map(rs);
                    if (item != null) existing.add(item);
                }

                if (existing.isEmpty()) {
                    LOG.debug("no existing period — no overlap");
                    return false;
                }

                for (T ep : existing) {
                    LocalDateTime epStart = getStart.apply(ep);
                    LocalDateTime epEnd   = getEnd.apply(ep);
                    if (epStart == null || epEnd == null) continue;

                    boolean isOverlap = !(newEnd.isBefore(epStart) || epEnd.isBefore(newStart));
                    LOG.debug("overlap check: new [{} - {}] vs existing [{} - {}] = {}",
                            newStart, newEnd, epStart, epEnd, isOverlap);

                    if (isOverlap) {
                        String msg = LCUtil.prepareMessageBean("tarif.overlapping")
                                + ZDF_DAY.format(newStart) + " - " + ZDF_DAY.format(newEnd)
                                + " against <br/>"
                                + ZDF_DAY.format(epStart)  + " - " + ZDF_DAY.format(epEnd);
                        LOG.error(msg);
                        showMessageFatal(msg);
                        return true;
                    }
                }
                return false;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // Surcharge in-memory — liste déjà chargée, pas de requête SQL
    public <T> boolean check(
            LocalDateTime newStart,
            LocalDateTime newEnd,
            List<T> existing,
            Function<T, LocalDateTime> getStart,
            Function<T, LocalDateTime> getEnd) {

        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("existing list size = {}", existing.size());

        try {
            for (T ep : existing) {
                LocalDateTime epStart = getStart.apply(ep);
                LocalDateTime epEnd   = getEnd.apply(ep);
                if (epStart == null || epEnd == null) continue;

                boolean isOverlap = !(newEnd.isBefore(epStart) || epEnd.isBefore(newStart));
                LOG.debug("overlap check: new [{} - {}] vs existing [{} - {}] = {}",
                        newStart, newEnd, epStart, epEnd, isOverlap);

                if (isOverlap) {
                    String msg = LCUtil.prepareMessageBean("tarif.overlapping")
                            + ZDF_DAY.format(newStart) + " - " + ZDF_DAY.format(newEnd)
                            + " against "
                            + ZDF_DAY.format(epStart)  + " - " + ZDF_DAY.format(epEnd);
                    LOG.error(msg);
                    showMessageFatal(msg);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return true;
        }
    } // end method

} // end class
