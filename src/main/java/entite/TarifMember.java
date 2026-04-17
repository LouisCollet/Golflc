package entite;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
// import jakarta.annotation.PostConstruct;  // migrated 2026-02-26 — POJO, not CDI-managed
// import jakarta.enterprise.context.RequestScoped;  // migrated 2026-02-24
// import jakarta.inject.Named;  // migrated 2026-02-24
import jakarta.validation.constraints.NotNull;
import utils.LCUtil;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
// @JsonInclude(Include.NON_NULL)  // ne fonctionne pas dans table multidimentional intéressant ?
// @Named("tarifMember")  // migrated 2026-02-24
// @RequestScoped  // migrated 2026-02-24

// à modifier commer tarifgreenfee en utilisant des arraylist ipv arrays
public class TarifMember implements Serializable{
    
 @NotNull(message="{tarifMember.startdate.notnull}")
   private LocalDateTime startDate;
 @NotNull(message="{tarifMember.enddate.notnull}")
    private LocalDateTime endDate;
 
@JsonInclude(Include.NON_NULL) // new 09/05/2022
private String comment;

private ArrayList<EquipmentsAndBasicAndRange> basicList = new ArrayList<>();

private ArrayList<EquipmentsAndBasic> equipmentsList = new ArrayList<>(); 

@JsonInclude(Include.NON_NULL) // new 09/05/2022
private String discount;

@JsonIgnore  private Integer tarifMemberIdClub; // pour periods

@NotNull(message="{tarifMember.workitem.notnull}")
@JsonIgnore  private String workItem;

 @NotNull(message="{tarifMember.workprice.notnull}")
 @JsonIgnore  private Double workPrice;  // mod 06/05/2022
 
@JsonIgnore private String workRangeAge; // pas de contrôle ??ex 10-25 ans

@JsonIgnore private LocalDateTime workStartDate;

 @NotNull(message="{tarifMember.enddate.notnull}")

 @JsonIgnore private LocalDateTime workEndDate;

public TarifMember(){ // constructor
//        membersBase = new String[25][3]; // 10 = lines, 2 = columnns : item, prix, age
 //       priceEquipments = new String[15][2]; // item, price
//        tarifMemberIndex = 0;
//        tarifMemberEquipmentsIndex = 0;
//        membersChoice = new Integer[25];  // choice of quantity egals 0, 1
 //       Arrays.fill(membersChoice, 0);
 //       equipmentsChoice = new Integer[15];  // new 28-02-2019
  //      Arrays.fill(equipmentsChoice, 0);
        comment = ""; 
        discount = "";
    } // end constructor

    // @PostConstruct  // migrated 2026-02-26 — POJO, not CDI-managed
    public void init(){
 //        sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
 //        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
// https://github.com/OWASP/java-html-sanitizer
            LOG.debug("sanitizer started ! = " );
    }

    public Integer getTarifMemberIdClub() {
        return tarifMemberIdClub;
    }

    public void setTarifMemberIdClub(Integer tarifMemberIdClub) {
        this.tarifMemberIdClub = tarifMemberIdClub;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getWorkItem() {
        return workItem;
    }

    public void setWorkItem(String workItem) {
        this.workItem = workItem;
    }

    public ArrayList<EquipmentsAndBasicAndRange> getBasicList() {
        return basicList;
    }

    public void setBasicList(ArrayList<EquipmentsAndBasicAndRange> basicList) {
        this.basicList = basicList;
    }

    public Double getWorkPrice() {
        return workPrice;
    }

    public void setWorkPrice(Double workPrice) {
        this.workPrice = workPrice;
    }

    public String getWorkRangeAge() {
        return workRangeAge;
    }

    public void setWorkRangeAge(String workRangeAge) {
        this.workRangeAge = workRangeAge;
    }

    public LocalDateTime getWorkStartDate() {
        return workStartDate;
    }

    public void setWorkStartDate(LocalDateTime workStartDate) {
        this.workStartDate = workStartDate;
    }

    public LocalDateTime getWorkEndDate() {
        return workEndDate;
    }

    public void setWorkEndDate(LocalDateTime workEndDate) {
        this.workEndDate = workEndDate;
    }

    public ArrayList<EquipmentsAndBasic> getEquipmentsList() {
        return equipmentsList;
    }

    public void setEquipmentsList(ArrayList<EquipmentsAndBasic> equipmentsList) {
        this.equipmentsList = equipmentsList;
    }

    public String getComment() {
 //       LOG.debug("getComment = " + comment);
   // ??      content = content.replaceAll("\\r|\\n", "");  
        return comment;
    }

    public void setComment(String comment) {
        if (comment == null) {
            this.comment = null;
            return;
        }
        PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(Sanitizers.STYLES);
        this.comment = sanitizer.sanitize(comment);
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

 @Override
public String toString(){
 try {
    LOG.debug("starting toString TarifMember !");
    return
            (NEW_LINE  + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + NEW_LINE + "<br>"
             + "Start Date : " + startDate //.format(ZDF_TIME_DAY)
            + NEW_LINE + "<br>"
            + "End Date : "   + endDate //.format(ZDF_TIME_DAY)
            + NEW_LINE + "<br>"
            + "Id Club : "   + this.getTarifMemberIdClub()
            + NEW_LINE + "<br>"
            + "basicList: " + basicList
            + NEW_LINE + "<br>"
  //          + "MembersChoice int[]: "   + Arrays.toString(getMembersChoice() )
   //         + NEW_LINE + "<br>"
            + "EquipmentsList : " + equipmentsList //Arrays.deepToString(utils.LCUtil.removeNull2D(getPriceEquipments()))
            + NEW_LINE + "<br>"
   //         + "EquipmentsChoice int[]: "   + Arrays.toString(getEquipmentsChoice() )
  //          + NEW_LINE + "<br>"
            + " workItem: "   + this.getWorkItem()
            + NEW_LINE + "<br>"
            + " workPrice: "   + this.getWorkPrice()
            + NEW_LINE + "<br>"
            + " workAge: "   + this.getWorkRangeAge()
            + NEW_LINE + "<br>"
            + " Discount: "   + this.getDiscount()
            + NEW_LINE + "<br>"
            + " Comment: "   + this.getComment()
            );
        } catch (Exception ex) {
           LOG.error("Exception in TarifMember to String" + ex);
           return null;
        }
} //end method
} // end class