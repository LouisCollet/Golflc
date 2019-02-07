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
import javax.inject.Named;
import javax.validation.constraints.NotNull;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
// @JsonInclude(Include.NON_NULL)  // ne fonctionne pas dans table multidimentional intéressant ?
@Named
public class TarifMember implements Serializable{

    @NotNull(message="{tarifMember.startdate.notnull}")
//@JsonSerialize(using = ToStringSerializer.class)
    private LocalDateTime memberStartDate;

    @NotNull(message="{tarifMember.enddate.notnull}")
//@JsonSerialize(using = ToStringSerializer.class)
    private LocalDateTime memberEndDate;
    
@JsonIgnore
    private Integer tarifMemberIndex; // pour periods
// @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MM")
    private String [][] membersBase;
@JsonIgnore
    private Integer [] membersChoice;
@JsonIgnore
@NotNull(message="{tarifMember.workitem.notnull}")
  private String workItem;
@JsonIgnore
 @NotNull(message="{tarifMember.workprice.notnull}")
  private String workPrice;
private Date workStartDate;
private Date workEndDate;

public TarifMember(){ // constructor
        membersBase = new String[25][2]; // 10 = lines, 2 = columnns : item, prix, choix
        tarifMemberIndex = 0; 
        membersChoice = new Integer[membersBase.length];  // choice of quantity egals 0, 1
        Arrays.fill(membersChoice, 0);
    } // end constructor


    public Integer getTarifMemberIndex() {
        return tarifMemberIndex;
    }

    public void setTarifMemberIndex(Integer tarifMemberIndex) {
        this.tarifMemberIndex = tarifMemberIndex;
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
    
  public void RemoveNull(){
   membersBase = utils.LCUtil.removeNull2D(membersBase);
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
  
 @Override
public String toString()
{  try {
    return
            (NEW_LINE 
            + "FROM ENTITE : " + this.getClass().getSimpleName().toUpperCase()
            + NEW_LINE + "<br>"
             + "Start Date : "   + this.getMemberStartDate().format(ZDF_TIME_DAY)
            + NEW_LINE + "<br>"
            + "End Date : "   + this.getMemberEndDate().format(ZDF_TIME_DAY)
            + NEW_LINE + "<br>"
            + "Base : " + Arrays.deepToString(getMembersBase() )
            + NEW_LINE + "<br>"
            + "Choice : "   + Arrays.toString(getMembersChoice() )
            + NEW_LINE + "<br>"
            + " workItem: "   + this.getWorkItem()
            + NEW_LINE + "<br>"
            + " workPrice: "   + this.getWorkPrice()
            );
        } catch (Exception ex) {
           LOG.error("Exception in TarifMember to String" + ex);
           return null;
        }
} //end method
} // end class