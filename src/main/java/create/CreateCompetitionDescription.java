package create;

import entite.CompetitionDescription;
import entite.ValidationsLC;
import entite.ValidationsLC.ValidationStatus;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;
import static utils.LCUtil.LocalDateTimeToDate;

@ApplicationScoped
public class CreateCompetitionDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    ValidationsLC vlc = new ValidationsLC();

    public CreateCompetitionDescription() { }

    public boolean create(final CompetitionDescription competition) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for competition = {}", competition);

        try {
            vlc = this.validate(competition);
            LOG.debug("vlc = {}", vlc);
            if (vlc.getStatus0().equals(ValidationStatus.REJECTED.toString())) {
                LOG.error(vlc.getStatus1());
                LCUtil.showMessageFatal(vlc.getStatus1());
                return false;
            }

            try (Connection conn = dao.getConnection()) {

                final String query = LCUtil.generateInsertQuery(conn, "competition_description");
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    sql.preparedstatement.psCreateCompetitionDescription.psMapCreate(ps, competition);

                    int row = ps.executeUpdate();
                    if (row != 0) {
                        competition.setCompetitionId(LCUtil.generatedKey(conn));
                        String msg = LCUtil.prepareMessageBean("competition.create") + competition;
                        LOG.debug(msg);
                        LCUtil.showMessageInfo(msg);
                        return true;
                    } else {
                        String msg = methodName + " - ERROR update competitionDescription : " + competition;
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }
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

    public ValidationsLC validate(final CompetitionDescription competition) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            vlc.setStatus0(ValidationsLC.ValidationStatus.APPROVED.toString());
            if (competition.getEndInscriptionDate().isBefore(competition.getStartInscriptionDate())) {
                vlc.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
                Object[] data = new Object[2];
                data[0] = competition.getEndInscriptionDate();
                data[1] = LocalDateTimeToDate(competition.getStartInscriptionDate());
                String msgerr = LCUtil.prepareMessageBean1("competition.endbefore.start", data);
                vlc.setStatus1(msgerr);
                return vlc;
            }
            if (competition.getCompetitionDate().isBefore(competition.getStartInscriptionDate())) {
                vlc.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
                Object[] data = new Object[2];
                data[0] = LocalDateTimeToDate(competition.getCompetitionDate());
                data[1] = LocalDateTimeToDate(competition.getStartInscriptionDate());
                String msgerr = LCUtil.prepareMessageBean1("competition.datebefore.start", data);
                vlc.setStatus1(msgerr);
                return vlc;
            }
            return vlc; // is approved

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // var b = create(competition);
        // LOG.debug("from main, b = {}", b);
        LOG.debug("from main, CreateCompetitionDescription = ");
    } // end main
    */

} // end class
