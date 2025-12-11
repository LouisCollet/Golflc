package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;
import utils.LCUtil;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
// @JsonInclude(Include.NON_NULL)  // ne fonctionne pas dans table multidimentional intéressant ?
@Named("tarifMember") // nécessaire ??
@RequestScoped

// à modifier commer tarifgreenfee en utilisant des arraylist ipv arrays
public class TarifMember implements Serializable{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 @NotNull(message="{tarifMember.startdate.notnull}")
   private LocalDateTime startDate;
 @NotNull(message="{tarifMember.enddate.notnull}")
    private LocalDateTime endDate;
 
@JsonInclude(Include.NON_NULL) // new 09/05/2022
private String comment;
// private String [][] membersBase; mod 06/05/2022 replaces by next line
private ArrayList<EquipmentsAndBasicAndRange> basicList = new ArrayList<>();

// private String [][] priceEquipments; 06/05/2022 replaces bu next line
private ArrayList<EquipmentsAndBasic> equipmentsList = new ArrayList<>(); 

@JsonInclude(Include.NON_NULL) // new 09/05/2022
private String discount;

@JsonIgnore  private Integer tarifMemberIdClub; // pour periods

//@JsonIgnore private Integer [] membersChoice;
//@JsonIgnore  private Integer [] equipmentsChoice;

@NotNull(message="{tarifMember.workitem.notnull}")
@JsonIgnore  private String workItem;

 @NotNull(message="{tarifMember.workprice.notnull}")
// @JsonIgnore  private String workPrice;
 @JsonIgnore  private Double workPrice;  // mod 06/05/2022
 
@JsonIgnore private String workRangeAge; // pas de contrôle ??ex 10-25 ans

  @NotNull(message="{tarifMember.startdate.notnull}")
//@JsonIgnore    private Date workStartDate;
@JsonIgnore private LocalDateTime workStartDate;

 @NotNull(message="{tarifMember.enddate.notnull}")
 //@JsonIgnore   private Date workEndDate;
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

    @PostConstruct
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


// modifié was String[][]
 //   public String[][] getMembersBase() {
  //    LOG.debug("getMembers = " + Arrays.deepToString(membersBase));
 //   return membersBase;
  //      return utils.LCUtil.array2DStringToInt(membersBase);
  //  }
    
    /*  à vérifier !!
 @JsonIgnore  // important, sinonn cree MembersBase Format dans Json !!   
    public String[][] getMembersBaseFormat() {
        // formattage de présentation du montant et de l'age (null devient 00-00
        return utils.LCUtil.ModifyMembersBase(membersBase);
    }
    */
/*
    public void setMembersBase(String[][] members) { 
        LOG.debug("setMembers before= " + Arrays.deepToString(members));
    //    this.membersBase = members;
        this.membersBase = utils.LCUtil.removeNull2D(members);
        LOG.debug("setMembers after = " + Arrays.deepToString(this.membersBase));
    }
*/

/*
    public Integer[] getMembersChoice() {
        return membersChoice;
    }

    public void setMembersChoice(Integer[] membersChoice) {
        this.membersChoice = membersChoice;
    }
*/
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




    
  //public void RemoveNull(){
//   membersBase = utils.LCUtil.removeNull2D(membersBase);
 //  priceEquipments = utils.LCUtil.removeNull2D(priceEquipments);
 //  LOG.debug("null removed from membersBase");
//   membersChoice = utils.LCUtil.removeNull1D(membersChoice);
   
 //  }
/*
    public Date getWorkStartDate() {
        return workStartDate;
    }

    public void setWorkStartDate(Date workStartDate) {
        LOG.debug("setWorkStartDate = " + workStartDate);// ici loader l'aute field ?
        setMemberStartDate(utils.LCUtil.DatetoLocalDateTime(workStartDate));
        this.workStartDate = workStartDate;
    }

    public Date getWorkEndDate() {
        return workEndDate;
    }

    public void setWorkEndDate(Date workEndDate) {
        setMemberEndDate(utils.LCUtil.DatetoLocalDateTime(workEndDate));
        this.workEndDate = workEndDate;
    }
*/
    public ArrayList<EquipmentsAndBasic> getEquipmentsList() {
        return equipmentsList;
    }

    public void setEquipmentsList(ArrayList<EquipmentsAndBasic> equipmentsList) {
        this.equipmentsList = equipmentsList;
    }
/*
     public Integer[] getEquipmentsChoice() {
        return equipmentsChoice;
    }

    public void setEquipmentsChoice(Integer[] equipmentsChoice) {
        this.equipmentsChoice = equipmentsChoice;
    }
*/
    public String getComment() {
 //       LOG.debug("getComment = " + comment);
   // ??      content = content.replaceAll("\\r|\\n", "");  
        return comment;
    }

    public void setComment(String comment) {
 //       LOG.debug("getComment = " + comment);
 //    PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
    //    String cleanResults = sanitizer.sanitize("<p>Hello, <b>World!</b>");
   //    String safeHTML = sanitizer.sanitize(comment);
   //   LOG.debug("cleanResults are = " + safeHTML);
        
        this.comment = comment;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }


public static TarifMember map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
        TarifMember tm = new TarifMember();
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        tm = om.readValue(rs.getString("TarifMemberJson"),TarifMember.class);
 //             LOG.debug("TarifMember extracted from database = "  + tm.toString());
        tm.setStartDate(rs.getTimestamp("TarifMemberStartDate").toLocalDateTime());
        tm.setEndDate(rs.getTimestamp("TarifMemberEndDate").toLocalDateTime());
        tm.setTarifMemberIdClub(rs.getInt("TarifMemberIdClub"));
      LOG.debug("TarifMember tm returned from map = " + tm);
   return tm;
}catch(Exception e){
   String msg = "£££ Exception in " + methodName + " / "+ e.getMessage();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
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