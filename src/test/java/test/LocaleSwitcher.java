
package test;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Named
@SessionScoped
public class LocaleSwitcher implements Serializable{
    public String getCountryEmojiFor( String countryCode ){
        // "DE", "US", "GB", "FR", "JP", ... ^^
        IntStream intStream = countryCode.toUpperCase().codePoints();
        String countryEmoji = "";
        for ( Integer codePoint : intStream.boxed().collect( Collectors.toList() ) ) {
            countryEmoji += Character.toString(codePoint + 127397);
        }
        return countryEmoji;
    }
    
    void main()  {// testing purposes
        
   //    String z = new LocaleSwitcher().getCountryEmojiFor("FR");
       // new java 22 https://www.happycoders.eu/java/java-22-features/
       LOG.debug("entering main");
  //  List<String> list = List.of("Earth", "Wind", "Fire");
 //   ListFormat formatter = ListFormat.getInstance(Locale.US, Type.STANDARD, Style.FULL);
 //   LOG.debug("US" + formatter.format(list));
 //   formatter = ListFormat.getInstance(Locale.FRANCE, Type.STANDARD, Style.FULL);
 //   LOG.debug("FRANCE" + formatter.format(list));   
 //   int a = 123;
 //   int b = 123;
  //  String interpolated = STR."\{a} times \{b} = \{Math.multiplyExact(a, b)}";
  //  LOG.debug("interpolated = " + interpolated);
    
       
  //     String s = lsw.getCountryEmojiFor("FR");
        LOG.debug("after call = " );
    
}// end main
    
}