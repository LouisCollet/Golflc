package lists;

import entite.CompetitionDescription;
import entite.composite.ECompetition;
import entite.HandicapIndex;
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

public class CompetitionInscriptionsList implements interfaces.Log{
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 private static List<ECompetition> liste = null;

public List<ECompetition> list(final CompetitionDescription cd ,final @NotNull Connection conn) throws SQLException{ // mod 17-03-2022
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("entering list CompetitionInscriptionsList ");
if(liste == null){
        LOG.debug(" ... entering " + methodName);
        LOG.debug(" ... for competition description =" + cd);
        if(cd == null){
            return liste = null;
        }
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     final String query = """
       SELECT *
       FROM competition_data
       JOIN competition_description
           ON cmpDataCompetitionId = CompetitionId
       WHERE CompetitionId = ?
       ORDER BY CmpDataFlightNumber
     """ ;
 /* https://cr.openjdk.java.net/~jlaskey/Strings/TextBlocksGuide_v11.html
*/
     ps = conn.prepareStatement(query);
     ps.setInt(1, cd.getCompetitionId());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     liste = new ArrayList<>();
     int i = 0;
     while(rs.next()){
           i++;
            ECompetition ec = new ECompetition();
            ec.setCompetitionDescription(entite.CompetitionDescription.map(rs));
            ec.setCompetitionData(entite.CompetitionData.map(rs));
         // ajouter handicap WHS
            HandicapIndex handicapIndex = new HandicapIndex();
            handicapIndex.setHandicapPlayerId(ec.getCompetitionData().getCmpDataPlayerId());
            handicapIndex.setHandicapDate(ec.getCompetitionDescription().getCompetitionDate());
            handicapIndex= new find.FindHandicapIndexAtDate().find(handicapIndex, conn);
            ec.getCompetitionData().setCmpDataHandicap(handicapIndex.getHandicapWHS().doubleValue());
	liste.add(ec);
      } // end while
//     liste.forEach(item -> LOG.debug("CompetitionInscriptionsList = " + item.getCompetitionData().getCmpDataPlayerId()));
 // LOG.debug("after loop i = " + i);
    if (i == 0){
         String msg = "i == 0 , ££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         return liste = null;
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
       LOG.debug("escaped to " + methodName + "repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method
    public static List<ECompetition> getListe() {
        return liste;
    }
    public static void setListe(List<ECompetition> liste) {
        CompetitionInscriptionsList.liste = liste;
    }
    
 
@SuppressWarnings("unchecked")
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
      CompetitionDescription cd = new CompetitionDescription();
      cd.setCompetitionId(35);
      var lp = new CompetitionInscriptionsList().list(cd,conn);
        LOG.debug("from main, after lp = " + lp);
  // serialize c'est quoi ?
      FileOutputStream fos= new FileOutputStream("c:/log/inscriptionlist.ser");
      ObjectOutputStream oos= new ObjectOutputStream(fos);
      oos.writeObject(lp);
      oos.close();
      fos.close();
        LOG.debug("working !");
  // sera utilisée dans CompetitionStartList  !!
  // next lines : for testing only
     List<ECompetition> liec = new ArrayList<>();
     FileInputStream   fileIn = new FileInputStream( "c:/log/inscriptionlist.ser" );
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