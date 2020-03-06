
package test_instruction;
import static interfaces.Log.LOG;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

@Health
@ApplicationScoped
public abstract class ServiceHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call1() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named(ServiceHealthCheck.class.getSimpleName());
        
        responseBuilder.withData("memory", Runtime.getRuntime().freeMemory());
        responseBuilder.withData("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        return responseBuilder.state(true).build();

    }
    
    @Override
    public HealthCheckResponse call2() {
        return HealthCheckResponse.up("Simple health check");
    }

    
    
    public static void main(String[] args) throws Exception {
 //  Connection conn = new DBConnection().getConnection();
   try{
 //           Player player = new Player();
 //           player.setIdplayer(324713);
      //   HealthCheckResponse hcr = new test_instruction.ServiceHealthCheck().call1();
  //                 HealthCheckResponse hcr = new test_instruction.ServiceHealthCheck().call1();
           HealthCheckResponse hcr2 = ServiceHealthCheck().call2();
           LOG.info("from main, CreateBlocking = " + hcr2.toString());
    }catch (Exception e){
            String msg = "££ Exception in main ServiceHealthCheck = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
        //    DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//// end main//
    
} //end class

