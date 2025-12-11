
package utils;

// https://www.softwaretestinghelp.com/array-of-objects-in-java/

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class TestArrayOfObjects {
//    private static Object[][]  datesSeason; // 
    private static ArrayList<DatesSeasons> datesSeasonsList = new ArrayList<>(); // pas de dimension de départ
    
   public static void main(String args[]){
     
     DatesSeasons ds = new DatesSeasons(LocalDateTime.parse("2022-01-01T12:45:30"),LocalDateTime.parse("2022-03-31T12:45:30"), "H");
     // ou alors utiliser la solution classique ??
     DatesSeasons ds1 = new DatesSeasons();
     ds1.setStartDate(LocalDateTime.parse("2022-01-04T12:45:30"));
     ds1.setEndDate(LocalDateTime.parse("2022-09-30T12:45:30"));
     ds1.setSeason("M");
 //   DatesSeasons[] datesSeason = new DatesSeasons[10] ;
  //     datesSeason[0] = new DatesSeasons(LocalDateTime.parse("2022-01-01T12:45:30"),LocalDateTime.parse("2022-03-31T12:45:30"), "H");
       datesSeasonsList.add(ds);
       datesSeasonsList.add(ds1);
  //     datesSeason[1] = new DatesSeasons(LocalDateTime.parse("2022-01-04T12:45:30"),LocalDateTime.parse("2022-09-30T12:45:30"), "M");
  //     datesSeason[2] = new DatesSeasons(LocalDateTime.parse("2022-01-10T12:45:30"),LocalDateTime.parse("2022-12-31T12:45:30"), "H");
      //print original array
        System.out.println("Original Array of datesSeason objects:");
        System.out.println(datesSeasonsList);
 }  // end main
} // end class testarray

class DatesSeasons{
  LocalDateTime startDate;
  LocalDateTime endDate;
  String season;  

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }
  
  
  //Employee class constructor
  DatesSeasons(LocalDateTime st, LocalDateTime en, String n){
     startDate =st;
     endDate =en;
     season = n;
  }
    DatesSeasons(){
  //   startDate =st;
  //   endDate =en;
  //   season = n;
  }
  
  
  
//overridden functions since we are working with array of objects
    @Override
    public String toString() {
        return "{" + "start='" + startDate
                + '\'' + ", end=" + endDate
                + '\'' + ", season=" + season
                + '}';
    }
}