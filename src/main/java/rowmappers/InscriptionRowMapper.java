
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

/*
import entite.Inscription;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InscriptionRowMapper implements RowMapper<Inscription> {
 
    @Override
   public Inscription map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();  
   try{  
           //LOG.debug("entering map for method = " + methodName);
    Inscription inscription = new Inscription();
    inscription.setInscriptionFinalResult(rs.getShort("InscriptionFinalResult"));
    inscription.setInscriptionTeeStart(rs.getString("InscriptionTeeStart"));
    inscription.setInscriptionInvitedBy(rs.getString("InscriptionInvitedBy"));
    inscription.setInscriptionIdTee(rs.getInt("InscriptionIdTee")); // new 31-03-2019
    inscription.setInscriptionMatchplayTeam(rs.getString("In‌scriptionMatchplayTeam")); // new 20-09-2021
  return inscription;
 }catch(Exception e){
    handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class*/