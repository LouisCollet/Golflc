package lc.golfnew;
 
import entite.Car;
import entite.CarService;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.component.column.Column;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;
import utils.DBConnection;

@Named("editView")
@SessionScoped

public class EditView implements Serializable, interfaces.Log {
     
    private List<Car> cars2;
    @Inject private CarService service;
    private Car car;

    @PostConstruct
    public void init()
    {
        cars2 = service.createCars(10);
            LOG.info("car2 = " + cars2.toString());
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
     
    public void onRowEdit(RowEditEvent event) throws SQLException, Exception {
        Car c = (Car) event.getObject();
   //     FacesMessage msg = new FacesMessage("Car Edited = ", c.getId() + " / " + c.getYear());
        FacesMessage msg = new FacesMessage("Car Edited = ", (c.getId()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        
      //  Object o = event.getObject();
     //     LOG.info("on row edit, new id = " + ((Car) o).getId());
          LOG.info("on row edit, new id = " + c.getId() );
          LOG.info("on row edit, new year = " + c.getYear());
          LOG.info("on row edit, new brand = " + c.getBrand());
          LOG.info("on row edit, new Color = " + c.getColor() );
    // ici faire appel à modifier database
       //   faut connection

          car.setId(c.getId() );
          car.setYear(c.getYear());
          car.setBrand(c.getBrand());
          car.setColor(c.getColor());
          DBConnection dbc = new DBConnection();
          Connection conn = dbc.getConnection();
          modify.ModifyCar mc = new modify.ModifyCar();
          mc.modifyCar(car, conn);
          DBConnection.closeQuietly(conn, null, null,null);
          
       //   LOG.info("on row edit, new Color = " + ((Car) event.getObject()).getColor() );
      //    LOG.info("on row edit, old Color = " + cars1.get(0).getColor() );
    }
     
    public void onRowEditInit(RowEditEvent event) {
        // activé par clic sur le crayon
        LOG.info("... entering onRowEditInit, Object = " + event.getObject());
         Car c = (Car) event.getObject();
          LOG.info("on row init, old id = " + c.getId() );
          LOG.info("on row init, old year = " + c.getYear());
          LOG.info("on row init, old brand = " + c.getBrand());
          LOG.info("on row init, old Color = " + c.getColor() );
   //       LOG.info("on row init, old Color = " + cars1.get(0).getColor() );
         
      //  LOG.info("onEditInit, Component = " + event.getComponent());
}
    
    public void onRowCancel(RowEditEvent event) {
         LOG.info("... entering onRowCancel ");
        FacesMessage msg = new FacesMessage("Edit Cancelled", ((Car) event.getObject()).getId());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
     
    public void onCellEdit(CellEditEvent event) {
         LOG.info("... entering onCellEdit ");
        int alteredRow = event.getRowIndex();
          LOG.info("Row Index = " + alteredRow);
        Column col = (Column) event.getColumn();
            LOG.info("column key = " + col.getColumnKey());
      //  String column_name;
        String column_name = col.getHeaderText();
            LOG.info("column header text = " + column_name);
        Object oldValue = event.getOldValue();
            LOG.info("Old Value = " + oldValue);
        String Id = cars2.get(alteredRow).getId();
            LOG.info("Old Value Id = " + cars2.get(alteredRow).getId()); // on a la clé
        Object newValue = event.getNewValue();
         LOG.info("New Value = " + newValue);
         
         LOG.debug("Cell Edited : id = {}, old = {} , new = {}", Id, oldValue, newValue);
         
        if(newValue != null && !newValue.equals(oldValue)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cell Edited", "Old: " + oldValue + ", New:" + newValue);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
}
