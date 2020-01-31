/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test_instruction;
import static interfaces.Log.LOG;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Stateless
public class MyTimer {
@Resource(name="sessionContext")
//  @Resource
  private SessionContext context;

    public void initTimer(String message) throws NamingException{
   //     InitialContext ic = new InitialContext();
   InitialContext ic = new InitialContext();
      SessionContext sctxLookup =
             (SessionContext) ic.lookup("java:comp/env/sessionContext");
       System.out.println("look up injected sctx: " + sctxLookup);
        LOG.info("entering initTimer");
  //     context.getBusinessObject();
        context.getTimerService().createTimer(10000, message);
    }

    @Timeout
    public void execute(){
        LOG.info("Starting");

        context.getTimerService()
                .getAllTimers()
                .stream()
                .forEach(timer -> LOG.info(String.valueOf(timer.getInfo())));
        

        LOG.info("Ending");
    }    
}
// end