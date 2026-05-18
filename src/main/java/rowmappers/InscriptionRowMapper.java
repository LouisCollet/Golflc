
package rowmappers;

import entite.Inscription;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InscriptionRowMapper extends AbstractRowMapper<Inscription> {

    @Override
    public Inscription map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            var inscription = new Inscription();
            inscription.setInscriptionFinalResult(getShort(rs,"InscriptionFinalResult"));
            inscription.setInscriptionTeeStart(getString(rs,"InscriptionTeeStart"));
            inscription.setInscriptionInvitedBy(getString(rs,"InscriptionInvitedBy"));
            inscription.setInscriptionIdTee(getInteger(rs,"InscriptionIdTee"));
            inscription.setInscriptionMatchplayTeam(getString(rs,"InscriptionMatchplayTeam"));
            return inscription;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}
