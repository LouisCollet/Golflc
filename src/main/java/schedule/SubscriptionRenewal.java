/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule;

import java.util.TimerTask;

/**
 *
 * @author Collet
 */
public class SubscriptionRenewal extends TimerTask implements interfaces.Log
{
    @Override
    public void run(){
        // your custom code here
        LOG.info("subscription renewal query started here");
    }
    
} //end class
