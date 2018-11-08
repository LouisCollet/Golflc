/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test_instruction;

import static interfaces.Log.LOG;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author Collet
 */
public class Stream {
   public static void main(String args[]) throws IOException  {
try{
    /*
       System.out.println("Using Java 7: ");
		
      // Count empty strings
      List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl");
      System.out.println("List: " +strings);
      long count = getCountEmptyStringUsingJava7(strings);
		
      System.out.println("Empty Strings: " + count);
      count = getCountLength3UsingJava7(strings);
		
      System.out.println("Strings of length 3: " + count);
		
      //Eliminate empty string
      List<String> filtered = deleteEmptyStringsUsingJava7(strings);
      System.out.println("Filtered List: " + filtered);
		
      //Eliminate empty string and join using comma.
      String mergedString = getMergedStringUsingJava7(strings,", ");
      System.out.println("Merged String: " + mergedString);
      List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);
		
      //get list of square of distinct numbers
      List<Integer> squaresList = getSquares(numbers);
      System.out.println("Squares List: " + squaresList);
      List<Integer> integers = Arrays.asList(1,2,13,4,15,6,17,8,19);
		
      System.out.println("List: " +integers);
      System.out.println("Highest number in List : " + getMax(integers));
      System.out.println("Lowest number in List : " + getMin(integers));
      System.out.println("Sum of all numbers : " + getSum(integers));
      System.out.println("Average of all numbers : " + getAverage(integers));
      System.out.println("Random Numbers: ");
		
      //print ten random numbers
      Random random = new Random();
		
      for(int i = 0; i < 10; i++) {
         System.out.println(random.nextInt());
      }
// -------------------------------------------------------------------		
  */    System.out.println("Using Java 8: ");
      
  int[] intArray = {1, 2, 3};
long[] longArray = Arrays.stream(intArray).asLongStream().toArray();
  LOG.info("longArray = " + Arrays.toString(longArray));
  
  long[] longArray2 = {1, 2, 3};
int[] intArray2 = Arrays.
        stream(longArray2)
        .mapToInt((i)-> (int) i) 
        .toArray();
  LOG.info("intArray2 = " + Arrays.toString(intArray2));
  
//  long[] longArray = Arrays.stream(intArray).mapToLong(i -> i).toArray();
      Map<Integer, String> map = new HashMap<>();
    map.put(1, "linode.com");
    map.put(2, "heroku.com");
    map.put(3, "louis");
    map.put(4, "collet");
    map.put(5, "amazone");
    
	
	//Map -> Stream -> Filter -> String
	String result = map.entrySet().stream()
		.filter(x -> "louis".equals(x.getValue()))
		.map(x->x.getValue())
		.collect(Collectors.joining());
System.out.println("result: " + result);

	//Map -> Stream -> Filter -> MAP
	Map<Integer, String> collect = map.entrySet().stream()
		.filter(x -> x.getKey() == 2)
		.collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
System.out.println("collect 1: " + collect);
	// or like this
	collect = map.entrySet().stream()
		.filter(x -> x.getKey() <= 3)
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
System.out.println("collect 2: " + collect);

      
      
      
       List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl");
      System.out.println("List: " +strings);
		
      long count = strings.stream().filter(string->string.isEmpty()).count();
      System.out.println("Empty Strings: " + count);
		
      count = strings.stream().filter(string -> string.length() == 3).count();
      System.out.println("Strings of length 3: " + count);
	List<String> filtered;
      filtered = strings.stream().filter(string ->!string.isEmpty()).collect(Collectors.toList());
      System.out.println("Filtered List: " + filtered);
		
     String mergedString = strings.stream().filter(string ->!string.isEmpty()).collect(Collectors.joining(", "));
      System.out.println("Merged String: " + mergedString);
	
      List<Integer> squaresList;
      List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);
      squaresList = numbers.stream().map( i ->i*i).distinct().collect(Collectors.toList());
      System.out.println("Squares List: " + squaresList);
      List<Integer> integers = Arrays.asList(1,2,13,4,15,6,17,8,19);
      System.out.println("List: " +integers);
		
      IntSummaryStatistics stats = integers.stream().mapToInt((x) ->x).summaryStatistics();
		
      System.out.println("Highest number in List : " + stats.getMax());
      System.out.println("Lowest number in List : " + stats.getMin());
      System.out.println("Sum of all numbers : " + stats.getSum());
      System.out.println("Average of all numbers : " + stats.getAverage());
      System.out.println("Random Numbers: ");
	
      Random random = new Random();
      random.ints().limit(10).sorted().forEach(System.out::println);
		
      //parallel processing
      count = strings.parallelStream().filter(string -> string.isEmpty()).count();
      System.out.println("Empty Strings: " + count);
      
      
   }catch (Exception ex){
            String msg = "Exception !  = "// + e.getFile().getFileName()
                    + " is NOT  = " + ex.getMessage();
           LOG.info(msg);
   }
   } // end main
	
   private static int getCountEmptyStringUsingJava7(List<String> strings) {
      int count = 0;

      count = strings.stream().filter((string) -> (string.isEmpty())).map((_item) -> 1).reduce(count, Integer::sum);
      return count;
   }
	
   private static int getCountLength3UsingJava7(List<String> strings) {
      int count = 0;
		
      count = strings.stream().filter((string) -> (string.length() == 3)).map((_item) -> 1).reduce(count, Integer::sum);
      return count;
   }
	
   private static List<String> deleteEmptyStringsUsingJava7(List<String> strings) {
      List<String> filteredList = new ArrayList<>();
		
      strings.stream().filter((string) -> (!string.isEmpty())).forEachOrdered((string) -> {
          filteredList.add(string);
       });
      return filteredList;
   }
	
   private static String getMergedStringUsingJava7(List<String> strings, String separator) {
      StringBuilder stringBuilder = new StringBuilder();
		
      for(String string: strings) {
		
         if(!string.isEmpty()) {
            stringBuilder.append(string);
            stringBuilder.append(separator);
         }
      }
      String mergedString = stringBuilder.toString();
      return mergedString.substring(0, mergedString.length()-2);
   }
	
   private static List<Integer> getSquares(List<Integer> numbers) {
      List<Integer> squaresList = new ArrayList<>();
		
      numbers.stream().map((number) -> number.intValue() * number.intValue()).filter((square) -> (!squaresList.contains(square))).forEachOrdered((square) -> {
          squaresList.add(square);
       });
      return squaresList;
   }
	
   private static int getMax(List<Integer> numbers) {
      int max = numbers.get(0);
		
      for(int i = 1;i < numbers.size();i++) {
		
         Integer number = numbers.get(i);
			
         if(number > max) {
            max = number;
         }
      }
      return max;
   }
	
   private static int getMin(List<Integer> numbers) {
      int min = numbers.get(0);
		
      for(int i= 1;i < numbers.size();i++) {
         Integer number = numbers.get(i);
		
         if(number < min) {
            min = number;
         }
      }
      return min;
   }
/*	
   private static int getSum(List<?> numbers) {
      int sum = (int)(numbers.get(0));
		
      for(int i = 1;i < numbers.size();i++) {
         sum += (int)numbers.get(i);
      }
      return sum;
   }
	
   private static int getAverage(List<Integer> numbers) {
      return getSum(numbers) / numbers.size();
   }*/
   
} // end class