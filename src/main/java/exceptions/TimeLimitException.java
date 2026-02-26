package exceptions;

import static interfaces.Log.LOG;

public class TimeLimitException extends Exception {
    
    public TimeLimitException(String message, Throwable cause)
   {
       super(message,cause);
       LOG.debug("message = " + message);
       LOG.debug("cause = " + cause);
   }
   public TimeLimitException(String message)
   {
       super(message);
       LOG.debug("message = " + message);
   }
 //   LOG.error(msg);
 //   LCUtil.showMessageFatal(msg);
  //  LCUtil.showDialogInfo(msg);
} // end class