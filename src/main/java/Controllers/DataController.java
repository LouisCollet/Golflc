/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controllers;

import java.io.Serializable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;


@Named("dataC")
@ApplicationScoped
    public class DataController implements Serializable, interfaces.Log{

        public enum Status{

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

 } //end enum

        
    public Status[] getStatuses()
    {
        return Status.values();
    }

    } //end class

