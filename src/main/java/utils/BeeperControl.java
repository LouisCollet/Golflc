/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.*;
public class BeeperControl implements interfaces.Log
 {
   private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public static void beepForAnHour() {
     final Runnable beeper = () -> {
         LOG.info("beep");
     };
     final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);
     scheduler.schedule(() -> {
         beeperHandle.cancel(true);
     }, 60 * 60, SECONDS);
   }
 } // end class