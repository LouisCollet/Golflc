
package interfaces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract interface Log{
public static final Logger LOG = LogManager.getLogger();
public static Logger getLogger(Class<?> clazz) { // new 03-04-2026
    return LogManager.getLogger(clazz);
}


static final String NEW_LINE = System.getProperty("line.separator");
static final String TAB = "\t";
} // end interface