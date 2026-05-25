package Controllers;

import entite.Course;
import entite.Distance;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static utils.LCUtil.showMessageFatal;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Named
@ApplicationScoped
public class StablefordController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = utils.LCUtil.getCurrentClassName();

    @Inject private read.ReadScoreList readScoreListService;
    @Inject private read.ReadStatisticsList readStatisticsListService;
    @Inject private find.FindInfoStableford findInfoStablefordService;
    @Inject private read.ReadParAndStrokeIndex readParAndStrokeIndexService;
    @Inject private calc.CalcStablefordCourseHandicap calcCourseHandicapService;
    @Inject private calc.CalcStablefordPlayingHandicap calcPlayingHandicapService;
    @Inject private find.FindDistances findDistancesService;

    public ScoreStableford completeScoreStableford(final Player player, final Round round, Tee tee) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Round = {}", round);
        LOG.debug("with Player = {}", player);
        try {
            LoggingUserController.write(CLASSNAME + "." + methodName, "i");
            LoggingUserController.write("score stableford", "t");
            ScoreStableford scoreStableford = new ScoreStableford();
            ArrayList<ScoreStableford.Score> v1 = readScoreListService.read(player, round, tee);
            LOG.debug("result of readScoreList = {}", v1);
            if (v1.isEmpty()) {
                LOG.debug("it's the first time : no scores already registered !");
                scoreStableford = prepareView(player, round);
            } else {
                LOG.debug("scores were previously registered ! = {}", v1.size());
                scoreStableford.setScoreList(v1);
                scoreStableford.setStatisticsList(readStatisticsListService.load(player, round));
                scoreStableford.setShowButtonStatistics(true);
            }
            LOG.debug("returned scoreStableford = {}", scoreStableford);
            return scoreStableford;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public ScoreStableford prepareView(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Round = {}", round);
        LOG.debug("with Player = {}", player);
        try {
            ECourseList ecl = findInfoStablefordService.find(player, round);
            Tee tee = ecl.tee();
            Course course = ecl.course();
            ScoreStableford scoreStableford = new ScoreStableford();
            scoreStableford = readParAndStrokeIndexService.read(course, scoreStableford);
            LOG.debug("score with par and index = {}", scoreStableford);
            scoreStableford = calcCourseHandicapService.calc(scoreStableford, player, round, tee);
            LOG.debug("courseHandicap = {}", scoreStableford.getCourseHandicap());
            LOG.debug("HandicapIndex player WHS = {}", scoreStableford.getPlayerHandicapWHS());
            int playingHandicap = calcPlayingHandicapService.calc(scoreStableford, player, round);
            scoreStableford.setPlayingHandicap(playingHandicap);
            LOG.debug("playingHandicap = {}", scoreStableford.getPlayingHandicap());
            scoreStableford.setExtraArray(completeWithExtraStrokesNew(scoreStableford, round));
            LOG.debug("ExtraArray completed = {}", scoreStableford);
            scoreStableford.setDistanceArray(completeWithDistances(scoreStableford, tee));
            LOG.debug("DistanceArray completed = {}", scoreStableford);
            scoreStableford.setTotalStrokes(Arrays.stream(scoreStableford.getStrokeArray()).sum());
            var v = readStatisticsListService.load(player, round);
            if (v.isEmpty()) {
                LOG.debug("statisticsList is empty");
            } else {
                scoreStableford.setStatisticsList(v);
                LOG.debug("statistics setted = {}", scoreStableford.getStatisticsList().toString());
            }
            scoreStableford.setStart(round.getRoundStart());
            scoreStableford.setHoles(round.getRoundHoles());
            scoreStableford.setScoreList(completeScoreList(scoreStableford));
            scoreStableford.setShowCreate(false);
            LOG.debug("scoreList setted for rows not = 0 = {}", scoreStableford.getScoreList().toString());
            return scoreStableford;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public int[] completeWithDistances(ScoreStableford scoreStableford, Tee tee) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with tee = {}", tee);
        try {
            Distance d = findDistancesService.find(tee);
            LOG.debug("array distances = {}", Arrays.toString(d.getDistanceArray()));
            return d.getDistanceArray();
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // Flux : handicap de jeu → coups reçus par trou selon stroke index
    public int[] completeWithExtraStrokesNew(ScoreStableford score, Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LoggingUserController.write(CLASSNAME + "." + methodName, "i");
        LoggingUserController.write("extra strokes", "t");
        try {
            int holes = round.getRoundHoles();
            LOG.debug("holes via round = {}", holes);
            int start = round.getRoundStart();
            LOG.debug("start via round = {}", start);
            LOG.debug("playingHandicap = {}", score.getPlayingHandicap());
            int complete = score.getPlayingHandicap() / holes;
            LOG.debug("-- loop Complete = {}", complete);
            LoggingUserController.write(" - loop Complete = " + complete);
            int uncomplete = score.getPlayingHandicap() % holes;
            LOG.debug("-- loop Uncomplete = {}", uncomplete);
            LoggingUserController.write(" - loop Uncomplete = " + uncomplete);
            LOG.debug("-- ArrayIndex input = {} control sum={} must be {}", Arrays.toString(score.getIndexArray()),
                    IntStream.of(score.getIndexArray()).sum(), IntStream.rangeClosed(1, 18).sum());

            // 0. Slicing the arrays
            int[] sliced = null;
            if (holes == 9 && start == 1) {
                sliced = utils.LCUtil.findSlice(score.getIndexArray(), start - 1, holes);
                LOG.debug("-- sliced v1 1,9 = {}", Arrays.toString(sliced));
            }
            if (holes == 9 && start == 10) {
                sliced = utils.LCUtil.findSlice(score.getIndexArray(), start - 1, start - 1 + holes);
                LOG.debug("-- sliced v1 10,9 = {}", Arrays.toString(sliced));
            }
            if (holes == 18) {
                sliced = score.getIndexArray();
            }

            // 1. Load list from array
            Integer[] arrayIndex = Arrays.stream(sliced).boxed().toArray(Integer[]::new);
            LOG.debug("arrayIndex is now = {}", Arrays.toString(arrayIndex));
            List<ScoreStableford.ExtraClass> listExtra = new ArrayList<>();
            for (int i = 0; i < arrayIndex.length; i++) {
                ScoreStableford.ExtraClass extra = new ScoreStableford.ExtraClass(i + start, arrayIndex[i]);
                listExtra.add(extra);
            }
            LOG.debug("-- listExtra loaded from arrayIndex with hole and index = {}", listExtra);

            // 2. Complete list with complete extra
            for (int i = 0; i < holes; i++) {
                listExtra.get(i).setExtra(complete);
            }
            LOG.debug("-- liste extra completed with complete strokes = {}", listExtra);

            // 3. Sort list on index
            List<ScoreStableford.ExtraClass> sortedlistExtra = listExtra.stream()
                    .sorted(Comparator.comparingInt(ScoreStableford.ExtraClass::getIndex))
                    .collect(Collectors.toList());
            LOG.debug("-- liste extra sorted on index= {}", sortedlistExtra);

            // 4. Complete list with uncomplete
            for (int i = 0; i < uncomplete; i++) {
                int e = sortedlistExtra.get(i).getExtra();
                sortedlistExtra.get(i).setExtra(e + 1);
            }
            LOG.debug("-- liste extra completed with strokes = {}", sortedlistExtra);

            // 5. Sort list back on hole
            listExtra = listExtra.stream()
                    .sorted(Comparator.comparingInt(ScoreStableford.ExtraClass::getHole))
                    .collect(Collectors.toList());
            LOG.debug("-- liste extra sorted back on hole = {}", listExtra);

            // 6. Convert back list to extraArray
            int[] extraArray = listExtra.stream()
                    .mapToInt(x -> x.getExtra())
                    .toArray();
            LOG.debug("-- array extraArray = {}", Arrays.toString(extraArray));
            LOG.debug("-- total strokes = {}", IntStream.of(extraArray).sum());

            // 7. Special handling 9 holes
            if (holes == 9) {
                int[] zero = new int[9];
                if (start == 10) {
                    extraArray = IntStream
                            .concat(IntStream.of(zero), IntStream.of(extraArray))
                            .toArray();
                }
                if (start == 1) {
                    extraArray = IntStream
                            .concat(IntStream.of(extraArray), IntStream.of(zero))
                            .toArray();
                }
                LOG.debug("-- extraArray concatenated to 18 holes = {}", Arrays.toString(extraArray));
            }

            return extraArray;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public ScoreStableford completeWithStatistics(ScoreStableford scoreStableford) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            int start = scoreStableford.getStart();
            int holes = scoreStableford.getHoles();
            for (int i = start - 1; i < start + holes - 1; i++) {
                ScoreStableford.Statistics sta = scoreStableford.new Statistics();
                sta.setHole(i + 1);
                sta.setPar(scoreStableford.getParArray()[i]);
                sta.setStroke(scoreStableford.getStrokeArray()[i]);
                sta.setFairway(0);
                sta.setGreen(0);
                sta.setPutt(0);
                sta.setBunker(0);
                sta.setPenalty(0);
                scoreStableford.getStatisticsList().add(sta);
            }
            return scoreStableford;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public ArrayList<ScoreStableford.Score> completeScoreList(ScoreStableford scoreStableford) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("scoreStableford = {}", scoreStableford);
        try {
            ArrayList<ScoreStableford.Score> scoreList = new ArrayList<>();
            int start = scoreStableford.getStart() - 1;
            LOG.debug("start = {}", start);
            int holes = scoreStableford.getHoles();
            LOG.debug("holes = {}", holes);
            int stop = start + holes;
            LOG.debug("stop = {}", stop);
            LOG.debug("before loading scoreList, distance array = {}", Arrays.toString(scoreStableford.getDistanceArray()));
            for (int i = start; i < stop; i++) {
                ScoreStableford.Score score = scoreStableford.new Score();
                score.setHole(i + 1);
                score.setPar(scoreStableford.getParArray()[i]);
                score.setIndex(scoreStableford.getIndexArray()[i]);
                score.setExtra(scoreStableford.getExtraArray()[i]);
                score.setPoints(scoreStableford.getPointsArray()[i]);
                score.setStrokes(scoreStableford.getStrokeArray()[i]);
                score.setDistances(scoreStableford.getDistanceArray()[i]);
                scoreList.add(score);
            }
            LOG.debug("completed scoreList = {}", scoreList.toString());
            if (scoreList.isEmpty()) {
                String msg = "generated scoreList is empty!";
                LOG.error(msg);
                showMessageFatal(msg);
            }
            return scoreList;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public ArrayList<ScoreStableford.Score> completeScoreListWithStrokes(ScoreStableford score) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        var scoreList = score.getScoreList();
        var strokesArray = score.getStrokeArray();
        try {
            if (score.getStart() == 1) {
                for (int i = 0; i < scoreList.size(); i++) {
                    scoreList.get(i).setStrokes(strokesArray[i]);
                }
            }
            if (score.getStart() == 10) {
                for (int i = 9; i < 18; i++) {
                    scoreList.get(i - 9).setStrokes(strokesArray[i]);
                }
            }
            LOG.debug("scoreList with strokes = {}", scoreList.toString());
            return scoreList;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public ArrayList<ScoreStableford.Score> completeScoreListWithPoints(ScoreStableford score) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        var scoreList = score.getScoreList();
        var pointsArray = score.getPointsArray();
        try {
            if (score.getStart() == 1) {
                for (int i = 0; i < scoreList.size(); i++) {
                    scoreList.get(i).setPoints(pointsArray[i]);
                }
            }
            if (score.getStart() == 10) {
                for (int i = 9; i < 18; i++) {
                    scoreList.get(i - 9).setPoints(pointsArray[i]);
                }
            }
            LOG.debug("scoreList points modified = {}", scoreList.toString());
            return scoreList;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    public ArrayList<ScoreStableford.Statistics> completeStatisticsListWithStrokes(
            ArrayList<ScoreStableford.Statistics> statisticsList, int[] strokeArray) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("statisticsList = {}", statisticsList);
        LOG.debug("strokeArray = {}", Arrays.toString(strokeArray));
        try {
            for (int i = 0; i < statisticsList.size(); i++) {
                LOG.debug("i iteration = {}", i);
                statisticsList.get(i).setStroke(strokeArray[i]);
            }
            LOG.debug("scoreList points added = {}", statisticsList.toString());
            return statisticsList;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

} // end class
