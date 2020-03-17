
package test_instruction;
import static interfaces.Log.LOG;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

//@Health
@ApplicationScoped
public class ServiceHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named(ServiceHealthCheck.class.getSimpleName());
        
        responseBuilder.withData("memory", Runtime.getRuntime().freeMemory());
        responseBuilder.withData("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        return responseBuilder.state(true).build();

    }
    public static void main(String[] args) throws Exception {
 //  Connection conn = new DBConnection().getConnection();
   try{
 //           Player player = new Player();
 //           player.setIdplayer(324713);
         HealthCheckResponse hcr = new test_instruction.ServiceHealthCheck().call();
           LOG.info("from main, CreateBlocking = " + hcr.toString());
    }catch (Exception e){
            String msg = "££ Exception in main ServiceHealthCheck = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
        //    DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//
 
} //end class

