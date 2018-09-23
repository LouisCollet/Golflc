/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batch;

import javax.batch.runtime.context.JobContext;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class SimpleItemProcessor implements javax.batch.api.chunk.ItemProcessor, interfaces.Log
{

    @Inject
    private JobContext jobContext;

    @Override
    public Object processItem(Object obj) throws Exception
    {
        return null;
  /*      PayrollInputRecord inputRecord =  (PayrollInputRecord) obj;
       PayrollRecord payrollRecord =   new PayrollRecord();

        int base = inputRecord.getBaseSalary();
        float tax = base * 27 / 100.0f;
        float bonus = base * 15 / 100.0f;

        payrollRecord.setEmpID(inputRecord.getId());
        payrollRecord.setBase(base);
        payrollRecord.setTax(tax);
        payrollRecord.setBonus(bonus);
        payrollRecord.setNet(base + bonus - tax);   
        return payrollRecord;
          */
    } //end method 
} //end class
