package entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TarifSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String code;
    private double price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime creationDate;

    // Work fields for wizard input
    @NotNull(message = "{tarifSubscription.price.notnull}")
    @Positive(message = "{tarifSubscription.price.positive}")
    @JsonIgnore private Double workPrice;

    @NotNull(message = "{tarifSubscription.startdate.notnull}")
    @JsonIgnore private LocalDateTime workStartDate;

    @NotNull(message = "{tarifSubscription.enddate.notnull}")
    @JsonIgnore private LocalDateTime workEndDate;

    // List for wizard confirmation display
    @JsonIgnore private List<TarifSubscription> tarifList = new ArrayList<>();

    public TarifSubscription() { }

    // === Getters/Setters ===

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public Double getWorkPrice() { return workPrice; }
    public void setWorkPrice(Double workPrice) { this.workPrice = workPrice; }

    public LocalDateTime getWorkStartDate() { return workStartDate; }
    public void setWorkStartDate(LocalDateTime workStartDate) { this.workStartDate = workStartDate; }

    public LocalDateTime getWorkEndDate() { return workEndDate; }
    public void setWorkEndDate(LocalDateTime workEndDate) { this.workEndDate = workEndDate; }

    public List<TarifSubscription> getTarifList() { return tarifList; }
    public void setTarifList(List<TarifSubscription> tarifList) { this.tarifList = tarifList; }

    @Override
    public String toString() {
        return "TarifSubscription{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", price=" + price +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    } // end method

} // end class
