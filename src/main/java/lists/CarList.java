package lists;

import entite.Car;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.LCUtil;

public class CarList {
    private static List<Car> listcar = null;
public List<Car> getListAllCars(final Connection conn) throws SQLException{ 
if (listcar == null){
    LOG.debug(" ... completing CarList !! ");
      final String query = "select idcar, caryear, carbrand, carcolor" +
        " from car "
              ;
    // see Try-with_resources - Using Multiple Resources - tutorial jenkov  
try(PreparedStatement ps = conn.prepareStatement(query); // attention, le ";" est un séparateur !!!
    ResultSet rs =  ps.executeQuery()   // pas de ;
    ){

    //utils.LCUtil.logps(ps);
    listcar = new ArrayList<>();
		while(rs.next()) {
			Car c = new Car(); 
			c.setId(rs.getString("idcar") );
			c.setYear(rs.getInt("caryear") );
                        c.setBrand(rs.getString("carbrand") );
                        c.setColor(rs.getString("carcolor"));
			listcar.add(c); //store all data into a List
		}
 //    LOG.debug("listcar = " + listcar.toString());
   //  LOG.debug("listcar = " + Arrays..toString());
    return listcar;
}catch (SQLException e){       String msg = "Connection Failed! SQL Exception" + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){   String msg = "NullPointerException in getCarList() " + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal("Exception = " + npe.toString() );
    return null;
}catch (Exception ex){
    LOG.debug("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception = " + ex.toString() );
    return null;
}finally{
    //  DBConnection.closeQuietly(conn, null, null, ps);
     // DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
   ////      LOG.debug("escaped to listCar repetition with lazy loading");
    return listcar;
}

} //end method

    public static List<Car> getListcar() {
        return listcar;
    }

    public static void setListcar(List<Car> listcar) {
        CarList.listcar = listcar;
    }
}