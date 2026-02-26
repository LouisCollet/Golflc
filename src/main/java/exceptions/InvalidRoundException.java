
package exceptions;

public class InvalidRoundException extends Exception {
    private final int actualHoles;
    
    public InvalidRoundException(String message) {
        super(message);
        this.actualHoles = 0;
    }
    
    public InvalidRoundException(String message, int actualHoles) {
        super(message);
        this.actualHoles = actualHoles;
    }
    
    public int getActualHoles() {
        return actualHoles;
    }
}