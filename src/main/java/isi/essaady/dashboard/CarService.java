package isi.essaady.dashboard;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class CarService {
    
    
    
    
     
    public List<Car> createCars(int size) {
        List<Car> list = new ArrayList<Car>();
        for(int i = 0 ; i < size ; i++) {
            list.add(new Car("Hamza", "kuorosaki", 12, 35));
        }
         
        return list;
    }
     
    
}