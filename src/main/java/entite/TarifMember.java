package entite;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
// @JsonInclude(Include.NON_NULL)  // ne fonctionne pas dans table multidimentional intéressant ?
@Named
@SessionScoped
public class TarifMember implements Serializable{

    @NotNull(message="{tarifMember.startdate.notnull}")
//@JsonSerialize(using = ToStringSerializer.class)
    private LocalDateTime memberStartDate;

    @NotNull(message="{tarifMember.enddate.notnull}")
//@JsonSerialize(using = ToStringSerializer.class)
    private LocalDateTime memberEndDate;
    
@JsonIgnore
    private Integer tarifMemberIndex; // pour periods
@JsonIgnore
    private Integer tarifMemberEquipmentsIndex; // pour periods
  private String [][] priceEquipments; //new 28-02-2019
  @JsonIgnore
    private Integer [] equipmentsChoice;
  private String comment;
  private String [][] membersBase;
@JsonIgnore
    private Integer [] membersChoice;
@JsonIgnore
@NotNull(message="{tarifMember.workitem.notnull}")
  private String workItem;
@JsonIgnore
 @NotNull(message="{tarifMember.workprice.notnull}")
  private String workPrice;
@JsonIgnore
 //@NotNull(message="{tarifMember.workprice.notnull}")
  private String workAge;
@JsonIgnore
private Date workStartDate;
@JsonIgnore
private Date workEndDate;

public TarifMember(){ // constructor
        membersBase = new String[25][3]; // 10 = lines, 2 = columnns : item, prix, age
        priceEquipments = new String[15][2]; // item, price
        tarifMemberIndex = 0;
        tarifMemberEquipmentsIndex = 0;
        membersChoice = new Integer[membersBase.length];  // choice of quantity egals 0, 1
        equipmentsChoice = new Integer[priceEquipments.length];  // new 28-02-2019
        Arrays.fill(membersChoice, 0);
        Arrays.fill(equipmentsChoice, 0);
    } // end constructor


    public Integer getTarifMemberIndex() {
        return tarifMemberIndex;
    }

    public void setTarifMemberIndex(Integer tarifMemberIndex) {
        this.tarifMemberIndex = tarifMemberIndex;
    }

    public Integer getTarifMemberEquipmentsIndex() {
        return tarifMemberEquipmentsIndex;
    }

    public void setTarifMemberEquipmentsIndex(Integer tarifMemberEquipmentsIndex) {
        this.tarifMemberEquipmentsIndex = tarifMemberEquipmentsIndex;
    }

    public LocalDateTime getMemberStartDate() {
        return memberStartDate;
    }

    
    public void setMemberStartDate(LocalDateTime memberStartDate) {
     LOG.info("setMemberStartDate = " + memberStartDate);
        this.memberStartDate = memberStartDate;
    }

    public LocalDateTime getMemberEndDate() {
   //     LOG.info("getMemberEndDate " + memberEndDate);
        return memberEndDate;
    }

    public void setMemberEndDate(LocalDateTime memberEndDate) {
  //      LOG.info("setMemberEndDate " + memberEndDate);
        this.memberEndDate = memberEndDate;
    }

    public String[][] getMembersBase() {
  //    LOG.info("getMembers = " + Arrays.deepToString(membersBase));
        return membersBase;
    }
/*
    public void setMembersBase(String[][] members) { 
        LOG.info("setMembers before= " + Arrays.deepToString(members));
    //    this.membersBase = members;
        this.membersBase = utils.LCUtil.removeNull2D(members);
        LOG.info("setMembers after = " + Arrays.deepToString(this.membersBase));
    }
*/

    public void setMembersBase(String[][] membersBase) {
        this.membersBase = membersBase;
    }

    public Integer[] getMembersChoice() {
        return membersChoice;
    }

    public void setMembersChoice(Integer[] membersChoice) {
        this.membersChoice = membersChoice;
    }

    public String getWorkItem() {
        return workItem;
    }

    public void setWorkItem(String workItem) {
        this.workItem = workItem;
    }

    public String getWorkPrice() {
        return workPrice;
    }

    public void setWorkPrice(String workPrice) {
        this.workPrice = workPrice;
    }

    public String getWorkAge() {
        return workAge;
    }

    public void setWorkAge(String workAge) {
        this.workAge = workAge;
    }
    
  public void RemoveNull(){
   membersBase = utils.LCUtil.removeNull2D(membersBase);
   priceEquipments = utils.LCUtil.removeNull2D(priceEquipments);
 //  LOG.info("null removed from membersBase");
//   membersChoice = utils.LCUtil.removeNull1D(membersChoice);
   
   }

    public Date getWorkStartDate() {
        return workStartDate;
    }

    public void setWorkStartDate(Date workStartDate) {
        LOG.info("setWorkStartDate = " + workStartDate);// ici loader l'aute field ?
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

    public String[][] getPriceEquipments() {
        return priceEquipments;
    }

    public void setPriceEquipments(String[][] priceEquipments) {
        this.priceEquipments = priceEquipments;
    }

    public Integer[] getEquipmentsChoice() {
        return equipmentsChoice;
    }

    public void setEquipmentsChoice(Integer[] equipmentsChoice) {
        this.equipmentsChoice = equipmentsChoice;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
  
 @Override
public String toString()
{  try {
    LOG.info("starting toString TarifMember !");
    return
            (NEW_LINE 
            + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + NEW_LINE + "<br>"
             + "Start Date : "   + this.getMemberStartDate().format(ZDF_TIME_DAY)
            + NEW_LINE + "<br>"
            + "End Date : "   + this.getMemberEndDate().format(ZDF_TIME_DAY)
            + NEW_LINE + "<br>"
            + "MembersBase [][]: " + Arrays.deepToString(getMembersBase() )
            + NEW_LINE + "<br>"
            + "MembersChoice []: "   + Arrays.toString(getMembersChoice() )
            + NEW_LINE + "<br>"
            + "priceEquipments [][] : " + Arrays.deepToString(getPriceEquipments() )
            + NEW_LINE + "<br>"
            + "equipmentsChoice : "   + Arrays.toString(getEquipmentsChoice() )
            + NEW_LINE + "<br>"
            + " workItem: "   + this.getWorkItem()
            + NEW_LINE + "<br>"
            + " workPrice: "   + this.getWorkPrice()
            + NEW_LINE + "<br>"
            + " workAge: "   + this.getWorkAge()
            + NEW_LINE + "<br>"
            + " Comment: "   + this.getComment()
            );
        } catch (Exception ex) {
           LOG.error("Exception in TarifMember to String" + ex);
           return null;
        }
} //end method
} // end class