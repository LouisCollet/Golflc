package Controller.refact;

import entite.Club;
import entite.Player;
import entite.Round;
import entite.TarifGreenfee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.SQLException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * SimulationController — simulation greenfee sans paiement ni persistance.
 * Construit un Round et un Player factices, appelle FindTarifGreenfeeData
 * et CalcTarifGreenfee, et expose le résultat pour affichage.
 */
@Named("simC")
@SessionScoped
public class SimulationController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private context.ApplicationContext          appContext;
    @Inject private find.FindTarifGreenfeeData          findTarifGreenfeeData;
    @Inject private calc.CalcTarifGreenfee              calcTarifGreenfee;
    @Inject private lists.ClubList                      clubList;
    @Inject private lists.CourseListForClub             courseListForClub;

    // ── Inputs ──────────────────────────────────────────────────────────────
    private Integer   simClubId;        // lazy init from appContext
    private Integer   simCourseId;
    private LocalDateTime simDateTime  = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
    private Integer   simHoles         = 18;
    private LocalDate simBirthDate     = null; // lazy init from current player

    // ── Result ───────────────────────────────────────────────────────────────
    private TarifGreenfee simTarifGreenfee;
    private String        simError;

    public SimulationController() { }

    // ── Clubs for selector ───────────────────────────────────────────────────

    public List<entite.Club> getClubsForSim() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            List<Integer> validClubIds = findTarifGreenfeeData.findClubIdsWithTarifForCurrentYear();
            return clubList.list().stream()
                    .filter(c -> validClubIds.contains(c.getIdclub()))
                    .toList();
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ── Courses for selector ─────────────────────────────────────────────────

    public List<entite.Course> getCoursesForSim() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Club club = findSelectedClub();
        if (club == null) {
            return Collections.emptyList();
        }
        try {
            List<entite.Course> courses = courseListForClub.list(club);
            // auto-select when only one course exists
            if (courses.size() == 1 && simCourseId == null) {
                simCourseId = courses.get(0).getIdcourse();
                LOG.debug("auto-selected single course id={}", simCourseId);
            }
            return courses;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ── Navigation ───────────────────────────────────────────────────────────

    public String toSimulation() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        simClubId        = null;
        simCourseId      = null;
        simDateTime      = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        simHoles         = 18;
        simBirthDate     = null;
        simTarifGreenfee = null;
        simError         = null;
        return "greenfee_simulation.xhtml?faces-redirect=true";
    } // end method

    // ── Simulate ─────────────────────────────────────────────────────────────

    public void simulate() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        simTarifGreenfee = null;
        simError = null;
        try {
            if (simCourseId == null) {
                simError = "Please select a course.";
                return;
            }
            if (simDateTime == null) {
                simError = "Please select a date and time.";
                return;
            }

            // Round factice — jamais persisté
            Round round = new Round();
            round.setCourseIdcourse(simCourseId);
            round.setRoundDate(simDateTime);
            round.setRoundHoles(simHoles != null ? simHoles.shortValue() : (short) 18);

            // Player factice — date naissance pour calcul âge (type EQ/DA)
            Player player = new Player();
            player.setPlayerBirthDate(getSimBirthDate().atStartOfDay());

            LOG.debug("sim round = {}", round);

            // Find tarif
            TarifGreenfee tarif = findTarifGreenfeeData.find(round);
            if (tarif == null) {
                simError = "No tarif found for this course / date / holes combination.";
                return;
            }

            // Calc — use selected club (currency, rules)
            Club club = findSelectedClub();
            simTarifGreenfee = calcTarifGreenfee.calc(tarif, round, club, player);
            if (simTarifGreenfee == null) {
                simError = "Tarif calculation failed.";
            } else {
                LOG.debug("simulation result type={} season={}", simTarifGreenfee.getGreenfeeType(), simTarifGreenfee.getSeason());
            }

        } catch (Exception e) {
            handleGenericException(e, methodName);
            simError = e.getMessage();
        }
    } // end method

    // ── Private helpers ───────────────────────────────────────────────────────

    private Club findSelectedClub() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Integer id = getSimClubId();
        if (id == null) return appContext.getClub();
        try {
            return clubList.list().stream()
                    .filter(c -> id.equals(c.getIdclub()))
                    .findFirst()
                    .orElse(appContext.getClub());
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return appContext.getClub();
        }
    } // end method

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Integer getSimClubId() {
        if (simClubId == null && appContext.getClub() != null) {
            simClubId = appContext.getClub().getIdclub();
        }
        return simClubId;
    } // end method

    public void setSimClubId(Integer simClubId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.simClubId = simClubId;
        this.simCourseId = null;            // reset course when club changes
        courseListForClub.invalidateCache(); // reload courses for new club
        try {
            getCoursesForSim();             // triggers auto-select if single course
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public Integer getSimCourseId()                    { return simCourseId; }
    public void setSimCourseId(Integer simCourseId)    { this.simCourseId = simCourseId; }

    public LocalDateTime getSimDateTime()                     { return simDateTime; }
    public void setSimDateTime(LocalDateTime simDateTime)     { this.simDateTime = simDateTime; }

    public Integer getSimHoles()                       { return simHoles; }
    public void setSimHoles(Integer simHoles)          { this.simHoles = simHoles; }

    public LocalDate getSimBirthDate() {
        if (simBirthDate == null) {
            // default: current player's birth date, fallback to adult default
            if (appContext.getPlayer() != null && appContext.getPlayer().getPlayerBirthDate() != null) {
                simBirthDate = appContext.getPlayer().getPlayerBirthDate().toLocalDate();
            } else {
                simBirthDate = LocalDate.of(1970, 1, 1);
            }
        }
        return simBirthDate;
    } // end method

    public void setSimBirthDate(LocalDate simBirthDate) { this.simBirthDate = simBirthDate; }

    public java.util.List<jakarta.faces.model.SelectItem> getHolesItems() {
        return java.util.List.of(
            new jakarta.faces.model.SelectItem(Integer.valueOf(18), "18T"),
            new jakarta.faces.model.SelectItem(Integer.valueOf(9),  "9T")
        );
    } // end method

    public TarifGreenfee getSimTarifGreenfee()         { return simTarifGreenfee; }
    public String getSimError()                        { return simError; }

} // end class
