/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package lc.golfnew;

import java.io.Serializable;
import javax.enterprise.context.ApplicationScoped;
//import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author collet
 */

@Named("dataC")
@ApplicationScoped
    public class DataController implements Serializable, interfaces.Log
    {

        public enum Status
{

    SUBMITTED("Submitted"),
    REJECTED("Rejected"),
    APPROVED("Approved");

    private final String label;

    private Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Status[] getStatuses()
    {
        return Status.values();
    }

 } //deleted

        
    public Status[] getStatuses()
    {
        return Status.values();
    }

    } //end class

