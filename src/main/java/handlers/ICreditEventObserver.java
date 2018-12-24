/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handlers;
import events.PaymentEvent;
import javax.enterprise.event.Observes;
import qualifiers.Credit;


public interface ICreditEventObserver {    

/**

 */
    void onCreditPaymentEvent(@Observes @Credit PaymentEvent event);

}

