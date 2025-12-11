
package numbertext;

/* Soros interpreter (see numbertext.org)
 * 2009-2010 (c) László Németh
 * License: LGPL/BSD dual license */

import static interfaces.Log.LOG;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Soros {
	private List<Pattern> patterns = new ArrayList<>();
	private List<String> values = new ArrayList<>();
	private List<Boolean> begins = new ArrayList<>();
	private List<Boolean> ends = new ArrayList<>();

	private static final String M = "\\\";#";
	private static final String M2 = "$()|[]";
	private static final String C = "\uE000\uE001\uE002\uE003";
	private static final String C2 = "\uE004\uE005\uE006\uE007\uE008\uE009";
	private static final String SLASH = "\uE000";
	private static final String PIPE = "\uE003";

	// pattern to recognize function calls in the replacement string

	private static final Pattern FUNC = Pattern.compile(translate("(?:\\|?(?:\\$\\()+)?" + // optional nested calls
			"(\\|?\\$\\(([^\\(\\)]*)\\)\\|?)" + // inner call (2 subgroups)
			"(?:\\)+\\|?)?", // optional nested calls
			M2.substring(0, C.length()), C, "\\")); // \$, \(, \), \| -> \uE000..\uE003

public Soros(String source, String lang) {
    try{
		source = translate(source, M, C, "\\"); // \\, \", \;, \# -> \uE000..\uE003
		// switch off all country-dependent lines, and switch on the requested ones
		source = source.replaceAll("(^|[\n;])([^\n;#]*#[^\n]*\\[:[^\n:\\]]*:][^\n]*)", "$1#$2")
				.replaceAll("(^|[\n;])#([^\n;#]*#[^\n]*\\[:" + lang.replace('_', '-') + ":][^\n]*)", "$1$2")
				.replaceAll("(#[^\n]*)?(\n|$)", ";"); // remove comments
		if (!source.contains("__numbertext__")) {
			source = "__numbertext__;" + source;
		}

		String replacement = "\"([a-z][-a-z]* )?0+(0|[1-9]\\d*)\" $(\\1\\2);"; // default left zero deletion
		replacement += "\"\uE00A(.*)\uE00A(.+)\uE00A(.*)\" \\1\\2\\3;"; // separator function
		replacement += "\"\\\"\\uE00A.*\\uE00A\\uE00A.*\\\"\");"; // no separation, if subcall returns with empty string
		source = source.replace("__numbertext__", replacement);

		final Pattern p = Pattern.compile("^\\s*(\"[^\"]*\"|[^\\s]*)\\s*(.*[^\\s])?\\s*$");
		final Pattern macro = Pattern.compile("== *(.*[^ ]?) ==");
		String prefix = "";
		for (String s : source.split(";")) {
			Matcher matchmacro = macro.matcher(s);
			if (matchmacro.matches()) {
				prefix = matchmacro.group(1);
				continue;
			}
			Matcher sp = p.matcher(s);
			if (!prefix.isEmpty() && !s.isEmpty() && sp.matches()) {
				s = sp.group(1).replaceFirst("^\"", "").replaceFirst("\"$", "");
				s = "\"" + (s.startsWith("^") ? "^" : "") + prefix + (s.isEmpty() ? "" : " ")
						+ s.replaceFirst("^\\^", "") + "\" " + sp.group(2);
				sp = p.matcher(s);
			}
			if (!s.isEmpty() && sp.matches()) {
				s = translate(sp.group(1).replaceFirst("^\"", "").replaceFirst("\"$", ""), C.substring(1),
						M.substring(1), "");
				s = s.replace(SLASH, "\\\\"); // -> \\, ", ;, #
				String s2 = "";
				if (sp.group(2) != null) {
					s2 = sp.group(2).replaceFirst("^\"", "").replaceFirst("\"$", "");
				}
				s2 = translate(s2, M2, C2, "\\"); // \$, \(, \), \|, \[, \] -> \uE004..\uE009

				// call inner separator: [ ... $1 ... ] -> $(\uE00A ... \uE00A$1\uE00A ... )
				s2 = s2.replaceAll("^\\[[$](\\d\\d?|\\([^\\)]+\\))", "\\$(\uE00A\uE00A|\\$$1\uE00A"); // add "|"
				s2 = s2.replaceAll("\\[([^$\\[\\\\]*)[$](\\d\\d?|\\([^\\)]+\\))", "\\$(\uE00A$1\uE00A\\$$2\uE00A");
				s2 = s2.replaceAll("\uE00A\\]$", "|\uE00A)"); // add "|" in terminating position
				s2 = s2.replaceAll("\\]", ")");
				s2 = s2.replaceAll("(\\$\\d|\\))\\|\\$", "$1||\\$"); // $()|$() -> $()||$()
				s2 = translate(s2, C, M, ""); // \uE000..\uE003-> \, ", ;, #
				s2 = translate(s2, M2.substring(0, C.length()), C, ""); // $, (, ), | -> \uE000..\uE003
				s2 = translate(s2, C2, M2, ""); // \uE004..\uE009 -> $, (, ), |, [, ]
				s2 = s2.replaceAll("[$]", "\\$"); // $ -> \$
				s2 = s2.replaceAll("\uE000(\\d)", "\uE000\uE001\\$$1\uE002"); // $n -> $(\n)
				s2 = s2.replaceAll("\\\\(\\d)", "\\$$1"); // \[n] -> $[n]
				s2 = s2.replace("\\n", "\n"); // \n -> [new line]
				patterns.add(Pattern.compile("^" + s.replaceFirst("^\\^", "").replaceFirst("\\$$", "") + "$"));
				begins.add(s.startsWith("^"));
				ends.add(s.endsWith("$"));
				values.add(s2);
			}
		}
} catch (Exception e) {
         LOG.error("exception in Soros !!" + e);
}               
} // end method

public String run(String input) {
		return run(input, true, true);
	}

private String run(String input, boolean begin, boolean end) {
    try{
		for (int i = 0; i < patterns.size(); i++) {
			if ((!begin && begins.get(i)) || (!end && ends.get(i))) {
				continue;
			}
			Matcher m1 = patterns.get(i).matcher(input);
			if (!m1.matches()) {
				continue;
			}

			String s = m1.replaceAll(values.get(i));
			Matcher n = FUNC.matcher(s);
			while (n.find()) {
				boolean b = false;
				boolean e = false;
				if (n.group(1).startsWith(PIPE) || n.group().startsWith(PIPE)) {
					b = true;
				} else if (n.start() == 0) {
					b = begin;
				}

				if (n.group(1).endsWith(PIPE) || n.group().endsWith(PIPE)) {
					e = true;
				} else if (n.end() == s.length()) {
					e = end;
				}
				s = s.substring(0, n.start(1)) + run(n.group(2), b, e) + s.substring(n.end(1));
				n = FUNC.matcher(s);
			}
			return s;
		}
		return "";
    } catch (Exception e) {
         LOG.error("exception in main !!" + e);
         return null;
}
    } //end run

private static String translate(String s, String chars, String chars2, String delim) {
    try{
		for (int i = 0; i < chars.length(); i++) {
			s = s.replace(delim + chars.charAt(i), "" + chars2.charAt(i));
		}
		return s;
     } catch (Exception e) {
         LOG.error("exception in main !!" + e);
}         return null;      
} //end translate
    
} // end class