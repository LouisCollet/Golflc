package find;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.Course;
import entite.Tarif;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindTarifData implements interfaces.Log, interfaces.GolfInterface{
    
final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 

public Tarif findCourseTarif(final Course course, final Connection conn) throws SQLException
{
        LOG.info("entering findClubTarif ...");
        LOG.info("starting findClub Tarif for course = " + course.toString());
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
  String query = 
    "SELECT TarifJson"
          + " from tarif"
          + " where tarif.course_idcourse = ?";

    ps = conn.prepareStatement(query);
    ps.setInt(1, course.getIdcourse() );
        utils.LCUtil.logps(ps);
        rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindTarif has " + rs.getRow() + " lines.");
        if(rs.getRow() == 0)
            { //  String msg = " -- No tarif found for this course = ";
                String msgerr =  LCUtil.prepareMessageBean("tarif.notfound");
                LOG.error(msgerr);
                LCUtil.showMessageFatal(msgerr);
            //    LOG.error(msg + " second sending");
                return null;
            //    throw new Exception(msg);
            }
        if(rs.getRow() > 1)
            {   throw new Exception(" -- More than 1 tarif = " + rs.getRow() );  }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
        String s = null;
	while(rs.next())
        {
             s = rs.getString("TarifJson");
	}
   //     LOG.info("line 01");
        ObjectMapper om = new ObjectMapper();
  //  	om.enable(SerializationFeature.INDENT_OUTPUT);//Set pretty printing of json
 //       om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // fields private accepted in class tarif 
        Tarif t = om.readValue(s,Tarif.class);
            LOG.info("Tarif extracted from database = "  + t.toString());
        return t;
}catch (SQLException e){
    String msg = "SQL Exception FindTarif= " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in FindTarif()" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

}//end method

public static void main(String[] args) throws Exception , Exception{

    Connection conn = new DBConnection().getConnection();
    Course course = new Course();
    course.setIdcourse(102);
  //  FindTarifData ftd = new FindTarifData();
    Tarif t1 = new FindTarifData().findCourseTarif(course, conn);
     LOG.info("Tarif extracted from database = "  + t1.toString());
//findPlayerHandicap(player,round, conn);
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class

