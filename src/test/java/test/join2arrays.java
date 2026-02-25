
package test;

//import entite.ScoreStableford;


import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class join2arrays {
//public join2arrays(){}
public static String[][] concat(String[][] a, String[][] b) {
                String[][] result = new String[a.length + b.length][];
                System.arraycopy(a, 0, result, 0, a.length);
                System.arraycopy(b, 0, result, a.length, b.length);
                return result;
}
static class User {

        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
}
static class ExtraClass {
//public class ExtraClass {
        private int hole;
        private int index;
        private int extra;

        public ExtraClass(int hole, int index) {
            this.hole = hole;
            this.index = index;
        }

        public int getHole() {
            return hole;
        }

        public void setHole(int hole) {
            this.hole = hole;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getExtra() {
            return extra;
        }

        public void setExtra(int extra) {
            this.extra = extra;
        }

        @Override
        public String toString() {
            return  NEW_LINE + "hole = " + hole +
                    TAB + "index = " + index +
                    TAB + "extra = " + extra;

        }
    }

static List<User> users = Arrays.asList(
            new User("C", 30),
            new User("D", 40),
            new User("A", 10),
            new User("B", 20),
            new User("E", 50));
static int[] acquireRow(int[][] orig, int row) {
    int[] ret = new int[orig.length];
    for(int i = 0; i < ret.length; i++) {
        ret[i] = orig[i][row];
    }
    return ret;
}


 public static <T> List<T> convertArrayToList(T array[]){
        // Create the List by passing the Array
        // as parameter in the constructor
        List<T> list = new ArrayList<>();
        // Add the array to list
        Collections.addAll(list, array);
        // Return the converted List
        return list;
    }
public static String[][] concatenateTwoStringArrays(String[][]a, String[][] b) { 
  // Function to merge two arrays of same type 
  //https://www.geeksforgeeks.org/merge-arrays-into-a-new-object-array-in-java/amp/
        return Stream.concat(Arrays.stream(a), Arrays.stream(b))
                     .toArray(String[][]::new); 
    } 

public static void calcExtra(){

 int[] index0118 = new int[] {11, 1, 17, 3, 9, 5, 13, 15, 7, 6, 16, 8, 2, 4, 10, 12, 14, 18};
  LOG.debug(" -- ArrayIndex input = " + Arrays.toString(index0118) + " sum = " + IntStream.of(index0118).sum());
 Integer[] index1018 = new Integer[] {6, 16, 8, 2, 4, 10, 12, 14, 18};
 int[] input = new int[] {11, 1, 17, 3, 9, 5, 13, 15, 7}; //01-09
  int holes = 9;
    LOG.debug(" -- holes = " + holes);
  int start = 10; //10
    LOG.debug(" -- start = " + start);
    int[] v1 = null;
   if(holes == 9 && start == 1){
     v1 = utils.LCUtil.findSlice(index0118, start-1, holes);
     //arrayIndex = v1;
     LOG.debug(" -- sliced v1 1,9  = " + Arrays.toString(v1));
   }  
 //   start = 10;
    if(holes == 9 && start == 10){
     v1 = utils.LCUtil.findSlice(index0118, start-1, start-1+holes);
     LOG.debug(" -- sliced v1 for 9,10 = " + Arrays.toString(v1));
    }

  //Integer[] arrayIndex = utils.LCUtil.intToInteger(v1); // convert int[] to Integer []
 
  Integer[] arrayIndex = Arrays.stream(v1).boxed().toArray(Integer[]::new); // boxed = primitve to wrapper class stream
  
  
 //Array arrayIndex = Arrays.stream(v1)
  //      .mapToObj(Integer::valueOf)
  //      .collect(Collectors.toCollection(array());
 //List<Integer> arrayIndex = Arrays.stream(v1).boxed().toList();
 
 //ArrayList<Integer> arrayIndex = Arrays.stream(v1)
 //                               .boxed()
 //                               .collect(Collectors.toCollection(ArrayList::new));
  
 LOG.debug(" -- ArrayIndex input = " + Arrays.toString(arrayIndex));
 Arrays.stream(arrayIndex).forEach(item -> LOG.debug("arrayIndex = " + item));  // item -> LOG.debug("before add ,list of dropped Players = " + item.getIdplayer()))
 
 Stream<String> stringStream = Stream.of("a", "b", "c");
//Integer[] stringArray = v1.toArray(size -> new String[size]);
//Arrays.stream(stringArray).forEach(System.out::println);
 Stream<Integer> stream = Stream.of(1,2,3,4,5,6,7,8,9,10);
  
 //IntStream.of(v1).boxed();
  
 int playingHandicap = 40; //16
  LOG.debug(" -- playingHandicap course = " + playingHandicap);
  
 int complete = playingHandicap / holes;
 LOG.debug(" -- ArrayExtraStrokes - loop Complete = " + complete);
 
 int uncomplete = playingHandicap % holes;
 

 
  LOG.debug(" -- ArrayExtraStrokes - loop unComplete = " + uncomplete);
 //List<String> list = Arrays.asList("9", "A", "Z", "1", "B", "Y", "4", "a", "c");
 // // create a list from the Array
 // créer liste extra
 List<ExtraClass> listExtra = new ArrayList<>();
  
 // 1. load list from array
 for(int i=0; i<arrayIndex.length; i++){ 
         ExtraClass extra = new ExtraClass(i+start, arrayIndex[i]); // see constructor !!
      //   LOG.debug(" i iteration listExtra = " + i);
         listExtra.add(extra);
     }
 LOG.debug(" -- liste extra loaded from array = " + listExtra + " size = " + listExtra.size());
// essai pour 1018
 /*
 for(int i=0; i<arrayIndex.length; i++){ 
         ExtraClass extra = new ExtraClass(i+start, arrayIndex[i]); // see constructor !!
      //   LOG.debug(" i iteration listExtra = " + i);
         listExtra.add(extra);
     }
 LOG.debug(" -- liste extra loaded from array = " + listExtra);
 */
 
 //2. complete list with complete extra
   for(int i=0;i<holes;i++){
     listExtra.get(i).setExtra(complete);
 }
 LOG.debug(" -- liste extra completed with complete strokes = " + listExtra);
 
 // azvec des int[] et non des Integer[]çà essayer pour rester 


 //private static void sort4() {
  //  System.out.println("Reverse sort using reverseOrder() method and streams");
    int[] sortedArr = Arrays.stream(index0118)
        .boxed()
        .sorted(Comparator.reverseOrder())
        .mapToInt(Integer::intValue)
        .toArray();
    System.out.println(Arrays.toString(sortedArr));
  
// 3. sort list on index
  List<ExtraClass> sortedlistExtra = listExtra.stream()
			.sorted(Comparator.comparingInt(ExtraClass::getIndex))  // sorted on StrokeIndex
                      //  .limit(uncomplete-1)  // new 22-08-2023
			.collect(Collectors.toList());
  LOG.debug(" -- liste extra sorted on index, limited to uncomplete-1 = " + sortedlistExtra);

//4. complete list with uncomplete
  for(int i=0;i<uncomplete;i++){
 LOG.debug("listestra limited = " + sortedlistExtra.size());
  //  for(int i=0;i<sortedlistExtra.size();i++){  // new 22-08-2023 va avec limit plus haut
     int e = sortedlistExtra.get(i).getExtra();
     sortedlistExtra.get(i).setExtra(e+1);
 }
  LOG.debug(" -- listExtra completed with extrastrokes uncomplete = " + sortedlistExtra);

 // 5. sort list back on hole
   listExtra = listExtra.stream()
			.sorted(Comparator.comparingInt(ExtraClass::getHole))  // sorted on Hole
			.collect(Collectors.toList());
 LOG.debug(" -- liste extra sorted back on hole = " + listExtra);
 
 // back to array ??
 ///int[] extraArray = new int[listExtra.size()];
//  int[] extraArray = int arr[] = new int[listExtra.size()];
// ici jouer avec 
//LOG.debug(" -- liste extraArray = " + Arrays.toString(extraArray) + " length = " + extraArray.length);
LOG.debug(" -- start = " + start);
 LOG.debug(" -- holes = " + holes);
 
 ///for(int j=0; j<listExtra.size(); j++){
 ///      extraArray[j] = listExtra.get(j).getExtra();
 ///}
 
 // alternative
     int[] extraArray = listExtra.stream()
            .mapToInt(x -> x.getExtra())
            .toArray();
     
  //    extraArray..forEach(item -> LOG.debug("before add ,list of dropped Players = " + item.getIdplayer())); 
     
 LOG.debug(" -- special LC arr1 = " + Arrays.toString(extraArray));

 
 LOG.debug(" -- array extraArray = " + Arrays.toString(extraArray));
 LOG.debug(" -- total strokes = " + Arrays.stream(extraArray).sum());
 
//  for(int j=start; j<start+holes; j++){
//       extraArray[j] = listExtra.get(j).getExtra();
// }
//   for(int j=0; j<listExtra.size(); j++){
//       extraArray[j] = listExtra.get(j).getExtra();
// }
 // LOG.debug(" -- array extraArray for 18 trous = " + Arrays.toString(extraArray));
  
  
  
  // sidepart = 10 concat avec 
  LOG.debug(" before concat");
      
      if(start == 10){ // pour avoir une array longueur 18
          int[] a = new int[9]; // int always has initial value of 0.
          extraArray = IntStream.concat(IntStream.of(a), IntStream.of(extraArray)).toArray();
      }
 LOG.debug(" -- array extraArray = " + Arrays.toString(extraArray));
 LOG.debug(" -- total strokes = " + Arrays.stream(extraArray).sum());
 //List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
//Integer sum0 = integers.stream() 
 // .mapToInt(Integer::intValue) // unboxing
 // .sum();
 

} // end method


 void main() throws Exception {
  try{
  //    LCUtil lcu  = ; //;
    //    String[] array1 = {"1", "2", "3"};
    //    String[] array2 = {"4", "5", "6"};
 //   ArrayList<ScoreStableford.Statistics> statisticsList = new ArrayList<>();
 //List<String> b = new FindTeeStart().find(course, player,round, conn);
 //new join2arrays().calcExtra();
 
    BigDecimal par = BigDecimal.valueOf(36);
    BigDecimal slopeRating = BigDecimal.valueOf(131);
    BigDecimal courseRating = BigDecimal.valueOf(35.7);
    BigDecimal courseHcp18holes = BigDecimal.valueOf(-4);
    BigDecimal adjustedScore = BigDecimal.valueOf(40);
    BigDecimal strokesNotReceived = BigDecimal.ZERO;
  //  BigDecimal d2 = adjustedScore.add(strokesNotReceived).subtract(BigDecimal.valueOf(36));
 //LOG.debug("d2 = " + d2);
            BigDecimal SD = (BigDecimal.valueOf(113.0).divide(slopeRating, MathContext.DECIMAL64)
                    .multiply((par.multiply(BigDecimal.valueOf(2),MathContext.DECIMAL64)
                            .add((courseHcp18holes)
                            .subtract(adjustedScore.add(strokesNotReceived).subtract(BigDecimal.valueOf(36)))
                            .subtract(courseRating)))));

            String s = "Score Differential for 9 holes = " + SD;
         LOG.debug("SD = " + s);
         
      double handicapIndex = 27.8;
    double Dpar = 36;
   double DslopeRating = 131;
    double DcourseRating = 35.7;
   // double courseHcp18holes = -4;
   // double adjustedScore = 40;
   // double strokesNotReceived = 0;
  //  double A_113 = 113.0;
         double CH18holes = (handicapIndex * (DslopeRating / 113)) + DcourseRating - (Dpar * 2);
//done 24.4 au lie de 24.7
            String s1 = "Course Handicap 18 holes = " + CH18holes;
    System.out.println("SD = " + s1);
    
 //        System.out.println("SD rounded = " + myDoubleRound(SD,1));
     //    System.out.println("SD2 = " + (113 / slopeRating) * 28.3);
        //int result = Integer.valueOf(String.format("%.1f", SD));
 //System.out.println("result = " + result);
 /*
 Tee tee = new Tee();
 tee.setTeeGender("M");
 tee.setTeeHolesPlayed("01-18");
 tee.setTeeStart("RED");
 if(tee.getTeeStart().equals("YELLOW") && tee.getTeeGender().equals("M") && tee.getTeeHolesPlayed().equals("01-18")){
                LOG.debug("this tee is his own teeMaster !!");
           //     tee.setTeeMasterTee(tee.getIdtee());
              //  return tee;
            //  LOG.debug("this tee is his own teeMaster !!");
 }
 if(!tee.getTeeStart().equals("YELLOW") && tee.getTeeGender().equals("M") && tee.getTeeHolesPlayed().equals("01-18")){
                LOG.debug("this tee is his a color tee !!");
           //     tee.setTeeMasterTee(tee.getIdtee());
              //  return tee;
            //  LOG.debug("this tee is his own teeMaster !!");
 }else{
 
 
 }
 */

 //List<String> firstNElementsList = sortedlistExtra.stream()
 //        .limit(5)
 //        .collect(Collectors.toList());
  Integer[] index1018 = new Integer[] {6, 16, 8, 2, 4, 10, 12, 14, 18};
 List<Integer> list2 = Arrays.asList(index1018);
list2.forEach(item -> LOG.debug("starting list " + item)); 
 // dpuble brace
 List<Integer> sortedList = list2.stream()
         .sorted()
         .collect(Collectors.toList());
 //sortedList.forEach(System.out::print);
  sortedList.forEach(item -> LOG.debug("sorted list " + item)); 
sortedList = list2.stream()
			.sorted(Comparator.reverseOrder())
			.collect(Collectors.toList());
//LOG.debug(" -- reverseOrder = ");
sortedList.forEach(item -> LOG.debug("reverse order " + item)); 
List<User> sortedListUser = users.stream()
			.sorted(Comparator.comparingInt(User::getAge))
			.collect(Collectors.toList());
sortedListUser.forEach(item -> LOG.debug("sorted list user " + item));
  
//    ArrayList<ScoreStableford.Score> scoreList = new ArrayList<>();
 
    int[] v = new int[] {453, 393, 169, 395, 393, 499, 336, 186, 364, 293, 165, 476, 379, 399, 343, 464, 168, 306};
 //   v = new int[] {1,2,3,4,5};
    LOG.debug("0,9 = " + Arrays.stream(v,0,9).sum());  // 2
    LOG.debug("9,18 = " + Arrays.stream(v,9,18).sum());  // 2
    LOG.debug("total = " + Arrays.stream(v).sum());  // 2
 //   LOG.debug("0,4 = " + Arrays.stream(v,0,4).sum());  // 2
 //   LOG.debug("3,5 = " + Arrays.stream(v,3,5).sum());  // 2
          
    int[][] matrix2d = new int [][] {
        {1, 5, 11, 413},{2, 4, 1, 355}, {3, 3, 17, 157},
        {4, 4, 3, 363},{5, 4, 9, 362}, {6, 5, 5, 475},
        {7, 4, 13, 325},{8, 3, 15, 159}, {9, 4, 7, 355},
        {10, 4, 6, 239}, {11, 3, 16, 152}, {12, 5, 8, 447},
        {13, 4, 2, 347}, {14, 4, 4, 367}, {15, 4, 10, 310},
        {16, 5, 12, 446},{17, 3, 14, 141}, {18, 4, 18, 296}};
    
    LOG.debug("matrix2d =  ");
    //utils.LCUtil.printArray2DInt(matrix2d);
    LOG.debug("first el = " + matrix2d[0][3]);
    
//    var v = acquireRow(matrix2d,3);
    LOG.debug("v = " + Arrays.toString(v));
    
    int[] oneDimArray = new ArrayList<int[]>(Arrays.asList(matrix2d)).get(3);
    LOG.debug("oneDimArray = " + Arrays.toString(v));
    
    LOG.debug(Arrays.deepToString(matrix2d));


  
  /*
  
  //  [[1, 5, 11, 413], [2, 4, 1, 355], [3, 3, 17, 157], [4, 4, 3, 363], [5, 4, 9, 362], [6, 5, 5, 475], [7, 4, 13, 325], [8, 3, 15, 159], [9, 4, 7, 355], [10, 4, 6, 239], [11, 3, 16, 152], [12, 5, 8, 447], [13, 4, 2, 347], [14, 4, 4, 367], [15, 4, 10, 310], [16, 5, 12, 446], [17, 3, 14, 141], [18, 4, 18, 296]] ,type (golbal or distance : distance 
    Integer[] arrayIndex = new Integer[] {1 ,2 , 3, 4, 5, 6,7,8,9,1 ,2 , 3, 4, 5, 6,7,8,9};
    int[] arrayExtra = new int[]{17 ,18 , 19, 20, 21 ,22,23,24,25,17 ,18 , 19, 20, 21 ,22,23,24,25};
    int[] arrayPar = new int[]{3 ,4 ,4, 3, 5, 5,4,4,3,3 ,4 ,4, 3, 5, 5,4,4,3};
    int[] arrayPoints = new int[]{7 ,8 ,9, 10, 11, 12,13,14,15,7 ,8 ,9, 10, 11, 12,13,14,15};
  
    // convert list to array
        List<Integer> holesList = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18));
    Integer [] numArray = holesList.stream().toArray( n -> new Integer[n]);
     LOG.debug(" numArray = "+ Arrays.toString(numArray));
    int[] arr = holesList.stream().mapToInt(i -> i).toArray();
    LOG.debug(" arr = "+ Arrays.toString(arr));
 //   ScoreStableford scoreStableford = new ScoreStableford();
 int start = 10;
 int holes = 9;
    //for(int i=0;i<arrayIndex.length;i++){
      for(int i=start-1;i<(start+holes)-1;i++){
          ScoreStableford stb = new ScoreStableford();
        ScoreStableford.Score score = stb.new Score();
        LOG.debug(" i= "+ i);
    //    ScoreStableford.Statistics sta = scoreStableford.new Statistics();
      //  score.setHole(holesList.get(i));
        score.setHole(i+1);
        score.setPar(arrayPar[i]);
        score.setIndex(arrayIndex[i]);
        score.setExtra(arrayExtra[i]);
        score.setPoints(0);
        // ajouter distances
        scoreList.add(score);
    }
    LOG.debug("result1 = " + scoreList.toString());
    
    scoreList.get(0).setHole(holes); //set(1, ScoreStableford.Score score); 
    
    
    
      int[] arr2 = scoreList.stream().mapToInt(i -> i.getIndex()).toArray();
      
    LOG.debug(" arr2 = "+ Arrays.toString(arr2));
    
     scoreList.get(0).setPoints(arrayPoints[1]);
 //    LOG.debug("index of 10 = " + scoreList.indexOf(10));
    LOG.debug("result1a = " + scoreList.toString());
    
      for(int i=0;i<scoreList.size();i++){ // index de la liste, idiot !
  //       Score score = new Score();
         LOG.debug(" i iteration 2 = "+ i);
         scoreList.get(i).setPoints(arrayPoints[i]);
        // score.setPoints();
     }
    LOG.debug("result2 = " + scoreList.toString());
    

String [][] array1 = 
{
{"18 holes High Season", "Twilight High Season(apd 16:30)", "H"},
{"70.0", "45.0"},
{"70.0", "45.0"," 0.0", "0.0", "0.0"},
{"70.0", "45.0", "0.0", "0.0", "0.0"},
{"80.0", "50.0", "0.0", "0.0", "0.0"},
{"80.0", "50.0", "0.0", "0.0", "0.0"},
};
String [][] array2 = 
{
{"18 holes Low Season", "Twilight Low Season(apd 14:00)", "L"},
{"50.0", "35.0"},
{"50.0", "35.0"," 0.0", "0.0", "0.0"},
{"50.0", "35.0", "0.0", "0.0", "0.0"},
{"55.0", "40.0", "0.0", "0.0", "0.0"},
{"55.0", "40.0", "0.0", "0.0", "0.0"},
};

String[][] str = concat(array1,array2);
LOG.debug("resultat1 = " + Arrays.deepToString(str));
str = concatenateTwoStringArrays(array1, array2);
LOG.debug("resultat2 = " + Arrays.deepToString(str));
*/
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         
   }
   } // end main//
} // end class