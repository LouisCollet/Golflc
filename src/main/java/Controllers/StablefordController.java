
package Controllers;

import entite.Club;
import entite.Course;
import entite.Distance;
import entite.composite.ECourseList;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import find.FindDistances;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

public class StablefordController implements interfaces.Log{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
// public TarifGreenfeeController(){  // constructor
//    }

public ScoreStableford completeScoreStableford(final Player player, final Round round, Tee tee,final Connection conn) throws SQLException{   
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("... entering " + methodName);
    LOG.debug(" with Round = " + round);
    LOG.debug(" with Player = " + player);
    LOG.debug("filling only - no score calculations");
//    LOG.debug(" with score input = " + score);
try{
     LoggingUserController.write(CLASSNAME + "." + methodName,"i"); 
     LoggingUserController.write("score stableford", "t");
    
     ScoreStableford scoreStableford = new ScoreStableford();
     ArrayList<ScoreStableford.Score> v1 = new read.ReadScoreList().read(player,round,tee,conn);
       LOG.debug("result of readScoreList = " + v1);
     if(v1.isEmpty()){
            LOG.debug(" it's the first time : no scores already registered ! = ");
         scoreStableford = new StablefordController().prepareView(player,round, conn);
     }else{
            LOG.debug("scores were previously registered ! = " + v1.size());
            LOG.debug("scoreStableford score<list completed with distance = " + v1);
         scoreStableford.setScoreList(v1);
         scoreStableford.setStatisticsList(new read.ReadStatisticsList().load(conn, player, round));
         scoreStableford.setShowButtonStatistics(true);
  //           LOG.debug("statistics setted for rows NOT = 0 = "+ scoreStableford.getStatisticsList().toString());
     }
     LOG.debug("returned scoreStableford = " + scoreStableford);
  return scoreStableford;
      
 }catch (Exception e){
     LOG.debug("Exception in " + methodName + e);
     return null;
 }finally{ }
} // end method calc

public int[] completeScoreListWithDistances(ArrayList<ScoreStableford.Score> scoreList, Tee tee, Connection conn) throws SQLException{
    // new 17-08-203
        LOG.debug("entering completeScoreListWithDistances, scoreList =  " + scoreList);
        LOG.debug("  with tee =  " + tee);
    Distance distance = new FindDistances().find(tee, conn);
        LOG.debug("!!! scoreList has to be completed with array distances = !!!" + Arrays.toString(distance.getDistanceArray()));
    // compléter scorelist distances
 //   int [] pointsArray = new int[18];
 //   for(int i=0;i<scoreList.size();i++){ // index de la liste, idiot !
 //        LOG.debug(" i iteration 1 = "+ i);
    //   scoreList.get(i).getPoints().setPoints(pointsArray[i]);
  //     pointsArray[i] = scoreList.get(i).getPoints();
  //    }
  //  LOG.debug("points array reconstituée = " + Arrays.toString(pointsArray));
    return null;
}

/*
public int[] completeArrayPoints(ArrayList<ScoreStableford.Score> scoreList){
    int [] pointsArray = new int[18];
    for(int i=0;i<scoreList.size();i++){ // index de la liste, idiot !
 //        LOG.debug(" i iteration 1 = "+ i);
    //   scoreList.get(i).getPoints().setPoints(pointsArray[i]);
       pointsArray[i] = scoreList.get(i).getPoints();
      }
    LOG.debug("points array reconstituée = " + Arrays.toString(pointsArray));
    return null;
}
public int[] completeArrayStrokes(ArrayList<ScoreStableford.Score> scoreList){
    int [] strokesEurrray = new int[18];
    for(int i=0;i<scoreList.size();i++){ // index de la liste, idiot !
 //        LOG.debug(" i iteration 1 = "+ i);
    //   scoreList.get(i).getPoints().setPoints(pointsArray[i]);
       strokesEurrray[i] = scoreList.get(i).getPoints();
      }
    LOG.debug("strokes array reconstituée = " + Arrays.toString(strokesEurrray));
    return null;
}
*/

