package lists;

import entite.CompetitionData;
import entite.CompetitionDescription;
import entite.CompetitionDescription.StatusExecution;
import entite.Course;
import entite.composite.ECompetition;
import entite.PlayingHandicap;
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Named
@ApplicationScoped
public class CompetitionStartList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private lists.TeesCourseList          teesCourseList;
    @Inject private update.UpdateCompetitionData  updateCompetitionData;

    public CompetitionStartList() { }

    public List<ECompetition> list(final List<ECompetition> listeInscriptions) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            CompetitionDescription cde = listeInscriptions.get(0).competitionDescription();
            LOG.debug(methodName + " - for competition Description = " + cde);
            LOG.debug(methodName + " - status = " + cde.getCompetitionStatus());
            LOG.debug(methodName + " - execution = " + cde.getCompetitionExecution());
            String execution = listeInscriptions.get(0).competitionDescription().getCompetitionExecution();
            LOG.debug(methodName + " - execution type description = " + execution);

            if (Integer.parseInt(cde.getCompetitionStatus()) > 0) {
                cde.setCompetitionExecution(StatusExecution.PROVISIONAL.toString());
                LOG.debug(methodName + " - execution forced to PROVISIONAL");
            }

            if (execution.equals(StatusExecution.PROVISIONAL.name())
                    || execution.equals(StatusExecution.FINAL.toString())) {
                LOG.debug(methodName + " - good execution type - PROVISIONAL or FINAL");
            } else {
                LOG.debug(methodName + " - wrong execution type = " + execution);
                return Collections.emptyList();
            }

            LOG.debug(methodName + " - before listSortAndComplete");
            var li = this.listSortAndComplete(listeInscriptions);
            LOG.debug(methodName + " - after listSortAndComplete");

            if (execution.equals(StatusExecution.FINAL.toString())) {
                LOG.debug(methodName + " - this is a final execution !");
                this.modifyCompetition(li);
            }

            li.forEach(item -> LOG.debug("Flight number " + item.competitionData().getCmpDataFlightNumber()
                    + " - Flight start time " + item.competitionData().getCmpDataFlightStart()));
            return li;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECompetition> listSortAndComplete(final List<ECompetition> li) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            CompetitionDescription cd = li.get(0).competitionDescription();
            LOG.debug(methodName + " - for competition Description = " + cd);
            String exec = cd.getCompetitionExecution();
            LOG.debug(methodName + " - for listSortAndComplete execution type = " + exec);
            LOG.debug(methodName + " - Status = " + cd.getCompetitionStatus());

            li.forEach(item -> LOG.debug("Liste before tri "
                    + item.competitionData().getCmpDataPlayerId() + " /"
                    + item.competitionData().getCmpDataAskedStartTime()));

            Collections.sort(li, Comparator
                    .comparing((ECompetition p) -> p.competitionData().getCmpDataAskedStartTime())
                    .thenComparing(Comparator.comparingDouble(
                            (ECompetition p) -> p.competitionData().getCmpDataHandicap()).reversed())
                    .thenComparing((ECompetition p) -> p.competitionData().getCmpDataId()));

            li.forEach(item -> LOG.debug("Liste after tri "
                    + item.competitionData().getCmpDataPlayerId() + " / "
                    + item.competitionData().getCmpDataAskedStartTime()));

            Course course = new Course();
            course.setIdcourse(cd.getCompetitionCourseId());
            List<Tee> tees = teesCourseList.list(course.getIdcourse());
            LOG.debug(methodName + " - tee size = " + tees.size());

            LocalDateTime ldt = cd.getCompetitionDate();
            LocalTime lt = ldt.toLocalTime().minusMinutes(12);
            int playersFlight = cd.getFlightNumberPlayers();
            LOG.debug(methodName + " - nombre de joueurs par flight = " + playersFlight);
            int flight = 0;

            for (int i = 0; i < li.size(); i++) {
                LOG.debug(methodName + " - i = " + i);
                if ((i + 1) % playersFlight == 1) {
                    flight++;
                    lt = lt.plusMinutes(12);
                    LOG.debug(methodName + " - flight is now = " + flight);
                }
                var cda = li.get(i).competitionData();
                LOG.debug(methodName + " - cda handled = " + cda + " for i = " + i);
                cda.setCmpDataFlightStart(lt);
                cda.setCmpDataFlightNumber((short) flight);
                li.get(i).withCompetitionData(cda);
                LOG.debug(methodName + " - cda out - liste get(i) = " + li.get(i));
            } // end for

            li.forEach(item -> LOG.debug("cda completed Flight number and start time "
                    + item.competitionData().getCmpDataFlightNumber()
                    + " - Flight start time " + item.competitionData().getCmpDataFlightStart()));
            return li;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public boolean modifyCompetition(final List<ECompetition> ec) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            for (int i = 0; i < ec.size(); i++) {
                CompetitionData cda = ec.get(i).competitionData();
                var v = ec.get(i).competitionData();
                cda.setCmpDataPlayingHandicap(v.getCmpDataPlayingHandicap());
                cda.setCmpDataHandicap(v.getCmpDataHandicap());
                cda.setCmpDataFlightStart(v.getCmpDataFlightStart());
                cda.setCmpDataFlightNumber(v.getCmpDataFlightNumber());
                ec.get(i).withCompetitionData(cda);
                if (updateCompetitionData.update(ec.get(i).competitionData())) {
                    LOG.debug(methodName + " - competitionData is updated ! for i = " + i);
                    LOG.debug(methodName + " - for cda = " + ec.get(i).competitionData());
                } else {
                    String msg = methodName + " - ModifyCompetitionData NOT updated for i = " + i;
                    LOG.error(msg);
                    utils.LCUtil.showMessageFatal(msg);
                    return false;
                }
            } // end loop
            return true;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public PlayingHandicap playingHandicap(final ECompetition competition, final List<Tee> tees) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug(methodName + " - with tees = " + tees);
            LOG.debug(methodName + " - with seriesHandicap = "
                    + Arrays.deepToString(competition.competitionDescription().getSeriesHandicap()));
            LOG.debug(methodName + " - limit white = "
                    + competition.competitionDescription().getSeriesHandicap()[0][1]);

            String playerGender = competition.competitionData().getCmpDataPlayerGender();
            LOG.debug(methodName + " - playerGender = " + playerGender);

            String teeStart = "YELLOW";
            LOG.debug(methodName + " - forced TeeStart = " + teeStart);

            PlayingHandicap plh = new PlayingHandicap();
            Optional<Tee> tee = tees.stream()
                    .filter((Tee p) -> playerGender.equals(p.getTeeGender()) && teeStart.equals(p.getTeeStart()))
                    .findFirst();

            if (tee.isPresent()) {
                LOG.debug(methodName + " - is Present = " + tee.get());
            } else {
                LOG.error(methodName + " - selectedTee : no value found");
                return plh;
            }
            LOG.debug(methodName + " - selectedTee = " + tee.get());

            double exact_hcp = competition.competitionData().getCmpDataHandicap();
            LOG.debug(methodName + " - exact handicap = " + exact_hcp);
            plh.setHandicapPlayerEGA(exact_hcp);
            double slope = tee.get().getTeeSlope();
            LOG.debug(methodName + " - slope = " + slope);
            plh.setTeeSlope((int) slope);
            double rating = tee.get().getTeeRating().doubleValue();
            LOG.debug(methodName + " - rating = " + rating);
            plh.setTeeRating(rating);
            double par = tee.get().getTeePar();
            LOG.debug(methodName + " - par = " + par);
            plh.setCoursePar((int) par);
            int nholes = 18;
            LOG.debug(methodName + " - forced holes = " + nholes);
            plh.setRoundHoles((short) nholes);
            int category;
            if (exact_hcp > 36 && exact_hcp < 55) {
                LOG.debug(methodName + " - Player category 6 - Club Handicap");
                category = 6;
            } else {
                LOG.debug(methodName + " - Player category 1-5");
                category = 15;
            }
            int playing_hcp = 0;
            if (category == 15) {
                LOG.debug(methodName + " - calculating playing hcp for categories 1 to 5");
                if (nholes == 18) {
                    playing_hcp = (int) Math.round((exact_hcp * (slope / 113.0)) + (rating - par));
                    LOG.debug(methodName + " - calculated playing hcp for categories 1 to 5, 18 holes");
                } else {
                    playing_hcp = (int) Math.round((exact_hcp * (slope / 113.0)) / 2 + ((rating / 2) - par));
                    LOG.debug(methodName + " - calculated playing hcp 9 holes = " + playing_hcp);
                }
            }
            plh.setPlayingHandicap(playing_hcp);
            return plh;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // CompetitionStartList does not cache — list() always recomputes
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - no-op: CompetitionStartList does not maintain a cache");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // var lp = list(listeInscriptions);
        // LOG.debug("from main, list = " + lp);
        LOG.debug("from main, CompetitionStartList = ");
    } // end main
    */

} // end class
