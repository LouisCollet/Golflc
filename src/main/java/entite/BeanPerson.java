
package entite;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

//https://balusc.omnifaces.org/2020/11/using-java-14-records-in-jsf-via-eclipse.html
@Named("beanPerson")
@RequestScoped
public class BeanPerson {

    private List<Person> persons;
	
    @PostConstruct
    public void init() { 
        persons = new ArrayList<>();
        persons.add(new Person(1L, "john.doe@example.com", LocalDate.of(1978, 3, 26)));
        persons.add(new Person(2L, "jane.doe@example.com", LocalDate.of(1980, 10, 31)));
        persons.add(new Person(3L, "joe.bloggs@example.com", LocalDate.of(2002, 10, 5)));
    }
	
    public List<Person> getPersons() {
        return persons;
    }
}
