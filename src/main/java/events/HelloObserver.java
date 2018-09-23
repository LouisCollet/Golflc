/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package events;

//mport events.HelloEvent;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

/**
 *
 * @author Collet
 */
//@Stateless
//@LocalBean 
public class HelloObserver implements interfaces.Log
{
    //The default is @Observer(during = TransactionPhase.IN_PROGRESS)
    //which causes the method to run immediately after the event has fired during the transaction.
    
    public void listenToHello(@Observes(during = TransactionPhase.IN_PROGRESS) HelloEvent helloEvent)
    {
        LOG.info("from HelloObserver - listenTo Hello - HelloEvent: " + helloEvent);
    }

} //end class