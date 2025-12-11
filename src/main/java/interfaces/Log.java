
package interfaces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// import org.apache.logging.log4j.core.LoggerContext;

public abstract interface Log{
//public static final Logger LOG = LogManager.getLogger(Log.class.getName()); // log4j2, was golflc
    //https://logging.apache.org/log4j/2.x/manual/api.html
public static final Logger LOG = LogManager.getLogger();
static final String NEW_LINE = System.getProperty("line.separator");
static final String TAB = "\t";
// final LoggerContext context = (LoggerContext) LogManager.getContext(false);

 // final org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();
} // end interface