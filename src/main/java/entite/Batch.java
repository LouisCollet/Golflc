package entite;

import java.io.Serializable;
import javax.inject.Named;

@Named
public class Batch implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;

    private long execID;
    private String StringID;

 
    public Batch()
    {
       
    }



 

     @Override
public String toString()
{ return 
        ("from entite : " + this.getClass().getSimpleName()
               + " = execID : "   + this.getExecID()
               + " ,StringID : " + this.getStringID()
    //           + " ,course_idcourse : " + this.getCourse_idcourse()
      //         + " ,period : " + this.getFlightPeriod()
        );
}


    public long getExecID() {
        return execID;
    }

    public void setExecID(long execID) {
        this.execID = execID;
    }

    public String getStringID() {
        return  Long.toString(execID);
      //  return StringID;
    }

    public void setStringID(String StringID) {
        this.StringID = StringID;
    }

    

    

    

} // end class
