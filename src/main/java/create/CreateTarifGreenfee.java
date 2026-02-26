package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import entite.TarifGreenfee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class CreateTarifGreenfee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public CreateTarifGreenfee() { }

    public boolean create(final TarifGreenfee tarif, final Club club) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with tarif = " + tarif);
        LOG.debug("for club = " + club);

        LOG.debug("currency code = " + club.getAddress().getCountry().getCurrency());

        if (tarif.getDatesSeasonsList().isEmpty()) {
            String msgerr = LCUtil.prepareMessageBean("create.greenfee.season.empty");
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
            return false;
        }
        if (tarif.getEquipmentsList().isEmpty()) {
            String msgerr = LCUtil.prepareMessageBean("create.greenfee.equipments.empty");
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
            return false;
        }
        if (tarif.getTwilightList().isEmpty() && tarif.isTwilightReady()) {
            String msgerr = LCUtil.prepareMessageBean("create.greenfee.twilight.empty");
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
            return false;
        }
        if (tarif.getDatesSeasonsList().isEmpty()
                && tarif.getDaysList().isEmpty()
                && tarif.getTeeTimesList().isEmpty()
                && tarif.getEquipmentsList().isEmpty()
                && tarif.getBasicList().isEmpty()) {
            String msgerr = LCUtil.prepareMessageBean("create.greenfee.empty");
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
            throw new Exception(msgerr);
        }

        LocalDateTime lddeb = tarif.getDatesSeasonsList().get(0).getStartDate().truncatedTo(ChronoUnit.DAYS);
        int j = tarif.getDatesSeasonsList().size() - 1;
        LocalDateTime ldfin = tarif.getDatesSeasonsList().get(j).getStartDate().truncatedTo(ChronoUnit.DAYS);
        LOG.debug("ldfin format LocalDateTime = " + ldfin);

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.configure(SerializationFeature.INDENT_OUTPUT, true);
        String json = om.writeValueAsString(tarif);
        LOG.debug("tarif converted in json format = " + json);

        try (Connection conn = dataSource.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "tarif_greenfee");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.getWarnings();
                ps.setNull(1, java.sql.Types.INTEGER);  // autoincrement
                ps.setInt(2, lddeb.getYear());
                ps.setTimestamp(3, Timestamp.valueOf(lddeb));
                ps.setTimestamp(4, Timestamp.valueOf(ldfin));
                ps.setInt(5, tarif.getTarifCourseId());
                ps.setString(6, json);
                ps.setString(7, club.getAddress().getCountry().getCurrency());
                LOG.debug("currency code = " + club.getAddress().getCountry().getCurrency());
                ps.setTimestamp(8, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                LOG.debug("row  = " + row);
                if (row != 0) {
                    String msg = "Tarif Created for Course = " + tarif.getTarifCourseId()
                            + "<br/>tarif = " + tarif;
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "<br/>NOT NOT Successful TarifGreenfee inserted for " + tarif.getTarifCourseId();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        TarifGreenfee tarif = new TarifGreenfee();
        Club club = new Club();
        club.setIdclub(151);
        club = new read.ReadClub().read(club);
        LOG.debug("club = " + club);
        boolean b = new CreateTarifGreenfee().create(tarif, club);
        LOG.debug("result = " + b);
    } // end main
    */

} // end class
