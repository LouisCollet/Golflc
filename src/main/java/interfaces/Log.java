
package interfaces;

import org.apache.logging.log4j.LogManager; // log4j2
import org.apache.logging.log4j.Logger; //log4j2
//import static java.lang.System.out; // permet out.println("print") au lieu de System.out.println("print")

public abstract interface Log  //public and abstract are default
{
// public static final Logger LOG = Logger.getLogger("golflc"); // log4j1    
// static final Logger LOG = LoggerFactory.getLogger("golflc"); // slf4j
public static final Logger LOG = LogManager.getLogger("golflc"); // log4j2, en production le 28/8/2014

static final String NEW_LINE = "\n";
static final String TAB = "\t";

} // end interface
