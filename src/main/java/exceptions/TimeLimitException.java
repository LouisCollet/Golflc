package exceptions;

public class TimeLimitException extends Exception implements interfaces.Log{
    
    public TimeLimitException(String message, Throwable cause)
   {
       super(message,cause);
       LOG.info("message = " + message);
       LOG.info("cause = " + cause);
   }
   public TimeLimitException(String message)
   {
       super(message);
       LOG.info("message = " + message);
   }
 //   LOG.error(msg);
 //   LCUtil.showMessageFatal(msg);
  //  LCUtil.showDialogInfo(msg);
} // end class