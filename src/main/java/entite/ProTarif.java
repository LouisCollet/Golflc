package entite;

import java.io.Serializable;

public class ProTarif implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double weekdayPrice;
    private Double weekendPrice;

    public ProTarif() { }

    public Double getWeekdayPrice()              { return weekdayPrice; }
    public void   setWeekdayPrice(Double price)  { this.weekdayPrice = price; }

    public Double getWeekendPrice()              { return weekendPrice; }
    public void   setWeekendPrice(Double price)  { this.weekendPrice = price; }

    @Override
    public String toString() {
        return "ProTarif{weekday=" + weekdayPrice + ", weekend=" + weekendPrice + "}";
    } // end method
} // end class
