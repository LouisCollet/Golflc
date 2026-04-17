
package numbertext;
/* See numbertext.org
 * 2009-2010 (c) László Németh
 * License: LGPL/BSD dual license */

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static java.lang.System.out;
import static numbertext.MenuState.LANGUAGE;
import static numbertext.MenuState.PARAM;
import static numbertext.MenuState.PREFIX;

/**
 * Conversion de nombres en texte (numbertext.org)
 * ✅ @ApplicationScoped — singleton CDI, cache modules partagé
 * ✅ Settings injecté
 * ✅ modules — champ d'instance (plus static)
 * ✅ System.exit() supprimé — dangereux dans WildFly
 */
@Named
@ApplicationScoped
public class Numbertext implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ static final acceptable — Pattern immuable compilé une seule fois
    private static final Pattern LANG_PATTERN_NO = Pattern.compile("n[bn]([-_]NO)?");

    // ✅ Champ d'instance — @ApplicationScoped garantit le singleton
    private Map<String, Soros> modules = new HashMap<>();

    @Inject private entite.Settings settings;

    public Numbertext() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end method

    // ========================================
    // LOAD — chargement fichier .sor
    // ========================================

    private Soros load(String langfile, String langcode) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName
                + " - langfile = " + langfile
                + " , langcode = " + langcode);
        try {
            Path path = Paths.get(settings.getProperty("RESOURCES")
                    + "numbertext/" + langfile + ".sor");
            LOG.debug(methodName + " - path = " + path);

            String readString = Files.readString(path, StandardCharsets.UTF_8);
            Soros soros = new Soros(readString, langcode);

            if (langfile != null) {
                modules.put(langcode, soros);
                LOG.debug(methodName + " - module added for langcode = " + langcode);
            } else {
                LOG.warn(methodName + " - langfile is null — module not added");
            }
            return soros;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // NUMBERTEXT — conversion nombre → texte
    // ========================================

    public String numbertext(String input, String lang) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName
                + " - input = " + input
                + " , lang = " + lang);
        try {
            lang = lang.substring(0, 2);
            LOG.debug(methodName + " - lang (2 chars) = " + lang);

            Soros soros = modules.get(lang);
            LOG.debug(methodName + " - modules size = " + modules.size());

            if (soros == null) {
                LOG.debug(methodName + " - loading with replace '-' → '_'");
                soros = load(lang.replace('-', '_'), lang);
            }
            if (soros == null) {
                LOG.debug(methodName + " - loading with replaceFirst");
                soros = load(lang.replaceFirst("[-_].*", ""), lang);
            }
            if (soros == null) {
                // Cas exceptionnel — Norvégien
                Matcher m = LANG_PATTERN_NO.matcher(lang);
                if (m.find()) {
                    LOG.debug(methodName + " - loading Norwegian fallback");
                    soros = load(m.replaceAll("no"), lang);
                }
            }
            if (soros == null) {
                LOG.error(methodName + " - Missing language module for lang = " + lang);
                return null;
            }
            return soros.run(input);

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // MONEYTEXT — conversion montant → texte
    // ========================================

    public String moneytext(String input, String money, String lang) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return numbertext(money + " " + input, lang);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // KERNEL — point d'entrée principal
    // ========================================

    public String kernel(String[] args) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (args == null || args.length == 0) {
                printHelp();
                return "invalid args";
            }

            for (String arg : args) {
                LOG.debug(methodName + " - argument = " + arg);
            }

            String lang = args[1];
            // Mapping fr → be (fichier be.sor)
            if (lang.equals("fr")) {
                lang = "be";
            }

            MenuState state        = PARAM;
            boolean missingNumbers = true;
            String prefix          = "";

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
                                    int b    = Integer.parseInt(args[i].substring(0, idx));
                                    String e = args[i].substring(idx + 1);
                                    int step = e.indexOf('~', idx);
                                    int end;
                                    if (step > -1) {
                                        end  = Integer.parseInt(e.substring(0, step));
                                        step = Integer.parseInt(e.substring(step + 1));
                                    } else {
                                        step = 1;
                                        end  = Integer.parseInt(e);
                                    }
                                    for (int j = b; j <= end; j = j + step) {
                                        String result = numbertext(prefix + j, lang);
                                        LOG.debug(methodName + " - numbertext range = " + result);
                                        return result;
                                    }
                                } else {
                                    String result = numbertext(prefix + args[i], lang);
                                    LOG.debug(methodName + " - numbertext single = " + result);
                                    return result;
                                }
                            }
                        }
                    }
                    case LANGUAGE -> {
                        lang = args[i];
                        // ✅ System.exit() supprimé — dangereux dans WildFly
                        if (numbertext("1", lang) == null) {
                            LOG.error(methodName + " - language module not found for lang = " + lang);
                            return null;
                        }
                        state = PARAM;
                    }
                    case PREFIX -> {
                        prefix = args[i] + " ";
                        state  = PARAM;
                    }
                }
            }

            if (missingNumbers) {
                String result = numbertext("help", lang);
                LOG.debug(methodName + " - missingNumbers result = " + result);
            }

            return null;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // PRINT HELP
    // ========================================

    private void printHelp() {
        out.println("Usage: java soros [-l lang] [-p prefix_function] [par1 [par2...]]");
        out.println("Parameter: n: number; n-m: range; n-m~s: range with step");
        out.println("Example: java -jar numbertext.jar -l en_US 99");
        out.println("License: GNU LGPL/BSD dual-license");
    } // end method

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    void main(String[] args) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            args    = new String[3];
            args[0] = "-l";
            args[1] = "fr-BE";
            args[2] = "73,95";
            String result = kernel(args);
            LOG.debug(methodName + " - result = " + result);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end main
    */

} // end class

/*
/* See numbertext.org
 * 2009-2010 (c) László Németh
 * License: LGPL/BSD dual license 


import static interfaces.Log.LOG;
import jakarta.inject.Inject;
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
    
@Inject private entite.Settings settings;        // ✅ injection CDI

private static final Pattern LANG_PATTERN_NO = Pattern.compile("n[bn]([-_]NO)?");
// https://github.com/Numbertext/libnumbertext/blob/master/data/fr.sor
private static Map<String, Soros> modules = new HashMap<>();

//private static Soros load(String langfile, String langcode) {
 private Soros load(String langfile, String langcode) {   
            LOG.debug("entering load");
            LOG.debug("entering load with langfile = " + langfile);
            LOG.debug("entering load with langcode = " + langcode);
 //  final Soros s;

try{
   // String p = "C:/Users/Collet/Documents/NetBeansProjects/GolfWfly/src/main/webapp/resources/numbertext/be.sor";
   // Path path = Paths.get(p);
    Path path = Paths.get(settings.getProperty("RESOURCES") + "numbertext/"+ langfile + ".sor" );
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

//public static String numbertext(String input, String lang) {
public String numbertext(String input, String lang) {    
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
public String moneytext(String input, String money, String lang) throws IOException {
		return numbertext(money + " " + input, lang);
	}

private void printHelp() {
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

public String kernel(String[] args) throws IOException {
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
*/