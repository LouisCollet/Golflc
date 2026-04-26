package create;

import entite.Club;
import entite.Cotisation;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
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
import java.util.Collections;
import java.util.List;
import utils.LCUtil;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class CreateInscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private find.FindInscriptionRound      findInscriptionRound;
    @Inject private find.FindCotisationAtRoundDate  findCotisationAtRoundDate;
    @Inject private find.FindGreenfeePaid           findGreenfeePaid;
    @Inject private lists.RoundPlayersList          roundPlayersList;
    @Inject private mail.InscriptionMail            inscriptionMail;

    public CreateInscription() { }

    public Inscription create(final Round round,
            final Player player,
            final Player invitedBy,
            final Inscription inscription,
            final Club club,
            final Course course,
            final String batch) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for round = {}", round);
        LOG.debug("for player = {}", player);
        LOG.debug("for inscription = {}", inscription);

        inscription.setPlayer_idplayer(player.getIdplayer());
        inscription.setRound_idround(round.getIdround());
        LOG.debug("inscription completed = {}", inscription);

        // Validation
        ValidationsLC vlc = this.validate(round, player, inscription, club, course);
        LOG.debug("returned from validate = {}", vlc);

        if (vlc.getStatus2().equals("04")) {  // déjà inscrit
            LOG.debug("error 04 (duplicate)");
            inscription.setInscriptionError(true);
            inscription.setErrorStatus(vlc.getStatus2());
            LCUtil.showMessageInfo(vlc.getStatus1());
            return inscription;
        }

        if (vlc.getStatus0().equals(ValidationStatus.REJECTED.toString())) {
            String msg = vlc.getStatus1();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            inscription.setInscriptionError(true);
            inscription.setWeather(vlc.getStatus1()); // astuce provisoire
            inscription.setErrorStatus(vlc.getStatus2());
            return inscription;
        }

        if (vlc.getStatus0().equals(ValidationStatus.APPROVED.toString())) {
            LOG.debug("validation APPROVED = {}", vlc.getStatus1());
            LCUtil.showMessageInfo(vlc.getStatus1());
            inscription.setInscriptionError(false);
        }

        // INSERT
        try (Connection conn = dao.getConnection()) {

            final String query = LCUtil.generateInsertQuery(conn, "inscription");
            try (PreparedStatement ps = conn.prepareStatement(query)) {

                // calcul de l'idTee avant le mapping PS — null si tee différé (score_stableford)
                String s = inscription.getInscriptionTeeStart();
                LOG.debug("inscriptionTeeStart = {}", s);
                if (s != null && s.contains("/")) {
                    int tee = Integer.parseInt(s.substring(s.lastIndexOf("/") + 2).trim());
                    inscription.setInscriptionIdTee(tee);
                } else {
                    inscription.setInscriptionIdTee(0); // tee sélectionné ultérieurement dans score_stableford
                }

                sql.preparedstatement.psCreateInscription.psMapCreate(ps, round, player, invitedBy, inscription);
                utils.LCUtil.logps(ps);

                int row = ps.executeUpdate();
                if (row == 1) {
                    LOG.debug("InscriptionId created = {}", LCUtil.generatedKey(conn));
                    String msg = LCUtil.prepareMessageBean("inscription.ok") + " = " + inscription + " for round = " + round;
                    LOG.debug(msg);
                    showMessageInfo(msg);
                    if (batch.equalsIgnoreCase("A")) {
                        try {
                            inscriptionMail.create(player, invitedBy, round, club, course);
                            LOG.debug("mail sent");
                        } catch (Exception mailEx) {
                            LOG.error("mail failed: {}", mailEx.getMessage());
                        }
                    }
                    inscription.setInscriptionError(false);
                    inscription.setErrorStatus("00");
                    return inscription;
                } else {
                    String msg = "-- NOT successful Insert in " + methodName + " row = " + row;
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    inscription.setInscriptionError(true);
                    inscription.setErrorStatus("90");
                    return inscription;
                }
            }

        } catch (SQLException sqle) {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                String msg = LCUtil.prepareMessageBean("create.inscription.duplicate");
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                inscription.setInscriptionOK(true);
                inscription.setInscriptionError(true);
                inscription.setErrorStatus("98");
                return inscription;
            }
            handleSQLException(sqle, methodName);
            inscription.setInscriptionError(true);
            inscription.setErrorStatus("99");
            return inscription;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            inscription.setInscriptionError(true);
            inscription.setErrorStatus("998");
            return inscription;
        }
    } // end method

    public ValidationsLC validate(final Round round, final Player player, final Inscription inscription,
            final Club club, final Course course) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        ValidationsLC v = new ValidationsLC();
        v.setStatus0(ValidationStatus.APPROVED.toString());
        v.setStatus1("");
        v.setStatus2("00");

        try {
            LOG.debug("starting validation before create inscription");

            List<Player> listPlayers = roundPlayersList.list(round);
            if (listPlayers == null) {
                LOG.debug("listPlayers is null");
                listPlayers = Collections.emptyList();
            }
            LOG.debug("number players already inscribed = {}", listPlayers.size());
            listPlayers.forEach(item -> LOG.debug("player = {} / {}", item.getIdplayer(), item.getPlayerLastName()));

            if (listPlayers.size() > 3) {
                v.setStatus0(ValidationStatus.REJECTED.toString());
                String msgerr = LCUtil.prepareMessageBean("inscription.toomuchplayers" + listPlayers.size());
                v.setStatus1(msgerr);
                v.setStatus2("01");
                return v;
            }

            LOG.debug("idplayer = {}", player.getIdplayer());
            LOG.debug("club = {}", club.getIdclub());
            LOG.debug("round date = {}", round.getRoundDate());

            // check duplicate BEFORE admin bypass — admin can also be already inscribed
            if (findInscriptionRound.find(round, player)) {  // déjà inscrit
                v.setStatus0(ValidationStatus.REJECTED.toString());
                String msg = LCUtil.prepareMessageBean("inscription.duplicate");
                v.setStatus1(msg);
                v.setStatus2("04");
                return v;
            }

            if ("ADMIN".equals(player.getPlayerRole())) {
                v.setStatus0(ValidationStatus.APPROVED.toString());
                String msg = LCUtil.prepareMessageBean("inscription.administrator");
                v.setStatus1(msg);
                v.setStatus2("00");
                return v;
            }

            Cotisation cotisation = findCotisationAtRoundDate.find(player, club, round);
            LOG.debug("cotisation at round date = {}", cotisation);
            LOG.debug("cotisation status = {}", (cotisation != null ? cotisation.getStatus() : "null"));

            if (findGreenfeePaid.find(player, round)) {
                v.setStatus0(ValidationStatus.APPROVED.toString());
                String msg = LCUtil.prepareMessageBean("inscription.greenfee");
                v.setStatus1(msg);
                return v;
            }

            if (cotisation != null && cotisation.getStatus().equals("Y")) {
                v.setStatus0(ValidationStatus.APPROVED.toString());
                String msg = LCUtil.prepareMessageBean("inscription.member");
                v.setStatus1(msg);
                v.setStatus2("00");
                return v;
            }

            if (cotisation != null && cotisation.getStatus().equals("NF")
                    && (!findGreenfeePaid.find(player, round))) {
                v.setStatus0(ValidationStatus.REJECTED.toString());
                v.setStatus1(LCUtil.prepareMessageBean("inscription.notmember.notgreenfee"));
                v.setStatus2("02");
                return v;
            }

            if (cotisation != null && cotisation.getStatus().equals("N")) {
                v.setStatus0(ValidationStatus.REJECTED.toString());
                v.setStatus1(LCUtil.prepareMessageBean("inscription.notmember"));
                v.setStatus2("03");
                return v;
            }

            return v;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            v.setStatus0(ValidationStatus.REJECTED.toString());
            return v;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            v.setStatus0(ValidationStatus.REJECTED.toString());
            return v;
        }
    } // end method
} // end class
