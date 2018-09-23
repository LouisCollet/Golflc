package exceptions;
import utils.LCUtil;

public class LCCustomException extends Exception implements interfaces.Log{
 //   public LCCustomException() {super();}
 //   public LCCustomException(String message){super(message);}
    
    public LCCustomException(String msg)
   {
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
  //  LCUtil.showDialogInfo(msg);
   } 

}
