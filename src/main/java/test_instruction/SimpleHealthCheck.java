/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test_instruction;

/**
 *
 * @author Collet
 */

import static interfaces.Log.LOG;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class SimpleHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("Simple health check");
    }


    public static void main(String[] args) throws Exception {
 //  Connection conn = new DBConnection().getConnection();
   try{
 //           Player player = new Player();
 //           player.setIdplayer(324713);
         HealthCheckResponse hcr = new test_instruction.SimpleHealthCheck().call();
           LOG.info("from main, CreateBlocking = " + hcr.toString());
    }catch (Exception e){
            String msg = "££ Exception in main ServiceHealthCheck = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
        //    DBConnection.closeQuietly(conn, null, null, null);
    }
    }
    } // end main//
 