package lc.golfnew;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.primefaces.event.DragDropEvent;
import entite.Car;
import entite.CarService;
import static interfaces.Log.LOG;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("dndCarsView")
@SessionScoped

public class DNDCarsView implements Serializable {

    @Inject private CarService service; // new CDI compliant
    private List<Car> cars;
    private List<Car> droppedCars;
    private Car selectedCar;
     
    @PostConstruct
    public void init() {
 //       LOG.info("from init");
        cars = service.createCars(8);
        LOG.info("from init : cars = " + cars.toString());
        droppedCars = new ArrayList<>();
        LOG.info("from init : droppedCcars = " + droppedCars.toString());
    }
     
    public void onCarDrop(DragDropEvent ddEvent) {
        Car car = ((Car) ddEvent.getData());
            LOG.info("Car dropped = " + car.toString());
        droppedCars.add(car);
        cars.remove(car);
    }

    public void setService(CarService service) {
        this.service = service;
    }
 
    public List<Car> getCars() {
        LOG.info("from getCars = " + cars.toString());
        return cars;
    }
 
    public List<Car> getDroppedCars() {
        return droppedCars;
    }    
 
    public Car getSelectedCar() {
        return selectedCar;
    }
 
    public void setSelectedCar(Car selectedCar) {
        this.selectedCar = selectedCar;
    }
}