package entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnavailableStructure implements Serializable{
@JsonIgnore private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

// rubriques stockées en db
private ArrayList<Structure> structureList = new ArrayList<>();
private String comment;
private String choiceType;
private LocalDateTime lastUpdate;

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
@JsonIgnore private boolean periodSaved;        // period data validated in bean (Tab 1 button)
@JsonIgnore private boolean periodPersistedToDB; // period record actually INSERTed in DB

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

    public boolean isPeriodSaved() {
        return periodSaved;
    }

    public void setPeriodSaved(boolean periodSaved) {
        this.periodSaved = periodSaved;
    }

    public boolean isPeriodPersistedToDB() {
        return periodPersistedToDB;
    }

    public void setPeriodPersistedToDB(boolean periodPersistedToDB) {
        this.periodPersistedToDB = periodPersistedToDB;
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

    public String getChoiceType() {
        return choiceType;
    }

    public void setChoiceType(String choiceType) {
        this.choiceType = choiceType;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

 @Override
public String toString(){ 
    if(structureList.isEmpty()){ // == null){
       return (this.getClass().getSimpleName().toUpperCase() + " is null, no print !! ");
    }
    return 
        (NEW_LINE + "FROM ENTITE " + this.getClass().getSimpleName().toUpperCase() + NEW_LINE
               + "<br/>idclub : "   + this.getIdclub()
               + ", idcourse :" + this.getIdcourse()
               + " ,course name : "   + this.getCourseName()
               + " <br/>,structureList: " + structureList.toString() 
        );
}

} //end method
//} // end class