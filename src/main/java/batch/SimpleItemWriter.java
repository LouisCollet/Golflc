/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batch;

//import javax.ejb.EJB;

import java.io.Serializable;
import java.util.List;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;

@Named("SimpleItemWriter")
public abstract class SimpleItemWriter extends AbstractItemWriter implements interfaces.Log
{

    @Override
    public void open(Serializable checkpoint) throws Exception {
        LOG.info("Open item writing stage");
    }
    public void writeItems(List list) throws Exception {
        for (Object obj : list) {
            System.out.println("PayrollRecord: " + obj);
       //     em.persist(obj);
        }
    }
    
}
