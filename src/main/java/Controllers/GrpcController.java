package Controllers;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import protobuf.order.OrderRequest;
import protobuf.order.OrderResponse;
import protobuf.order.OrderServiceGrpc;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

//https://code.google.com/archive/p/protobuf-netbeans-plugin/
// https://github.com/mwillema/protobuf-netbeans-plugin
//https://dzone.com/articles/interview-google-protocol-buff
@Named("grpcController") // this qualifier  makes a bean EL-injectable (Expression Language)
@SessionScoped
public class GrpcController implements Serializable{
   io.grpc.Server server  = null;
   int port = 50051;
   /*https://github.com/grpc/grpc-java/blob/master/SECURITY.md
   https://dzone.com/articles/secure-your-grpc-services-with-ssltls
   https://groups.google.com/g/grpc-io/c/kZ56GMj3TsM
   Enabling TLS on a server

To use TLS on the server, a certificate chain and private key need to be specified in PEM format. 
   The standard TLS port is 443, but we use 8443 below to avoid needing extra permissions from the OS.

Server server = ServerBuilder.forPort(8443)
    // Enable TLS
    .useTransportSecurity(certChainFile, privateKeyFile)
    .addService(serviceImplementation)
    .build();
server.start();
   */
private boolean isLocalPortInUse(int port) {
    try {
        // ServerSocket try to open a LOCAL port
        new ServerSocket(port).close();
        // local port can be opened, it's available
        return false;
    } catch(Exception e) {
        String msg = "Local port cannot be closed, it's in use = " + port;
        LOG.debug(msg);
        showMessageFatal(msg);
        return true;
    }
}
   public String startServer() {
   try{
       LOG.debug("entering Grpc startServer");
       if(isLocalPortInUse(port)){
           String msg = "This port is already in use = " + port;
           showMessageFatal(msg);
           return null;
       }
        server = ServerBuilder
                .forPort(port)
                .addService(new grpc.OrderServiceImpl())
            //    .intercept(new LogInterceptor())
                .intercept(new grpc.GlobalExceptionInterceptor()) // new 07-06-2024
                .build();
            LOG.debug("gRpcServer implemented ! "); // + server.getClass().toString());
        server.start();
           String msg = "gRpcServer started = " + server.toString() + " , listening op port : " + server.getPort() + server.getServices().toString();
           LOG.debug(msg);
           showMessageInfo(msg);
   //        String s = new grpc.ApplicationClient().sendMessage();   
   //        LOG.debug("retour send message = " + s);
        server.awaitTermination();
           LOG.debug("gRpcServer awaiting termination = ");
       return null;   // back to same screen
       
     }catch (Exception me) {
         String msg = "Unable to startServer due to error: " + me;
         LOG.error(msg);
         showMessageFatal(msg);
         return null;
     }

} // end method
/*
// https://medium.com/@mahdiyusefi72/first-step-to-tune-grpc-in-java-8b57f7f0f591
    public void start() throws IOException {
        NettyServerBuilder nettyServer = NettyServerBuilder.forPort(port)
                .bossEventLoopGroup(new NioEventLoopGroup(1))
                .workerEventLoopGroup(new NioEventLoopGroup(5))
                .channelType(NioServerSocketChannel.class)
                .executor(Executors.newFixedThreadPool(5,
                        new ThreadFactoryBuilder().setNameFormat("grpc-%d").build()))
        //        .addService(new GrpcApi())
                .addService(new grpc.OrderServiceImpl())
                .build();
                .start();
    }
*/
public String stopServer() {
      LOG.debug("entering stopServer with server = " + server);
   try{
               if(server != null){
                  server.shutdown();
                  server.awaitTermination(1, TimeUnit.MINUTES); // Whatever value you want
                  server.shutdownNow(); 
                  String msg = "server is now down !! " + server.getPort() + server.getServices().toString();
                  LOG.debug(msg);
                  showMessageInfo(msg);
               }else{
                  String msg = "server is null !! ";
                  LOG.debug(msg);
                  showMessageFatal(msg);
               }   
 //              server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
       return null; // d'ou il vient
       
     }catch (Exception me) {
         String msg = "Unable to stopServer due to an error: " + me;
         LOG.error(msg);
         showMessageFatal(msg);
         return null;
     }

} // end method

  public String sendMessage(){
  try{
      LOG.debug("entering sendMessage with server = " + server);
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", port)
                                      .usePlaintext()
                                 //     .enableRetry()
                                 //     .maxRetryAttempts(5)
                                      .build();
       LOG.debug("managedChannel = " + managedChannel);
        OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub = OrderServiceGrpc.newBlockingStub(managedChannel);
//LOG.debug("line 01" + orderServiceBlockingStub);
        OrderRequest orderRequest = OrderRequest.newBuilder()
                                             .setEmail("hello@word.com")
                                             .setProduct("Product : no-name")
                                             .setAmount(3)
                                             .build();
              LOG.debug("orderRequest = " + NEW_LINE +orderRequest);
        OrderResponse orderResponse = orderServiceBlockingStub.executeOrder(orderRequest);
        String msg = "Received response: "+ orderResponse.getInfo();
        LOG.debug(msg);
        showMessageInfo(msg);
        LOG.debug("state channel = " + managedChannel.getState(true));
         managedChannel.shutdown();
       
        return null;
        
       }catch (Exception me) {
         String msg = "Unable to sendMessage due to an error: " + me;
         LOG.error(msg);
         showMessageFatal(msg);
         return null;
     }
} // end method

  void main() {
       LOG.debug("starting main");
  }
} // end class