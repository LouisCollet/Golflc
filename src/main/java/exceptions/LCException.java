package exceptions;
import utils.LCUtil;

public class LCException extends Exception implements interfaces.Log{
   // private ErrorCode code = null;

  // Constructor that accepts a message
    public LCException(String msg){
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
   } 

    public LCException(String msg, Throwable err){
        super(msg, err);
    LOG.error(msg + " / error " + err);
    LCUtil.showMessageFatal(msg + " / error " + err);
   } 
  }