 public ScoreStableford prepareView(final Player player, final Round round, final Connection conn) throws SQLException{   
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("... entering " + methodName);
    LOG.debug(" with Round = " + round);
    LOG.debug(" with Player = " + player);
    LOG.debug("filling only - no score calculations");
//    LOG.debug(" with score input = " + score);
try{
          // a besoin de par index et extra
            ECourseList ecl = new find.FindInfoStableford().find(player, round, conn);
            Tee tee = ecl.getTee();
            Course course = ecl.getCourse();
            ScoreStableford scoreStableford = new ScoreStableford();
            scoreStableford = new read.ReadParAndStrokeIndex().read(conn, course, scoreStableford); // from hole
                LOG.debug("score with par and index = " + scoreStableford);
            scoreStableford = new calc.CalcStablefordCourseHandicap().calc(scoreStableford, player, round, tee, conn);
                LOG.debug("courseHandicap = " + scoreStableford.getCourseHandicap());
                LOG.debug("HandicapIndex player WHS = " + scoreStableford.getPlayerHandicapWHS());
            int i = new calc.CalcStablefordPlayingHandicap().calc(scoreStableford, player, round);
            scoreStableford.setPlayingHandicap(i);
                LOG.debug(" playingHandicap = " + scoreStableford.getPlayingHandicap()); 
            scoreStableford.setExtraArray(completeWithExtraStrokesNew(scoreStableford, round));
                LOG.debug("ExtraArray completed = " + scoreStableford);
            scoreStableford.setDistanceArray(completeWithDistances(scoreStableford, tee, conn));
                 LOG.debug("DistanceArray completed = " + scoreStableford); //ng()toString(scoreStableford.getExtraArray()));
            scoreStableford.setTotalStrokes(Arrays.stream(scoreStableford.getStrokeArray()).sum());
            var v = new read.ReadStatisticsList().load(conn, player, round);
            if(v.isEmpty()){
                LOG.debug("statisticsList is empty");
            }else{
                scoreStableford.setStatisticsList(v);
                LOG.debug("statistics setted = " + scoreStableford.getStatisticsList().toString());
            }
            scoreStableford.setStart(round.getRoundStart());
            scoreStableford.setHoles(round.getRoundHoles());
    // explication : on travaille en array mais ici on va compléter une liste (plus lisible)qui sera utilisée dans la dataTable de
    // score_Stableford.xhtml 
            scoreStableford.setScoreList(completeScoreList(scoreStableford));
            scoreStableford.setShowCreate(false);
                 LOG.debug("scoreList setted for rows not = 0 = "+ scoreStableford.getStatisticsList().toString());
     return scoreStableford;
   }catch (Exception e){

      String msg = " -- Exception Error in " + methodName + e;
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return null;
 }finally{ }
} // end method calc  
    
 
 public static int[] completeWithExtraStrokesNew (ScoreStableford score, Round round){
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
     LOG.debug(" ... entering " + methodName  + " for score = " + score);
//     LOG.debug(" with round = " + round);
     LoggingUserController.write(CLASSNAME + "." + methodName,"i");
     LoggingUserController.write("extra strokes", "t");
try{
    int holes = round.getRoundHoles();
       LOG.debug("holes via round = " + holes);
    int start = round.getRoundStart();
       LOG.debug("start via round = " + start);
     LOG.debug("playingHandicap = " + score.getPlayingHandicap());  
    int complete = score.getPlayingHandicap() / holes;
       LOG.debug("--  loop Complete = " + complete);
       LoggingUserController.write(" - loop Complete = " + complete);
    int uncomplete = score.getPlayingHandicap() % holes;
       LOG.debug("--  loop Uncomplete = " + uncomplete);
        LoggingUserController.write(" - loop Uncomplete = " + uncomplete);
        
        LOG.debug(" -- ArrayIndex input = " + Arrays.toString(score.getIndexArray()) 
                + " control : sum must be 171 ! second calcul = " + IntStream.of(score.getIndexArray()).sum()
                + "must be also " + IntStream.rangeClosed(1, 18).sum());
    
 // 0. slicing the arrays in function of holes and start
    int[] sliced = null;
    if(holes == 9 && start == 1){
       sliced = utils.LCUtil.findSlice(score.getIndexArray(), start-1, holes);
        LOG.debug(" -- sliced v1 1,9  = " + Arrays.toString(sliced));
 //       var v1 = IntStream.range(start, uncomplete);
    }  
    if(holes == 9 && start == 10){
      sliced = utils.LCUtil.findSlice(score.getIndexArray(), start-1, start-1+holes);
        LOG.debug(" -- sliced v1 10,9 = " + Arrays.toString(sliced));
    }
    if(holes == 18){
        sliced = score.getIndexArray();
    }
    
// 1. load list from array : hole and index
// wrap a primitive value and use it like an object
// you need to put a primitive value into a generic collection, These wrappers can be useful when you need to put a primitive
// value into a generic collection, which only accepts reference objects.
  //  Integer[] arrayIndex = utils.LCUtil.intToInteger(sliced); // convert int[] to Integer[]
  // boxed() method that returns a Stream consisting of the elements of the given stream, each boxed to an object of the corresponding wrapper class.
    Integer[] arrayIndex = Arrays.stream(sliced).boxed().toArray(Integer[]::new);
            LOG.debug("arrayIndex is now = " + Arrays.toString(arrayIndex));
    List<ScoreStableford.ExtraClass> listExtra = new ArrayList<>(); // create work list
 
    for(int i=0; i<arrayIndex.length; i++){ // complete work list
         ScoreStableford.ExtraClass extra = new ScoreStableford.ExtraClass(i+start, arrayIndex[i]); // see constructor !!
      //   LOG.debug(" i iteration listExtra = " + i);
         listExtra.add(extra);
     }
        LOG.debug(" -- listExtra loaded from arrayIndex with hole and index = " + listExtra);

 //2. complete list with complete extra
    for(int i=0; i<holes; i++){     
        listExtra.get(i).setExtra(complete);
    }
        LOG.debug(" -- liste extra completed with complete strokes = " + listExtra);
  
// 3. sort list on index
  List<ScoreStableford.ExtraClass> sortedlistExtra = listExtra.stream()
        .sorted(Comparator.comparingInt(ScoreStableford.ExtraClass::getIndex))  // sorted on StrokeIndex
        .collect(Collectors.toList());
  LOG.debug(" -- liste extra sorted on index= " + sortedlistExtra);

//4. complete list with uncomplete : on ne prend que les x premiers de la liste
    for(int i=0; i<uncomplete; i++){
        int e = sortedlistExtra.get(i).getExtra();
        sortedlistExtra.get(i).setExtra(e+1);
    }
  LOG.debug(" -- liste extra completed with strokes = " + sortedlistExtra);
 
// 5. sort list back on hole
   listExtra = listExtra.stream()  // normalement sortedListExtra mais les modifications on été apportées à listExtra
        .sorted(Comparator.comparingInt(ScoreStableford.ExtraClass::getHole))  // sorted on Hole
        .collect(Collectors.toList());
 LOG.debug(" -- liste extra sorted back on hole = " + listExtra);
 
 // 6. convert back list to extraArray
 
/* old    int[] extraArray = new int[listExtra.size()];
    for(int j=0; j<listExtra.size(); j++){
        extraArray[j] = listExtra.get(j).getExtra();
    }
*/
    int[] extraArray = listExtra.stream()  // stream() converts given ArrayList to stream
            .mapToInt(x -> x.getExtra())   // mapToInt() converts the obtained stream to IntStream
            .toArray();                    // toArray() is used to return an array

        LOG.debug(" -- array extraArray = " + Arrays.toString(extraArray));
    //LOG.debug(" -- total strokes = " + Arrays.stream(extraArray).sum());
    LOG.debug(" -- total strokes = " + IntStream.of(extraArray).sum());
    
 //7. special handling 9 holes, pour avoir une array longueur 18
   if(holes == 9){
        int[] zero = new int[9]; // int[] always has initial value of 0.
        if(start == 10){ 
            extraArray = IntStream
                  .concat(IntStream.of(zero), IntStream.of(extraArray))
                  .toArray();
        }
        if(start == 1){ 
            extraArray = IntStream
                  .concat(IntStream.of(extraArray),IntStream.of(zero))
                  .toArray();
        }
          LOG.debug(" -- extraArray concatenated to 18 holes = " + Arrays.toString(extraArray));
    } // end if 9
 
    
    
// LOG.debug("after add extra score = " + score);
   return extraArray;
}catch (Exception e){
      String msg = " -- Exception Error in " + methodName + " / " + e;
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
}finally{
//    LOG.debug(NEW_LINE + Arrays.deepToString(points) );
 }
} // end method setExtra Strokes
 
  
   /*/ remplacée le 21-08-2023
public static int[] completeWithExtraStrokes (ScoreStableford score, Round round){
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
     LOG.debug(" ... entering " + methodName  + " for score = " + score);
//     LOG.debug(" with round = " + round);
     LoggingUserController.write(CLASSNAME + "." + methodName,"i");
     LoggingUserController.write("extra strokes", "t");
try{
    int holes = round.getRoundHoles();
       LOG.debug("holes via round = " + holes);
    int start = round.getRoundStart();
       LOG.debug("start via round = " + start);
      
    int complete = score.getPlayingHandicap() / holes; // mod 04/07/2022
       LOG.debug(" -- ArrayExtraStrokes - # of loop Complete = " + complete);
       LoggingUserController.write("ArrayExtraStrokes - loop Complete = " + complete);
    int uncomplete = score.getPlayingHandicap() % holes;
       LOG.debug("ArrayExtraStrokes - # of loop Uncomplete = " + uncomplete);
        LoggingUserController.write("ArrayExtraStrokes - loop Uncomplete = " + uncomplete);
    int[] extra = score.getExtraArray().clone();
// fonctionne pour 18 trous !!
if(holes == 18){
    if(uncomplete !=0) {
        for (int i=0; i<extra.length; i++){
           extra[i] = extra[i] + complete; 
           if(score.getIndexArray()[i] < uncomplete + 1){   // uncomplete
             extra[i]++;
           }
        } //end for
    }
    score.setExtraArray(extra);
    LoggingUserController.write("Extra strokes = " + Arrays.toString(score.getExtraArray()));
 //   LOG.debug("after add extra score = " + score);
    return extra;
}
// holes 9 trous 
   start = start - 1;
       if(uncomplete !=0) {
           LOG.debug("j + holes = " + (start + holes));
     LOG.debug("start = " + start);
     LOG.debug("start + holes = " + start + holes);
     int[] arr = score.getIndexArray().clone();  // vraiment deux différents YES YES ??
        // only sort subarray {7, 6, 45, 21} and
        // keep other elements as it is.
        //https://www.geeksforgeeks.org/arrays-sort-in-java-with-examples/
        Arrays.sort(arr, start, start + holes);
           LOG.debug("Sorted arr[] :" + Arrays.toString(arr));
       int k = 0;
        for (int i = start; i < start + holes; ++i){
   //        if (Arrays.binarySearch(arr,9,18, arr[i]) >-1){
   
 //  LOG.debug("arr[i] = " + arr[i]);
//   LOG.debug("result of binary search = " + Arrays.binarySearch(arr,start,start + holes, arr[i]));
   
   //https://www.geeksforgeeks.org/arrays-binarysearch-in-java-with-examples-set-2-search-in-subarray/?ref=rp
           if (Arrays.binarySearch(arr,start,start + holes, arr[i]) >-1){  // -1 = not found
   //          LOG.debug("element ok = " + arr[i] + " i = " + i);
              extra[i] = extra[i] + complete;
   //               LOG.debug("extra complete = " + Arrays.toString(extra));
              k++;
               if(k < uncomplete + 1){
   //                 LOG.debug("element trouvé = " + arr[i]);
                  int x = ArrayUtils.indexOf(score.getIndexArray(), arr[i]); // ex strokeindex = 2 /Xe élément de array
   //                 LOG.debug("index x  = " + x);
                  extra[x] = extra[x] + 1;
   //                 LOG.debug("extra both uncomplete= " + Arrays.toString(extra));
               }  //end if 2
           } // end if 1
        }
 //       LOG.debug("after add arr[] :" + Arrays.toString(arr));
 //       LOG.debug("after add index :" + Arrays.toString(score.getIndexArray()));
 //       LOG.debug("after add extra :" + Arrays.toString(extra));
         score.setExtraArray(extra);
       }
         LOG.debug("after add extra score = " + score);
   return extra;
}catch (Exception e){
      String msg = " -- Exception Error in " + methodName + " / " + e;
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
}finally{
//    LOG.debug(NEW_LINE + Arrays.deepToString(points) );
 }
} // end method setExtra Strokes
*/
public ScoreStableford completeWithStatistics(ScoreStableford scoreStableford) throws SQLException, Exception{  // executed before include_statistics.xhtml
    LOG.debug("entering completeWithStatistics !");
try{
       int start = scoreStableford.getStart();
  //        LOG.debug("start = " + start);
       int holes = scoreStableford.getHoles();
  //        LOG.debug("holes = " + holes);    
//    List<Integer> holesList = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18));
 //       LOG.debug("parArray = "    + Arrays.toString(scoreStableford.getParArray()));
 //       LOG.debug("strokeArray = "    + Arrays.toString(scoreStableford.getStrokeArray()));
    for(int i=start-1; i<start+holes-1 ; i++){
          ScoreStableford.Statistics sta = scoreStableford.new Statistics();
          sta.setHole(i+1);
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
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method

public int[] completeWithDistances(ScoreStableford scoreStableford, Tee tee, Connection conn){  // executed before include_statistics.xhtml
    LOG.debug("entering completeWithDistances !");
    LOG.debug("with tee = " + tee);
   // du tee il faut retrouver le tee dont les distances sont complétées, càd toutes les couleurs pour Men en 18 trous
   
try{
 //   int arr [] = {257,425,105,272,226,383,252,135,310,295,285,155,337,308,396,105,283,332};
    Distance distance = new Distance();
  //  distance.setIdTee(tee.getIdtee());
       LOG.debug("entering completeWithDistances with tee = " + tee);
    Distance d = new FindDistances().find(tee, conn);
    LOG.debug("array distances = " + Arrays.toString(d.getDistanceArray()));
    return d.getDistanceArray();

}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method


// transformation arrays en list
public ArrayList<ScoreStableford.Score> completeScoreList(ScoreStableford scoreStableford){  // executed ?before ou pour utilisation dans score_stableford.xhtml
   LOG.debug("entering completeScoreList with scoreStableford = ! " + scoreStableford);
try{
 //      List<Integer> holesList = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18));
       ArrayList<ScoreStableford.Score> scoreList = new ArrayList<>();
       int start = scoreStableford.getStart()-1;
          LOG.debug("start = " + start);
       int holes = scoreStableford.getHoles();
          LOG.debug("holes = " + holes);
       int stop = start+holes;
          LOG.debug("stop = " + stop);
 LOG.debug("before loading scoreList, distance array = " + Arrays.toString(scoreStableford.getDistanceArray()));
       for(int i=start;i<stop;i++){
          ScoreStableford.Score score = scoreStableford.new Score();
          score.setHole(i+1);
          score.setPar(scoreStableford.getParArray()[i]);
          score.setIndex(scoreStableford.getIndexArray()[i]);
     // ici si 10-18 l'array est de longueur 9 !! 22-08-2023 si ok améliorer ailleurs !!
       //   if(scoreStableford.getStart() == 10){
       //       score.setExtra(scoreStableford.getExtraArray()[i+9]);
       //   }else{
              score.setExtra(scoreStableford.getExtraArray()[i]);
       //   }
          score.setPoints(scoreStableford.getPointsArray()[i]);
          score.setStrokes(scoreStableford.getStrokeArray()[i]);
          score.setDistances(scoreStableford.getDistanceArray()[i]);
          scoreList.add(score);
        }  // end for
      LOG.debug("completed scoreList = " + scoreList.toString());
    if(scoreList.isEmpty()){
        String msg = "generated scoreList is empty!";
        LOG.debug(msg);
        showMessageFatal(msg);
    }
   return scoreList;
}catch(Exception ex){
    String msg = "Exception in completeScoreList! " + ex;
     LOG.error(msg);
       //     showMessageFatal(msg);
            return null;
}
}
public ArrayList<ScoreStableford.Score> completeScoreListWithStrokes(ScoreStableford score){  // executed before score_stableford.xhtml
  //  LOG.debug("entering completeScoreListWithStrokes");
  //  LOG.debug("with scoreStableford = " + score);
   var scoreList = score.getScoreList();
    //  LOG.debug("entering completeScoreList with scoreList = ! " + scoreList);
   var strokesArray = score.getStrokeArray();
    //  LOG.debug("entering completeScoreList with strokesArray = ! " + Arrays.toString(strokesArray));
try{
    if(score.getStart() == 1){
      for(int i=0;i<scoreList.size();i++){
 //        LOG.debug(" i iteration 1 = "+ i);
       scoreList.get(i).setStrokes(strokesArray[i]);
      }
    }
    if(score.getStart() == 10){
        for(int i=9;i<18;i++){
 //         LOG.debug(" i iteration 2 = "+ i);
       scoreList.get(i-9).setStrokes(strokesArray[i]); // 09-0-7-2025 cherché longtemps was setPoints !!
      }
    }
   LOG.debug("scoreList with strokes = " + scoreList.toString());
    return scoreList;
}catch(Exception ex){
    String msg = "Exception in completeScoreListWithStrokes ! " + ex;
     LOG.error(msg);
     showMessageFatal(msg);
     return null;
}
} // end method

public ArrayList<ScoreStableford.Score> completeScoreListWithPoints(ScoreStableford score){  // executed before score_stableford.xhtml
   // LOG.debug("entering completeScoreListWithPoints");
   // LOG.debug("with scoreStableford = " + score);
    
   var scoreList = score.getScoreList();
     // LOG.debug("entering completeScoreList with scoreList = ! " + scoreList);
   var pointsArray = score.getPointsArray();
    //  LOG.debug("entering completeScoreList with pointsArray = ! " + Arrays.toString(pointsArray));
 //  LOG.debug("with score.getStart = " + score.getStart());
 //  LOG.debug("with score.getHoles = " + score.getHoles());
try{
    if (score.getStart() == 1){
      for(int i=0;i<scoreList.size();i++){
      //   LOG.debug(" i iteration 1 = "+ i);
       scoreList.get(i).setPoints(pointsArray[i]);
      }
     }
    if(score.getStart() == 10){
       //  LOG.debug("score.getStart = 10");
        for(int i=9;i<18;i++){
       //   LOG.debug(" i iteration 2 = "+ i);
          scoreList.get(i-9).setPoints(pointsArray[i]);
        }
    } // end if
   LOG.debug("scoreList points modified  = " + scoreList.toString());
    return scoreList;
}catch(Exception ex){
    String msg = "Exception in completScoreListPoints! " + ex;
     LOG.error(msg);
     showMessageFatal(msg);
     return null;
}
} // end method

// simplified version
/*public ArrayList<ScoreStableford.Score> completeScoreListWithPointsAndStrokes(ScoreStableford score){  // executed before score_stableford.xhtml
    LOG.debug("entering completeScoreListWithPointsAndStrokes");
    LOG.debug("with scoreStableford = " + score);
    
   var scoreList = score.getScoreList();
      LOG.debug("entering completeScoreList with scoreList size = ! " + scoreList.size());
   var pointsArray = score.getPointsArray();
   var strokesArray = score.getPointsArray();
    //  LOG.debug("entering completeScoreList with pointsArray = ! " + Arrays.toString(pointsArray));
 //  LOG.debug("with score.getStart = " + score.getStart());
 //  LOG.debug("with score.getHoles = " + score.getHoles());
try{
      for(int i=0;i<scoreList.size();i++){
         LOG.debug(" i iteration 1 = "+ i);
       scoreList.get(i).setPoints(pointsArray[i]);
       scoreList.get(i).setStrokes(strokesArray[i]);
      }
   LOG.debug("scoreList NEW points and strokes modified  = " + scoreList.toString());
    return scoreList;
}catch(Exception ex){
    String msg = "Exception in completScoreListPoints! " + ex;
     LOG.error(msg);
     showMessageFatal(msg);
     return null;
}
} // end method

*/



public ArrayList<ScoreStableford.Statistics> completeStatisticsListWithStrokes(ArrayList<ScoreStableford.Statistics> statisticsList, int[] strokeArray){ 
    // executed before score_statistics.xhtml pour les points qui viennent d'être modifiés ??ou dans calc ??
   LOG.debug("entering completeStatisticsListWithStrokes with statisticsList = ! " + statisticsList);
   LOG.debug("with strokeArray = ! " + Arrays.toString(strokeArray));
 //  int[] arr = score.getScoreList().stream().mapToInt(i -> i.getStrokes()).toArray();
try{
    for(int i=0;i<statisticsList.size();i++){ // index de la liste, idiot !
         LOG.debug(" i iteration 1 = "+ i);
       statisticsList.get(i).setStroke(strokeArray[i]);
     }
    LOG.debug("scoreList points added  = " + statisticsList.toString());
    return statisticsList;
}catch(Exception ex){
    String msg = "Exception in completeStatisticsListWithStrokes! " + ex;
     LOG.error(msg);
     showMessageFatal(msg);
     return null;
}
} // end method

 void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
  try{
      Club club = new Club();
      club.setIdclub(1075); // la cala
      Round round = new Round();
      round.setIdround(676);  // 19/05/2022 16:01
      round = new read.ReadRound().read(round, conn);
   // changing data for testing purpose
      round.setRoundDate(LocalDateTime.parse("2022-06-29T17:11:30"));  // limite 17:10
      round.setCourseIdcourse(95);  // other course was 101
  //    Structure str = new UnavailableController().isRoundUnavailable(club, round, conn);
  //      LOG.debug("from main, after lp = " + str);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main     
} //end Class