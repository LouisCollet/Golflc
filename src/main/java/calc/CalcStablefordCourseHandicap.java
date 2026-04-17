package calc;

import Controllers.LoggingUserController;
import entite.HandicapIndex;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;

@ApplicationScoped
public class CalcStablefordCourseHandicap implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = utils.LCUtil.getCurrentClassName();

    @Inject
    private find.FindHandicapIndexAtDate findHandicapIndexAtDateService;

    /**
     * Calcule le Course Handicap pour un score stableford
     * @param score le score à compléter
     * @param player le joueur
     * @param round le round
     * @param tee le tee joué
     * @return score avec courseHandicap calculé
     */
    public ScoreStableford calc(final ScoreStableford score, final Player player, final Round round, final Tee tee) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            LOG.debug("entering " + methodName);
            LoggingUserController.write(CLASSNAME + "." + methodName, "i");

            // ✅ Trouver le handicap index à la date du round
            HandicapIndex handicapIndex = new HandicapIndex();
            handicapIndex.setHandicapPlayerId(player.getIdplayer());
            handicapIndex.setHandicapDate(round.getRoundDate());

            LOG.debug("start calculations");
            var hi = findHandicapIndexAtDateService.find(handicapIndex);  // ✅ Sans conn
            double handicapWHS = hi.getHandicapWHS().doubleValue();

            LOG.debug("Player HandicapIndex WHS = " + handicapWHS);
            LoggingUserController.write(LocalDateTime.now().format(ZDF_TIME), "i");
            LoggingUserController.write(player.getPlayerFirstName() + " - " + player.getPlayerLastName(), "i");
            LoggingUserController.write("round name = " + round.getRoundName() + " - " + round.getRoundDate().format(ZDF_TIME), "t");
            LoggingUserController.write("handicapWHS = " + handicapWHS);

            handicapIndex.setHandicapWHS(BigDecimal.valueOf(handicapWHS));
            score.setPlayerHandicapWHS(handicapIndex.getHandicapWHS().doubleValue());
            score.setHandicapType("WHS");

            // ✅ Calculer le course handicap
            BigDecimal courseHandicap = courseHandicap(handicapIndex, tee, round);
            LoggingUserController.write("courseHandicap = " + courseHandicap);
            LOG.debug("-- courseHandicap = " + courseHandicap);

            score.setCourseHandicap(courseHandicap.intValue());

            return score;

        } catch (SQLException sqle) {
            String msg = "SQLException in " + methodName
                    + " -- ErrorCode = " + sqle.getErrorCode()
                    + " -- SQLSTATE = " + sqle.getSQLState();
            LOG.error(msg, sqle);
            throw sqle;

        } catch (Exception ex) {
            String msg = "Exception in " + methodName + ": " + ex.getMessage();
            LOG.error(msg, ex);
            throw new SQLException(msg, ex);
        }
    } // end method

    /**
     * Calcule le Course Handicap selon la formule WHS
     * Course Handicap = Handicap Index × (Slope Rating / 113) + (Course Rating − Par)
     * Pour 9 trous, diviser le Handicap Index par 2
     */
    public BigDecimal courseHandicap(HandicapIndex handicapIndex, Tee tee, Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            LOG.debug("entering " + methodName);
            LOG.debug("with handicapIndex = " + handicapIndex);
            LOG.debug("with tee = " + tee.toString());
            LOG.debug("with round = " + round.toString());

            LoggingUserController.write(CLASSNAME + "." + methodName, "i");
            LoggingUserController.write("Course Handicap", "t");
            LoggingUserController.write("Course Id" + round.getCourseIdcourse());

            BigDecimal handicapWHS = handicapIndex.getHandicapWHS();
            LOG.debug("HandicapWHS from database = " + handicapWHS);

            BigDecimal slopeRating = new BigDecimal(tee.getTeeSlope());
            LOG.debug("slopeRating = " + slopeRating);

            BigDecimal courseRating = tee.getTeeRating();
            LOG.debug("Course Rating = " + courseRating);

            BigDecimal par = new BigDecimal(tee.getTeePar().intValue());
            LOG.debug("Par = " + par);

            int nholes = round.getRoundHoles();
            LOG.debug("holes = " + nholes);

            // ===== 9 HOLES =====
            if (nholes == 9) {
                LOG.debug("9 holes paragraph");
                LOG.debug("slope rating / 113 = " + slopeRating.divide(new BigDecimal("113.0"), MathContext.DECIMAL32));
                LOG.debug("courseRating - par = " + courseRating.subtract(par));
                LOG.debug("Handicap Index / 2 = " + handicapWHS.divide(new BigDecimal("2.0"), MathContext.DECIMAL32));

                BigDecimal courseHandicap =
                        (handicapWHS.divide(new BigDecimal("2.0"), MathContext.DECIMAL32)
                                .multiply(slopeRating.divide(new BigDecimal("113.0"), MathContext.DECIMAL32))
                                .add(courseRating.subtract(par))
                        );

                LOG.debug("courseHandicap for 9 holes = " + courseHandicap);
                courseHandicap = courseHandicap.setScale(0, RoundingMode.HALF_EVEN);
                LOG.debug("courseHandicap 9 holes rounded = " + courseHandicap);

                LoggingUserController.write("Course handicap 9 holes = HandicapIndex X SlopeRating/113 + Course Rating - Par)", "b");

                StringBuilder sb = new StringBuilder();
                sb.append(handicapIndex.getHandicapWHS()).append("/2 ");
                sb.append(" X ");
                sb.append(slopeRating).append("/113");
                sb.append(" + ").append(courseRating).append(" - ").append(par);
                LoggingUserController.write(sb.toString(), "b");

                return courseHandicap;
            }

            // ===== 18 HOLES =====
            if (nholes == 18) {
                LOG.debug("slope rating / 113 = " + slopeRating.divide(new BigDecimal("113.0"), MathContext.DECIMAL32));
                LOG.debug("courseRating - par = " + courseRating.subtract(par));

                BigDecimal courseHandicap =
                        (handicapWHS
                                .multiply(slopeRating.divide(new BigDecimal("113.0"), MathContext.DECIMAL32))
                                .add(courseRating.subtract(par))
                        );

                LOG.debug("courseHandicap for 18 holes = " + courseHandicap);

                LoggingUserController.write("CourseHandicap 18 holes = Handicap Index X Slope Rating/113 + CourseRating - Par", "b");

                StringBuilder sb = new StringBuilder();
                sb.append("Course Handicap 18 holes = Handicap Index");
                sb.append(handicapIndex);
                sb.append(" X ");
                sb.append(slopeRating);
                sb.append("/113 + ");
                sb.append(courseRating);
                sb.append(" - ");
                sb.append(par);
                LoggingUserController.write(sb.toString(), "b");
                LoggingUserController.write("courseHandicap 18 holes = " + courseHandicap);

                courseHandicap = courseHandicap.setScale(0, RoundingMode.HALF_EVEN);
                LOG.debug("courseHandicap 18 holes rounded = " + courseHandicap);

                LoggingUserController.write("Course handicap = HandicapIndex X SlopeRating/113 + Course Rating - Par)", "b");
                LoggingUserController.write("courseHandicap 18 holes rounded = " + courseHandicap);

                return courseHandicap;
            }

            // Cas non géré (ni 9 ni 18 trous)
            throw new SQLException("Invalid number of holes: " + nholes);

        } catch (Exception ex) {
            String error = "Exception in " + methodName + ": " + ex.getMessage();
            LOG.error(error, ex);
            throw new SQLException(error, ex);
        }
    } // end method
} // end class
