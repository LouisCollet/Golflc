
package exceptions;

public class ExceptionGolfLC extends Exception
{
  public ExceptionGolfLC()
    { super(); }
  public ExceptionGolfLC(String message)
    { super(message); }
  public ExceptionGolfLC(String message, Throwable cause)
    { super(message, cause); }
  public ExceptionGolfLC(Throwable cause)
    { super(cause); }
}    
