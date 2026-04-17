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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
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

            final String query = LCUtil.generateInsertQuery(conn, "player_has_round");
            try (PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setNull(1, java.sql.Types.INTEGER);
                ps.setInt(2, round.getIdround());
                ps.setInt(3, player.getIdplayer());
                ps.setInt(4, 0);  // FinalResult initial value
                ps.setString(5, inscription.getInscriptionMatchplayTeam());
                ps.setInt(6, 0);  // NotUsed2
                ps.setString(7, inscription.getInscriptionTeeStart());
                String s = inscription.getInscriptionTeeStart();
                LOG.debug("inscriptionTeeStart = {}", s);
                int tee = Integer.parseInt(s.substring(s.lastIndexOf("/") + 2));
                inscription.setInscriptionIdTee(tee);
                ps.setInt(8, inscription.getInscriptionIdTee());
                ps.setInt(9, invitedBy.getIdplayer());
                ps.setTimestamp(10, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);

                int row = ps.executeUpdate();
                if (row == 1) {
                    LOG.debug("InscriptionId created = {}", LCUtil.generatedKey(conn));
                    String msg = LCUtil.prepareMessageBean("inscription.ok") + " = " + inscription;
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
                String msg = "Cotisation pas trouvée, greenfee pas trouvé";
                v.setStatus1(msg);
                v.setStatus2("02");
                return v;
            }

            if (cotisation != null && cotisation.getStatus().equals("N")) {
                v.setStatus0(ValidationStatus.REJECTED.toString());
                String msg = "cotisation.notmember";
                v.setStatus1(msg);
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

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new CreateInscription().create(..., conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #create(Round, Player, Player, Inscription, Club, Course, String)} via injection CDI */
    /** @deprecated Utiliser {@link #validate(Round, Player, Inscription, Club, Course)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324714);
        player.setPlayerRole("ADMIN");
        Player invitedBy = player;
        Round round = new Round();
        round.setIdround(435);
        Club club = new Club();
        club.setIdclub(1135);
        Course course = new Course();
        course.setIdcourse(135);
        Inscription inscription = new Inscription();
        inscription.setInscriptionIdTee(154);
        String batch = "A";
        Inscription result = create(round, player, invitedBy, inscription, club, course, batch);
        LOG.debug("from main, result = {}", result);
    } // end main
    */

} // end class
