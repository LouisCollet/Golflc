
package numbertext;
/* See numbertext.org
 * 2009-2010 (c) László Németh
 * License: LGPL/BSD dual license */


import entite.Settings;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static numbertext.MenuState.LANGUAGE;
import static numbertext.MenuState.PARAM;
import static numbertext.MenuState.PREFIX;
import static java.lang.System.out;
public class Numbertext {

private static final Pattern LANG_PATTERN_NO = Pattern.compile("n[bn]([-_]NO)?");
// https://github.com/Numbertext/libnumbertext/blob/master/data/fr.sor
	private static Map<String, Soros> modules = new HashMap<>();

private static Soros load(String langfile, String langcode) {
            LOG.debug("entering load");
            LOG.debug("entering load with langfile = " + langfile);
            LOG.debug("entering load with langcode = " + langcode);
 //  final Soros s;

try{
   // String p = "C:/Users/Collet/Documents/NetBeansProjects/GolfWfly/src/main/webapp/resources/numbertext/be.sor";
   // Path path = Paths.get(p);
Path path = Paths.get(Settings.getProperty("RESOURCES") + "numbertext/"+ langfile + ".sor" );
            LOG.debug("path soros = " + path);
             String readString = Files.readString(path, StandardCharsets.UTF_8);
//             LOG.debug("readString = \n" + readString);
             Soros soros = new Soros(readString, langcode);
   //                     LOG.debug("just before s");
		if(modules != null && langfile != null) {
                      LOG.debug("modules and langfile OK");
		      modules.put(langcode, soros);
                      LOG.debug("modules put !");
		}else{
                      LOG.debug("modules and langfile NOT ok");
                }
      return soros;
} catch (IOException e) {
                     LOG.error("IOexception in load !!" + e);
		     return null;

} catch (Exception e) {
                     LOG.error("exception in load !!" + e);
		     return null;
}

} //end method

public static String numbertext(String input, String lang) {
    try{
             LOG.debug("entering numbertext with input =  " + input);
                LOG.debug("entering numbertext with lang =  " + lang);
             lang = lang.substring(0, 2);
                LOG.debug("entering first two with lang =  " + lang);
		Soros soros = modules.get(lang);
                 LOG.debug("length modules = " + modules.size());
        //           LOG.debug("Soros s = " + s.toString());
		if (soros == null) {
                    LOG.debug("numbertext - first null");
		    soros = load(lang.replace('-', '_'), lang);
		}
                   
		if (soros == null) {
                     LOG.debug("numbertext - second null");
		     soros = load(lang.replaceFirst("[-_].*", ""), lang);
                    LOG.debug("numbertext - third null");
		}
		if (soros == null) {
                    LOG.debug("numbertext - fourth null");  
			// some exceptional language codes
			// Norwegian....
			Matcher m = LANG_PATTERN_NO.matcher(lang);
			if (m.find()) {
				soros = load(m.replaceAll("no"), lang);
			}
		}
		if (soros == null) {
			LOG.debug("null 04 louis, Missing language module: " + lang);
			return null;
		}
		return soros.run(input);
//	}
} catch (Exception e) {
                     LOG.error("exception in load !!" + e);
		     return null;
		}
} //end method
public static String moneytext(String input, String money, String lang) throws IOException {
		return numbertext(money + " " + input, lang);
	}

private static void printHelp() {
		out.println("Usage: java soros [-l lang] [-p prefix_function] [par1 [par2...]]");
		out.println("Parameter: n: number; n-m: range; n-m~s: range with step");
		out.println("Example: java -jar numbertext.jar -l en_US 99 # spell out number 99 in English");
		out.println("         # spell out different ordinal numbers and number ranges");
		out.println("         java -jar numbertext.jar -l en_US -p ordinal 1-10 500 1000-10000~1000");
		out.println("         java -jar numbertext.jar -l en_US # print prefix functions of the language module");
		out.println("License: GNU LGPL/BSD dual-license");
	}

//	void main() {

void main(String [] args) throws IOException {
try{
        LOG.debug("entering main");
         args = new String[3];
         args[0] = "-l";
         args[1] = "fr-BE"; // en_US
         args[2] = "73,95";
         String result = kernel(args);
            LOG.debug("result in main = \n" + result);
    } catch (Exception e) {
         LOG.error("exception in main !!" + e);
}
} // end main

public static String kernel(String[] args) throws IOException {
  try{
      LOG.debug("entering kernel");
   //            String lang = "fr";
		if (args.length == 0) {
			printHelp();
			return "invalid args";
		}
            LOG.debug("Command-line arguments =");
            for (String arg : args) {
                  LOG.debug("argument = " + arg);

            }
        String lang = args[1];
        if(lang.equals("fr")){
            lang = "be";
        }
	MenuState state = PARAM;
	boolean missingNumbers = true;
	String prefix = "";
	for (int i = 0; i < args.length; i++) {
                OUTER:
                switch (state) {
                    case PARAM -> {
                        switch (args[i]) {
                            case "-l" -> {
                                state = LANGUAGE;
                                break OUTER;
                        }
                            case "-p" -> {
                                state = PREFIX;
                                break OUTER;
                        }
                            default -> {
                                    missingNumbers = false;
                                    int idx = args[i].indexOf('-', 1);
                                    if (idx > -1) {
                                            int b = Integer.parseInt(args[i].substring(0, idx));
                                            String e = args[i].substring(idx + 1);
                                            int step = e.indexOf('~', idx);
                                            int end;
                                            if (step > -1) {
                                                    end = Integer.parseInt(e.substring(0, step));
                                                    step = Integer.parseInt(e.substring(step + 1));
                                                    } else {
                                                            step = 1;
                                                            end = Integer.parseInt(e);
                                                    }
                                            for (int j = b; j <= end; j = j + step) {
                                                    LOG.debug("numbertext 1 - " + numbertext(prefix + j, lang));
                                                    return numbertext(prefix + j, lang);
                                                    }
                                            } else {
                                                    LOG.debug("numbertext 2 - " + numbertext(prefix + args[i], lang));
                                                    return numbertext(prefix + args[i], lang);
                                            }
                        }
                        }
                    }
                    case LANGUAGE -> {
                        lang = args[i];
                        if (numbertext("1", lang) == null) {
                            LOG.debug("system exit 1");
                            System.exit(1);
                        }
                        state= PARAM;
                    }
                    case PREFIX -> {
                        prefix = args[i] + " ";
                        state = PARAM;
                    }
                }
		}
		if (missingNumbers) {
			LOG.debug("numbertext 3 - " + numbertext("help", lang));
		}
 return "c'est bizarre";
} catch (Exception e) {
         LOG.error("exception in kernel " + e);
         return e.toString();
}
} //end main
} //end class