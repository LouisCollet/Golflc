
package showcase;

import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.FilterMeta;

@Named("dtFilterView")
@ViewScoped
public class FilterView implements Serializable {

    private List<Customer> customers1;
    private List<Customer> filteredCustomers1;

    @Inject
    private CustomerService service;

    private List<FilterMeta> filterBy;

    @PostConstruct
    public void init() {
        customers1 = service.getCustomers(10);
    }

    public List<Representative> getRepresentatives() {
        return service.getRepresentatives();
    }

    public CustomerStatus[] getCustomerStatus() {
        return service.getCustomerStatus();
    }

    public List<Customer> getCustomers1() {
        return customers1;
    }

    public List<Customer> getFilteredCustomers1() {
        return filteredCustomers1;
    }

    public void setFilteredCustomers1(List<Customer> filteredCustomers1) {
        this.filteredCustomers1 = filteredCustomers1;
    }

    public void setService(CustomerService service) {
        this.service = service;
    }

    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }
    
    public void selectCustomer(Customer customer) {
        LOG.info("customer found = " + customer.getName());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("customer found = " + customer.getName()));
    }
}
