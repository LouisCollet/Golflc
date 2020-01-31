package find;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindCountHoles implements interfaces.Log {

    public int findCountHoles(final int tee, final Connection conn) throws SQLException 
    {
          LOG.debug("starting findCountHoles, tee = {}", tee);
            PreparedStatement ps = null;
        //    ParameterMetaData paramMetaData = null;
// https://books.google.be/books?id=a8W8fKQYiogC&pg=PA192&lpg=PA192&dq=mysql+parametermetadata+jdbc&source=bl&ots=ol4Gxz6ZBS&sig=ACfU3U0RQLoiCYMV2_XstizfF5b_ALVzMg&hl=fr&sa=X&ved=2ahUKEwjJ45Wu_qDnAhUEJlAKHbx3ACIQ6AEwAHoECAkQAQ#v=onepage&q=mysql%20parametermetadata%20jdbc&f=false
// https://www.xyzws.com/javafaq/how-to-use-parametermetadata-to-learn-parameter-information/178
ResultSet rs = null;
       try{
      //          LOG.info("starting findCountHoles.. = ");
                String query = " SELECT * from hole where hole.tee_idtee = ?";
                ps = conn.prepareStatement(query);
                ParameterMetaData paramMetaData = ps.getParameterMetaData(); // new 
                if(paramMetaData == null){
                    LOG.info("db vendor NOT support ParameterMetaData");
                }else{
                    LOG.info("db vendor support ParameterMetaData = " + paramMetaData.toString());
                    LOG.info("parameter count = " + paramMetaData.getParameterCount());
                }

                ps.setInt(1, tee);
                utils.LCUtil.logps(ps);
                rs = ps.executeQuery();
                
                ResultSetMetaData rsMetaData = rs.getMetaData();
      //Number of columns
      System.out.println("Number of columns: "+rsMetaData.getColumnCount());
      //Column label
      System.out.println("Column Label: "+rsMetaData.getColumnLabel(1));
      //Column name
      System.out.println("Column Name: "+rsMetaData.getColumnName(1));
      //Number of columns
      System.out.println("Table Name: "+rsMetaData.getTableName(1));
                
                rs.last(); //on récupère le numéro de la ligne
                    LOG.info("ResultSet findCountHoles " + rs.getRow() + " lines.");
                return rs.getRow();
            } catch (SQLException e) {
                String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
                        + ", ErrorCode = " + e.getErrorCode();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return 0;
            } catch (Exception ex) {
                LOG.error("Exception ! " + ex);
                LCUtil.showMessageFatal("Exception = " + ex.toString());
                return 0;
            } finally {
                //   DBConnection.closeQuietly(conn, null, rs, ps);
                DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
            }
    } //end method

    public static void main(String[] args) throws SQLException, Exception {// testing purposes
        
        Connection conn = new DBConnection().getConnection();
        int i = new FindCountHoles().findCountHoles(147, conn);  // teeid
        LOG.info("main - after holes = " + i);
        DBConnection.closeQuietly(conn, null, null, null);

    }// end main
} //end Class