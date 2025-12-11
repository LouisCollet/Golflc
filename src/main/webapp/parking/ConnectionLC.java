
package utils;

class ConnectionLC { // defaualt 
    private static final ConnectionLC instance = new ConnectionLC();
    private ConnectionLC(){}
    public static ConnectionLC getInstance(){
       return instance;
    }
public class Main{
    void main(){
        System.out.println(ConnectionLC.getInstance().equals(ConnectionLC.getInstance()));
    }
    }
}
