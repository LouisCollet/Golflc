package lists;

import entite.CompetitionDescription;
import entite.composite.ECompetition;
import static interfaces.Log.LOG;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import utils.DBConnection;
import utils.LCUtil;

public class CompetitionRoundsList implements interfaces.Log{
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 private static List<ECompetition> liste = null;

public List<ECompetition> list(final CompetitionDescription cd ,final @NotNull Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
if(liste == null){
        LOG.debug(" ... entering " + methodName);
        LOG.debug(" ... for competition " + cd);
        LOG.debug("   . for competitionId = " + cd.getCompetitionId());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
// à ajouter filtre sur les dates !
     final String query = """
       SELECT *
       FROM competition_data
       JOIN competition_description
           ON cmpDataCompetitionId = CompetitionId
       WHERE CompetitionId = ?
       ORDER BY CmpDataFlightNumber
     """ ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, cd.getCompetitionId());

     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
    int i = 0;
       while(rs.next()){
           i++;
	     ECompetition c = new ECompetition();
             c.setCompetitionDescription(entite.CompetitionDescription.map(rs));
             c.setCompetitionData(entite.CompetitionData.map(rs));
	liste.add(c);
      }
 //      liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda
 LOG.debug("after loop i = " + i);
    if(i == 0){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
    }else{
         LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
    }
 return liste;
}catch (SQLException e){
        String error = "SQL Exception in " + methodName + ": " + e;
	LOG.error(error);
        LCUtil.showMessageFatal(error);
        return null;
}catch (Exception ex){
    String error = "Exception in " + methodName + " / " + ex;
    LOG.error(error);
    LCUtil.showMessageFatal(error);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
 //   LOG.debug("escaped to " + methodName + "repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECompetition> getListe() {
        return liste;
    }

    public static void setListe(List<ECompetition> liste) {
        CompetitionRoundsList.liste = liste;
    }
@SuppressWarnings("unchecked")
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
      CompetitionDescription cd = new CompetitionDescription();
      cd.setCompetitionId(35);
  //    var v = new load.LoadCompetitionDescription().load(cd, conn);
   //   ECompetition ec = new ECompetition();
   //   ec.setCompetitionDescription(new load.LoadCompetitionDescription().load(cd, conn));
      var lp = new CompetitionRoundsList().list(cd,conn);
        LOG.debug("from main, after lp = " + lp);
  // serialize
      FileOutputStream fos= new FileOutputStream("c:/log/roundslist.ser");
      ObjectOutputStream oos= new ObjectOutputStream(fos);
      oos.writeObject(lp);
      oos.close();
      fos.close();
        LOG.debug("working !");
  // sera utilisée dans CompetitionStartList  !!
  // next lines : for testing only
     List<ECompetition> liec = new ArrayList<>();
     FileInputStream fileIn = new FileInputStream( "c:/log/roundslist.ser" );
     ObjectInputStream   in = new ObjectInputStream( fileIn );
     liec = (List<ECompetition>) in.readObject();
     fileIn.close();
     in.close();
       LOG.debug("working again !");
        LOG.debug("printing list = " + Arrays.deepToString(liec.toArray()));

 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null);
          }
   } // end main//
} //end class