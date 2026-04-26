package sql.preparedstatement;

import entite.Inscription;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class psCreateInscription implements Serializable, interfaces.Log, interfaces.GolfInterface {

    /**
     * @param inscription doit avoir InscriptionIdTee déjà calculé par l'appelant
     */
    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final Round round,
            final Player player,
            final Player invitedBy,
            final Inscription inscription) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setNull(1, java.sql.Types.INTEGER);                              // AUTO-INCREMENT
            ps.setInt(2, round.getIdround());                                    // InscriptionIdRound
            ps.setInt(3, player.getIdplayer());                                  // InscriptionIdPlayer
            ps.setInt(4, 0);                                                     // FinalResult — initial value
            ps.setString(5, inscription.getInscriptionMatchplayTeam());          // InscriptionMatchplayTeam
            ps.setInt(6, 0);                                                     // NotUsed2
            String teeStart = inscription.getInscriptionTeeStart();
            if (teeStart != null) {
                ps.setString(7, teeStart);                                       // InscriptionTeeStart
            } else {
                ps.setNull(7, java.sql.Types.VARCHAR);                           // tee différé — sélectionné dans score_stableford
            }
            ps.setInt(8, inscription.getInscriptionIdTee());                     // InscriptionIdTee — 0 si tee différé
            ps.setInt(9, invitedBy.getIdplayer());                               // InscriptionInvitedBy
            ps.setTimestamp(10, Timestamp.from(Instant.now()));                 // InscriptionModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);// TarifModificationDate
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
