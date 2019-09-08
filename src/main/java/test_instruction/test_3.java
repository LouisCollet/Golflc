package test_instruction;
import static interfaces.Log.LOG;
import java.text.ParseException;

public class test_3{

  public static void main(String[] args) throws ParseException{
      
      String s = "YELLOW / M / 01-18 / 1040"; 
      LOG.info("length string s = " + s.length());
            String s3 = s.substring(s.length() -3);
        LOG.info("string s3 = " + s3);
     //       faut substring dernier / jusque fin de string
            // 3 dernières positions format : BLUE / L / 01-09 / 154
      //      inscription.setInscriptionIdTee(Integer.valueOf(s3));
        LOG.info("connection closed : " + s.substring(s.lastIndexOf("/")+2,s.length() ) );
      
      /*
      String str = "MON, Mon Dec 31  CET 2018, low=6, high=8, Cloudy, code=26 / Cloudy";
      LOG.info("str before = " + str);
      // delete 5 premiers charactères
      str = str.substring(5);
       LOG.info("str after 1 = " + str);
   //    str = str.substring(4, str.length());
  //     LOG.info("str after 2 = " + str);
       int end = str.indexOf(",");
        LOG.info("end = " + end);
         str = str.substring(4, str.indexOf(","));
           LOG.info("str after 1 = " + str);  
           
      Map<String, String> articles = ImmutableMap.of("Title", "My New Article", "Title2", "Second Article");
      for (Map.Entry<String, String> entry : articles.entrySet()) {
      System.out.println(entry.getKey() + ":" + entry.getValue().toString());
}
      
      
      
 //     private static Map<Long,Player> messages = new HashMap<Long,Player>();
      String sunris = "Time [hours=11, minutes=52, convention=AM]";
      String lc = utils.LCUtil.extractHHmm(sunris);
        LOG.info("after call function = " + lc);
      
      
      LOG.info("string = " + sunris);
      int egal1 = sunris.indexOf("=")+1; //, sunrise.indexOf(",") + 1);  // cherche 2e virgule
        LOG.info("egal 1 = " + egal1);
      int virgule1 = sunris.indexOf(",");
        LOG.info("virgule1 = " + virgule1);
      String hours = sunris.substring(egal1, virgule1);
      LOG.info("hours = " + hours);
      
      int egal2 = sunris.indexOf("=", sunris.indexOf("=") + 1);  // cherche 2e virgule
        LOG.info("egal 2 = " + egal2);
      int virgule2 = sunris.indexOf(",", sunris.indexOf(",") + 1);  // cherche 2e virgule     
        LOG.info("virgule2 = " + virgule2);
      String minutes = sunris.substring(egal2+1, virgule2);   
       LOG.info("minutes = " + minutes);
       
        int egal3 = sunris.lastIndexOf("="); //, sunris.indexOf("=") + 1);  // cherche 2e virgule
        LOG.info("egal 3 = " + egal3);
       
      String ampm = sunris.substring(egal3+1, egal3+2+1);
          LOG.info("AMPM = " + ampm);
      if(ampm.equals("PM")){
            LOG.info("hours = PM ");
          int h = Integer.valueOf(hours) + 12;
          hours = String.valueOf(h);
          LOG.info("hours corrected = " + hours);
      }else{
           LOG.info("hours = AM ");
      }
      LOG.info("");
      LOG.info("sunrise = " + hours +"." + minutes);
      
      
      String str = "day=THU, date=Thu Dec 27 00:00:00 CET 2018, low=0, high=3, text=Cloudy, code=26";
   int comma =  str.indexOf(",", str.indexOf(",") + 1);  // cherche 2e virgule
    LOG.info("comma = " + comma);  
          String Datestr = str.substring(14,comma);  // commence char 18
             LOG.info("Datestr = " + Datestr);
  //   ZonedDateTime zdt = ZonedDateTime.of(2018, 01, 01, 0, 0, 0, 0, ZoneId.of("UTC"));
  //  LOG.info(" ZonedDateTime zdt = " + zdt);  
  
  //  String ather = "Thu Dec 27 00:00:00 CET 2018";
    String pattern = "EEE MMM dd HH:mm:ss z uuuu"; // uuuu = yyyy i strict mode
     LOG.info("pattern = " + pattern);
    DateTimeFormatter DTF = DateTimeFormatter.ofPattern(pattern).withLocale(Locale.US).withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);;
    LOG.info("dateWeather = " + Datestr);
     LocalDateTime sunrise = LocalDateTime.parse(Datestr, DTF);
     LOG.info(" success !! sunrise = " + sunrise); 
     
     DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern(pattern).withLocale(Locale.US).withZone(ZoneId.of("Europe/Brussels"));
     ZonedDateTime zdt = ZonedDateTime.parse(Datestr,dtf3);
     LOG.info(" success !! zdt = " + zdt); 
    str = zdt + " " + str;
        LOG.info(" success !!str = " + str); 
  //  zdt =  ZonedDateTime.of(Datestr);
 
   //   String Datestr = str.substring(0, str.indexOf(",")); // isole date
   //        LOG.info("Datestr = " + Datestr);  
      
    //  String image = "Image [title=Yahoo! Weather, link=http://weather.yahoo.com, url=http://l.yimg.com/a/i/brand/purplelogo//uh/us/news-wea.gif, width=142, height=18] ";
      String image = "Item [title=Conditions for Brussels, Capital Region of Brussels, BE at 07:00 AM CET, link=http://us.rd.yahoo.com/dailynews/rss/weather/Country__Country/*https://weather.yahoo.com/country/state/city-12817380/, description=<![CDATA[<img src=\"http://l.yimg.com/a/i/us/we/52/26.gif\"/>\n" +
"<BR />\n" +
"<b>Current Conditions:</b>\n" +
"<BR />Cloudy\n" +
"<BR />\n" +
"<BR />\n" +
"<b>Forecast:</b>\n" +
"<BR /> Thu - Cloudy. High: 3Low: 0\n" +
"<BR /> Fri - Partly Cloudy. High: 4Low: -1\n" +
"<BR /> Sat - Cloudy. High: 7Low: 3\n" +
"<BR /> Sun - Cloudy. High: 9Low: 7\n" +
"<BR /> Mon - Cloudy. High: 8Low: 6\n" +
"<BR />\n" +
"<BR />\n" +
"<a href=\"http://us.rd.yahoo.com/dailynews/rss/weather/Country__Country/*https://weather.yahoo.com/country/state/city-12817380/\">Full Forecast at Yahoo! Weather</a>\n" +
"<BR />\n" +
"<BR />\n" +
"<BR />\n" +
"]]>, guid=, pubDate=Thu Dec 27 07:00:00 CET 2018, geoLat=50.83022, geoLong=4.34485, condition=Condition [text=Cloudy, code=26, temp=0, date=Thu Dec 27 07:00:00 CET 2018], forecasts=[Forecast [day=THU, date=Thu Dec 27 00:00:00 CET 2018, low=0, high=3, text=Cloudy, code=26], Forecast [day=FRI, date=Fri Dec 28 00:00:00 CET 2018, low=-1, high=4, text=Partly Cloudy, code=30], Forecast [day=SAT, date=Sat Dec 29 00:00:00 CET 2018, low=3, high=7, text=Cloudy, code=26], Forecast [day=SUN, date=Sun Dec 30 00:00:00 CET 2018, low=7, high=9, text=Cloudy, code=26], Forecast [day=MON, date=Mon Dec 31 00:00:00 CET 2018, low=6, high=8, text=Cloudy, code=26], Forecast [day=TUE, date=Tue Jan 01 00:00:00 CET 2019, low=4, high=7, text=Cloudy, code=26], Forecast [day=WED, date=Wed Jan 02 00:00:00 CET 2019, low=3, high=6, text=Mostly Cloudy, code=28], Forecast [day=THU, date=Thu Jan 03 00:00:00 CET 2019, low=2, high=5, text=Mostly Cloudy, code=28], Forecast [day=FRI, date=Fri Jan 04 00:00:00 CET 2019, low=3, high=6, text=Mostly Cloudy, code=28], Forecast [day=SAT, date=Sat Jan 05 00:00:00 CET 2019, low=3, high=6, text=Scattered Showers, code=39]]] ";
      LOG.info("string image = " + image);
  //    LOG.info("extracted 1 = " + image.substring(image.indexOf("<img src="), image.indexOf(".gif")+8) ); 
  //    LOG.info("extracted 2 =");
  //    LOG.info(image.substring(image.indexOf("Forecast"), image.indexOf("<a href")+8) ); 
  //         LOG.info("extracted 3 =");
  //    LOG.info(image.substring(image.indexOf("Forecast [day="), image.indexOf("], Forecast")) ); 
  //    LOG.info("extracted 4 =");
      int start = 0;
      List<String> myList = new ArrayList<>();
      
      start = image.indexOf("[day=",start);
      int end = image.indexOf("], Forecast");
      String result = image.substring(start+1, end);
      LOG.info("result 1 = " + result); 
      myList.add(result);
  //    LOG.info("myList = " + myList);
      LOG.info("end 1 = " + end);
      start = end + 12;
    //  start = image.indexOf("[day=",i);
    //  LOG.info("start 2 = " + start);
      end = image.indexOf("], Forecast",start);
      
      result = image.substring(start+1, end);
      LOG.info("result 2 = " + result); 
      myList.add(result);
  //    LOG.info("myList = " + myList);
      LOG.info("end 2 = " + start);
      
     start = end + 12;
    //  start = image.indexOf("[day=",i);
    //  LOG.info("start 2 = " + start);
      end = image.indexOf("], Forecast",start);
      
      result = image.substring(start+1, end);
      LOG.info("result 3 = " + result); 
      myList.add(result);
  //    LOG.info("myList = " + myList);
      LOG.info("end 3 = " + start);
      
         LOG.info("myList = " + myList);
      
      LOG.info("");
      LOG.info("starting loop ");
      int start = 0;
       ArrayList<String> myList = new ArrayList<>(9);
   //   List<String> myList = new ArrayList<>();
  while (true) {
    start = image.indexOf("[day=",start);
 //   LOG.info("start 2 = " + start);
    String str1 = "], Forecast";
    int end2 = image.indexOf(str1,start);
 //   LOG.info("end 2 = " + end2);
    if(end2 == -1) break;
    String result = image.substring(start+1, end2);
        LOG.info("result = " + result); 
    result = result.replaceAll("00.00.00", "").replaceAll("text=", "").replaceAll("day=", "").replaceAll("date=", "");
    myList.add(result);
    start = end2 + str1.length()+1;
 }
    LOG.info("ending with myList = " + myList.toString());
      myList.forEach(System.out::println);
      
        Map<String,String> condition = new HashMap<>();
        condition.put("0","Tornado");
        condition.put("1","Tropical storm");
        condition.put("2","Hurricane");
        condition.put("3","Severe thunderstorms");
        condition.put("12","Showers");
        condition.put("23","Blustery");
        condition.put("26","Cloudy");
        condition.put("28","Mostly cloudy (day)");
        condition.put("30","Partly cloudy(day)");
        condition.put("34","Fair(day)");
           
      
      for(int i = 0; i < myList.size(); i++) {
            String code = myList.get(i).substring(myList.get(i).length()-2);
            LOG.info("code = " + code);
            String value = condition.get(code);
            LOG.info("Map condition value retrieved = " + value);
            LOG.info("classic printing : <BR />" + myList.get(i) + " / " + value);

        }


   //     String value = condition.get("26");
  //      LOG.info("Map value retrieved = " + value);
 // input primitive integer +myarray
      
      
       input primitive integer array
		int[] intArray = { 1, 2, 3, 4 ,5 };
		String strArray[] = new String[intArray.length];
		for (int i = 0; i < intArray.length; i++)
			strArray[i] = String.valueOf(intArray[i]);
		System.out.println("array string =" + Arrays.toString(strArray));
      
      
             //   int[] intArray = {1, 2, 3, 4, 5};

        // 1. Arrays.stream -> IntStream 
    //    IntStream intStream1 = Arrays.stream(intArray);
    //    intStream1.forEach(x -> System.out.println(x));

        // 2. Stream.of -> Stream<int[]>
    //    Stream<int[]> temp = Stream.of(intArray);

        // Cant print Stream<int[]> directly, convert / flat it to IntStream 
    //    IntStream intStream2 = temp.flatMapToInt(x -> Arrays.stream(x));
    //    intStream2.forEach(x -> System.out.println(x));
                
                
      
//		int[] intArray2 = { 1, 2, 3, 4, 5 };

//		String strArray2[] = Arrays.stream(intArray2)
//			.mapToObj(String::valueOf)
//			.toArray(String[]::new);

//		System.out.println(Arrays.toString(strArray));};
      
      
      
      
      
      String text = "abcdefghijkl1234"; 
        LOG.info("text =  " + text); 
        System.out.println("The size of the String is " + text.length()); 
      
      String numbers = text.substring(0, text.length() - 4) + "****"; 

//.subtring("****");
        System.out.println("numbers =  " + numbers); 
        
    //      numbers = "************" + text.substring(text.length()-4, text.length()); 
    //      System.out.println("numbers =  " + numbers); 
          
          numbers = repeat("*", 12) + text.substring(text.length()-4); 
          System.out.println("numbers =  " + numbers); 
          
         
          
        int n = 8;
String s = "Hello, World!";
 System.out.println("s =  " + s); 
System.out.println("reducted = " + s.substring(0,n));
        
      Map<String, Integer> items = new HashMap<>();

        items.put("coins", 3);
        items.put("pens", 2);
        items.put("keys", 1);
        items.put("sheets", 12);

        items.forEach((k, v) -> {
            System.out.printf("%s : %d%n", k, v);
        });

      
      
   // from Double[] to double[]   
      Double[] boxed = new Double[] { 1.0, 2.0, 3.0 };
      double[] unboxed = Stream.of(boxed).mapToDouble(Double::doubleValue).toArray();
   //   LOG.info("itme double = " + Arrays.deepToString(unboxed));
      for(double speed : unboxed) {
            LOG.info("Print ad element double = " + speed);
        }
    // from double[] to Double[]
        double[] boxed1 = new double[] { 26.1, 2.0, 3.0 };
        
String numberAsString = "153.25";
double number = Double.parseDouble(numberAsString);
System.out.println("The number is: " + number);


   double[] ddouble = new double[] { 26.1, 2.0, 3.0, 29.4 };
//   System.out.println("double Array[ = " + Arrays.deepToString(ddouble));
   // Double[] dDouble = new Double[] { 0.0, 0.0, 0.0 };
   Double[] dDouble = utils.LCUtil.doubleArrayToDoubleArray(ddouble);
   
   LOG.info("Double Array[ = " + Arrays.deepToString(dDouble));
   
   System.out.println("Double Array = " + Arrays.deepToString(dDouble));
   for(int i=0; i < ddouble.length ; i++)
   {
       double d = ddouble[i];
    Double dd = new Double(d);
 //      System.out.println("dObj dd = " + dd);
   dDouble[i] = dd;    
 //   System.out.println("dDouble[i] = " + dDouble[i]);
   }
   
    
    Use Double constructor to convert double primitive type to a Double object.
    

 //   Double dObj = new Double(d);
 //   dDouble[1] = valueOf(ddouble[1]);
  //  dDouble[1] = ddouble[1]..doubleValue());
  //  System.out.println("dObj = " + dDouble[1]);


//String numberAsString2 = "153.25";




//We can shorten to:

//String numberAsString = "153.25";
//double number = new Double(numberAsString).doubleValue();


   //     double[] tempArray = new double[4];
   //     int i = 0;
   //     for(Double d : (Double) data[columnIndex]) {
   //         tempArray[i] = (double) d;
   //         i++;
   //     }
        
        
//        for(Double speed : unboxed) {
 //           LOG.info("Print ad element Double = " + speed);
 //       }

      List<String> myList = new ArrayList<>();
    myList.add("Black");
    myList.add("White");
    myList.add("Yellow");
    myList.add("Blue");
    myList.add("Red");
    System.out.println("longueur list = " + myList.size());

String[] myArray = new String[myList.size()];
myList.toArray(myArray);

for (String myString:myArray) {
   System.out.println("myString = " + myString);
}
      
      System.out.println("myArray = " + Arrays.deepToString(myArray));
   
      String s1 = "Corinne Henrotte <henrottecorinne@hotmail.com>"; 
      System.out.println(s1);
      System.out.println(s1.indexOf('<'));
      System.out.println(s1.indexOf('>'));
      System.out.println(s1.substring(s1.indexOf('<')+1, s1.indexOf('>')) ); 
      s1 = "brigitte.vanderschueren@gmail.com"; 
      System.out.println(s1);
 //     int found = s1.indexOf('<');
      if (s1.indexOf('<') != -1) {
        System.out.println(s1.substring(s1.indexOf('<')+1, s1.indexOf('>')) ); 
      }else{
        System.out.println(s1);
      }

      System.out.println(s1.indexOf('<'));
      
      
      
       s1 = s1.trim(); //.replace(" ","0");
      System.out.println( "s1 = /" + s1 + "/");
      String s2 = " 4";
 //     s2 = s2.replace(" ","0");
 //     System.out.println( "s2 = /" + "/");
      if(s2 == "  ")      {
          System.out.println( "s2 = blanco/" + "/");
          s2 = "0";
      }else{
       s2 = s2.trim();} //.replace(" ","0");
      System.out.println( "s2 = /" + s2 + "/");
      
     
  }    
*/
  }
}
