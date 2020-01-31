/*
https://developers.redhat.com/blog/2019/12/13/jakarta-ee-creating-an-enterprise-javabeans-timer/
 */
package test_instruction;

import static interfaces.Log.LOG;
import javax.ejb.Singleton;
@Singleton
public class MyTimerAutomatic {
// à remettre lige suivante 
//    @Schedule(hour = "*", minute = "*",second = "0,10,20,30,40,50",persistent = false)
    public void execute(){

        LOG.info("Automatic timer executing");

    }
}