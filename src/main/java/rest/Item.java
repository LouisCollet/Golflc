package rest;
// import jakarta.xml.bind.annotation.XmlRootElement;
//import jakarta.xml.bind.annotation.XmlRootElement;
// @XmlRootElement
public class Item {
public Item() { }// constructor
public Item(String description, int price) { 
            this.description = description;
            this.price = price;
	}
	private String description;
	private int price;
        // Getter- Setter methods

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
} //end class