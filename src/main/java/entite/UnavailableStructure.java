package entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.LCUtil;

@Named //enlevé 10/06/2022 nécessaire car parent est named ?
@RequestScoped
@JsonInclude(JsonInclude.Include.NON_NULL) // déplacé 10/06/2022
public class UnavailableStructure implements Serializable{
@JsonIgnore private static final long serialVersionUID = 1L;
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

//private String[] itemStructure;
// rybriques stockées en db
private ArrayList<Structure> structureList = new ArrayList<>();
private String comment;

// autres rubriques non stockées en table
@JsonIgnore private Integer idclub;
@Pattern(regexp = "[a-zA-Z0-9éèàê ç]*",message="{club.name.characters}")
@JsonIgnore private Integer idcourse;
@Pattern(regexp = "[a-zA-Z0-9éèàê ç]*",message="{course.name.characters}")
@JsonIgnore private String courseName;
/* This regular expression will reject (because of ^) all inputs that have a blacklisted character in them.
The brackets define a character class,
and the \ is necessary before the dollar sign because dollar sign has a special meaning in regular expressions. */
@NotNull(message="{tarifMember.workitem.notnull}")
@JsonIgnore private String workItem;

@NotNull(message="{unavailable.courseid.notnull}")
@JsonIgnore private String workCourseId;

@JsonIgnore private boolean menuLaunched;
@JsonIgnore private boolean structureExists;
@JsonIgnore private boolean itemExists;

public UnavailableStructure(){    // constructor
    workCourseId = " ";
}

    public Integer getIdclub() {
        return idclub;
    }

    public void setIdclub(Integer idclub) {
        this.idclub = idclub;
    }

    public Integer getIdcourse() {
        return idcourse;
    }

    public void setIdcourse(Integer idcourse) {
        this.idcourse = idcourse;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public boolean isMenuLaunched() {
        return menuLaunched;
    }

    public void setMenuLaunched(boolean menuLaunched) {
        this.menuLaunched = menuLaunched;
    }

    public boolean isStructureExists() {
        return structureExists;
    }

    public void setStructureExists(boolean structureExists) {
        this.structureExists = structureExists;
    }

    public boolean isItemExists() {
        return itemExists;
    }

    public void setItemExists(boolean itemExists) {
        this.itemExists = itemExists;
    }

    public String getWorkItem() {
        return workItem;
    }

    public void setWorkItem(String workItem) {
        this.workItem = workItem;
    }

    public ArrayList<Structure> getStructureList() {
        return structureList;
    }

    public void setStructureList(ArrayList<Structure> structureList) {
        this.structureList = structureList;
    }

    public String getWorkCourseId() {
        return workCourseId;
    }

    public void setWorkCourseId(String workCourseId) {
        this.workCourseId = workCourseId;
    }
    
    
/*
    public void RemoveNull(){
   //     LOG.debug("itemstructure = " + itemStructure.toString());
   //     LOG.debug("itemstructure length = " + itemStructure.length);
    //    itemStructure = utils.LCUtil.removeNull1D(itemStructure);
         LOG.debug("null removed from all Arrays");
}
*/
 @Override
public String toString(){ 
    if(structureList.isEmpty()){ // == null){
       return ("UnavailableStructure is null, no print !! "  );
    }
    return 
        (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE
               + "<br/>idclub : "   + this.getIdclub()
               + ", idcourse :" + this.getIdcourse()
               + " ,course name : "   + this.getCourseName()
               + " <br/>,structureList: " + structureList.toString() 
        );
}

public static UnavailableStructure map(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
  try{
         String structure = rs.getString("ClubUnavailableStructure");
            LOG.debug("String structure from DB = " + structure);
         if(rs.getString("ClubUnavailableStructure") == null){
            LOG.debug("map - Unavailable Structure is null !! Null returned");
            return null;
         }
         ObjectMapper om = new ObjectMapper();
  //       om.registerModule(new JavaTimeModule());
         UnavailableStructure str = om.readValue(structure,UnavailableStructure.class);
            LOG.debug("UnavailableStructure extracted from database = "  + str);
            LOG.debug("nombre d'items structure = " + str.getStructureList().size());
 //           LOG.debug("array items structure = " + Arrays.deepToString(str.getItemStructure()));
         str.setIdclub(rs.getInt("IdClub"));
            LOG.debug("idclub setted = " + str.getIdclub());
   return str;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class