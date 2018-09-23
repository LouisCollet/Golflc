package batch;

import java.io.Serializable;
import java.util.Properties;
import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named("SimpleItemReader")
public class SimpleItemReader extends AbstractItemReader {

////    @EJB // enlevé le 15-06-2018 donnait une erreur dans wildfly 13 !!

    @Inject
    private JobContext jobContext;

 //   Iterator<PayrollInputRecord> payrollInputRecords;

    public void open(Serializable e) throws Exception {
        Properties jobParameters = BatchRuntime.getJobOperator().getParameters(jobContext.getExecutionId());
 //       Set<PayrollInputRecord> records = dataBean.getPayrollInputRecords(
  //              (String) jobParameters.get("Month-Year"));
  //      payrollInputRecords = records.iterator();
    }

    public Object readItem() throws Exception {
        return null; // payrollInputRecords.hasNext() ? payrollInputRecords.next() : null;
    }
    
}