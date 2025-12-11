
package calc;
import static interfaces.Log.LOG;
//import org.testng.annotations.Test;
// https://www.geeksforgeeks.org/how-to-run-specific-testng-test-group-via-maven/
// ne fonctionne pas !
public class GroupedTest {

  //  @Test(groups = {"sanity"})
    public void testMethodOne() {
        LOG.debug("This is a sanity test");
    }

 //   @Test(groups = {"regression"})
    public void testMethodTwo() {
        System.out.println("This is a regression test");
    }

  //  @Test(groups = {"sanity", "regression"})
    public void testMethodThree() {
        System.out.println("This is both a sanity and regression test");
    }
}