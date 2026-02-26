package lc.golfnew;
 
import entite.Car;
import entite.CarService;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.component.column.Column;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;
import connection_package.DBConnection;

@Named("editView")
//@SessionScoped
@RequestScoped

public class EditView implements Serializable, interfaces.Log {
     
    private List<Car> cars2;
    @Inject private CarService service;
    private Car car;

    @PostConstruct
    public void init(){
        cars2 = service.createCars(10);
            LOG.debug("car2 = " + cars2.toString());
    }
 
    public List<Car> getCars2() {
        return cars2;
    }
     
    public List<String> getBrands() {
        return service.getBrands();
    }
     
    public List<String> getColors() {
        return service.getColors();
    }
 
    public void setService(CarService service) {
        this.service = service;
    }
     
    public void onRowEdit(RowEditEvent<Object> event) throws SQLException, Exception {
        Car c = (Car) event.getObject();
   //     FacesMessage msg = new FacesMessage("Car Edited = ", c.getId() + " / " + c.getYear());
        FacesMessage msg = new FacesMessage("Car Edited = ", (c.getId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        
      //  Object o = event.getObject();
     //     LOG.debug("on row edit, new id = " + ((Car) o).getId());
          LOG.debug("on row edit, new id = " + c.getId() );
          LOG.debug("on row edit, new year = " + c.getYear());
          LOG.debug("on row edit, new brand = " + c.getBrand());
          LOG.debug("on row edit, new Color = " + c.getColor() );
    // ici faire appel à modifier database
          car.setId(c.getId() );
          car.setYear(c.getYear());
          car.setBrand(c.getBrand());
          car.setColor(c.getColor());
          Connection conn = new DBConnection().getConnection();
     //     modify.ModifyCar mc = ;
          new update.ModifyCar().modifyCar(car, conn);
          DBConnection.closeQuietly(conn, null, null,null);
          
       //   LOG.debug("on row edit, new Color = " + ((Car) event.getObject()).getColor() );
      //    LOG.debug("on row edit, old Color = " + cars1.get(0).getColor() );
    }
     
    public void onRowEditInit(RowEditEvent<Object> event) {
        // activé par clic sur le crayon
        LOG.debug("... entering onRowEditInit, Object = " + event.getObject());
         Car c = (Car) event.getObject();
          LOG.debug("on row init, old id = " + c.getId() );
          LOG.debug("on row init, old year = " + c.getYear());
          LOG.debug("on row init, old brand = " + c.getBrand());
          LOG.debug("on row init, old Color = " + c.getColor() );
   //       LOG.debug("on row init, old Color = " + cars1.get(0).getColor() );
         
      //  LOG.debug("onEditInit, Component = " + event.getComponent());
}
    
    public void onRowCancel(RowEditEvent<Object> event) {
         LOG.debug("... entering onRowCancel ");
        FacesMessage msg = new FacesMessage("Edit Cancelled", ((Car) event.getObject()).getId());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
     
    public void onCellEdit(CellEditEvent<Object> event) {
         LOG.debug("... entering onCellEdit ");
        int alteredRow = event.getRowIndex();
          LOG.debug("Row Index = " + alteredRow);
        Column col = (Column) event.getColumn();
            LOG.debug("column key = " + col.getColumnKey());
      //  String column_name;
        String column_name = col.getHeaderText();
            LOG.debug("column header text = " + column_name);
        Object oldValue = event.getOldValue();
            LOG.debug("Old Value = " + oldValue);
        String Id = cars2.get(alteredRow).getId();
            LOG.debug("Old Value Id = " + cars2.get(alteredRow).getId()); // on a la clé
        Object newValue = event.getNewValue();
         LOG.debug("New Value = " + newValue);
         
         LOG.debug("Cell Edited : id = {}, old = {} , new = {}", Id, oldValue, newValue);
         
        if(newValue != null && !newValue.equals(oldValue)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cell Edited", "Old: " + oldValue + ", New:" + newValue);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
}
