package lists;

import entite.CompetitionData;
import entite.CompetitionDescription;
import entite.CompetitionDescription.StatusExecution;
import entite.Course;
import entite.composite.ECompetition;
import entite.PlayingHandicap;
import entite.Tee;
import static interfaces.Log.LOG;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import jakarta.validation.constraints.NotNull;
import utils.DBConnection;
import utils.LCUtil;

public class CompetitionStartList implements interfaces.Log{
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 private static List<ECompetition> liste = null;

public List<ECompetition> list(List<ECompetition> listeInscriptions , final @NotNull Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
 //      LOG.debug("entering CompetitionStartList with liste = " + liste);
   if(liste == null){
     try{
        LOG.debug(" ... entering CompetitionStartList " + methodName);
      CompetitionDescription cde = listeInscriptions.get(0).getCompetitionDescription();
        LOG.debug(" ... for competition Description " + cde);
        LOG.debug(" ... status = " + cde.getCompetitionStatus());
        LOG.debug(" ... execution, vérifier avec ligne suivante = " + cde.getCompetitionExecution());
        String execution = listeInscriptions.get(0).getCompetitionDescription().getCompetitionExecution();
   //     LOG.debug(" ... execution type param = " + execution);
        LOG.debug(" ... execution type description = " + execution);
 //       listeInscriptions.get(0).getCompetitionDescription().setCompetitionExecution(execution);
    //  if(competition.getCompetitionStatus().equals("1")){  // liste inscriptions est définitive
      if(Integer.parseInt(cde.getCompetitionStatus()) > 0){  // mod 31-10-2020
          cde.setCompetitionExecution(StatusExecution.PROVISIONAL.toString());
             LOG.debug("execution forced to PROVISIONAL");
      }  

      if(execution.equals(StatusExecution.PROVISIONAL.name())
          || execution.equals(StatusExecution.FINAL.toString())){
          LOG.debug("good execution type - PROVISIONAL or FINAL");
        }else{
            LOG.debug("wrong execution type = " + execution);
 //           liste = null;
            return null;
        }

    //    var li = new lists.CompetitionStartList().listHandle(liste, conn);
    LOG.debug("before listHandle");
        var li = new lists.CompetitionStartList().listSortAndComplete(listeInscriptions, conn);
    // tester résultat
        LOG.debug("after listHandle");
  //    if(execution.equals(StatusExecution.PROVISIONAL.toString())){
  ////          LOG.debug("provisional execution);");
   //         return li;
   //     }
 
      if(execution.equals(StatusExecution.FINAL.toString())){
            LOG.debug("this is a final execution !");
     //       var li = new lists.CompetitionStartList().listExecutionProvisional(liste, conn);
            var b = new lists.CompetitionStartList().modifyCompetition(li, conn);
        }
      
  li.forEach(item -> LOG.debug("Flight number " + item.getCompetitionData().getCmpDataFlightNumber() + 
  " - Flight start time " + item.getCompetitionData().getCmpDataFlightStart()));
 // liste.forEach(item -> LOG.debug("Flight start time " + item.getCompetitionData().getCmpDataFlightStart()));
  return li;
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
}finally{   }
}else{ // list != null
    LOG.debug("escaped to " + methodName + " repetition, thanks to lazy loading !");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECompetition> getListe() {
        return liste;
    }
   public static void setListe(List<ECompetition> liste) {
       CompetitionStartList.liste = liste;
   }

   public List<ECompetition> listSortAndComplete(List<ECompetition> li , final @NotNull Connection conn) throws SQLException{
       // complete data and sort
       
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
 //   LOG.debug("entering CompetitionStartList with liste = " + liste );
 // execution : on génère une liste mais PAS de update database
   try{
        LOG.debug(" ... entering " + methodName);
      CompetitionDescription cd = li.get(0).getCompetitionDescription();
        LOG.debug(" ... for competition Description" + cd);
      String exec = cd.getCompetitionExecution();
        LOG.debug(" ... for listSortAndComplete execution type  " + exec);
   //     LOG.debug(" ... for execution type description " + );
        LOG.debug(" ... Status = " + cd.getCompetitionStatus());
        
      li.forEach(item -> LOG.debug("Liste before tri " + item.getCompetitionData().getCmpDataPlayerId() + " /" + item.getCompetitionData().getCmpDataAskedStartTime())); 
         Collections.sort(li, Comparator
            .comparing    ((ECompetition p) -> p.getCompetitionData().getCmpDataAskedStartTime())
            .thenComparing(Comparator.comparingDouble((ECompetition p) -> p.getCompetitionData().getCmpDataHandicap()).reversed()) // plus petit handicap
            .thenComparing((ECompetition p) -> p.getCompetitionData().getCmpDataId()) // à égalité priorité au premier inscrit
         );
       li.forEach(item -> LOG.debug("Liste after tri  " + item.getCompetitionData().getCmpDataPlayerId() + " / " + item.getCompetitionData().getCmpDataAskedStartTime())); 
// 3) compléter les données
// LOG.debug("line 00");
  //  3a) chercher liste des tees pour le course
   Course course = new Course();
   course.setIdcourse(cd.getCompetitionCourseId());
   List<Tee> tees = new lists.TeesCourseList().list(course, conn);
      LOG.debug("line 01 - tee size = " + tees.size());
      
 // 3b trouver slope, rating, holes en fonction de gender et 
   LocalDateTime ldt = cd.getCompetitionDate(); 
   LocalTime lt = ldt.toLocalTime().minusMinutes(12);  // workaround pour facilité loop!
   int playersFlight = cd.getFlightNumberPlayers();
      LOG.debug("nombre de joueurs par flight = " + playersFlight);
   int flight = 0;
// attention : ne fonctionne pas pour les flights de un joueur !
// kernel : mise à jour de la liste !!!

 for (int i = 0; i < li.size(); i++) {
     LOG.debug("i = " + i);
 //     LOG.debug("i remainder = " + (i+1) % playersFlight);
      if((i+1) % playersFlight == 1){  // on a atteint le nombre de players par flight !!
          flight++;
          lt = lt.plusMinutes(12);
          LOG.debug("flight is now = " + flight);
      }
  // pourquoi ce doublon pas possible de réutiliser l'existant ? ??

 //    PlayingHandicap playingHandicap = null;
  //   if(exec.equals("FINAL")){
  //       playingHandicap = new CompetitionStartList().playingHandicap(li.get(i), tees);
  //          LOG.debug("playing handicap = " + playingHandicap);
   //  }else{
//
//     }
      
      /// handling current element
      
      var cda = li.get(i).getCompetitionData();
         LOG.debug("cda handled = " + cda.toString() + " for i = " + i);
      cda.setCmpDataFlightStart(lt);
      cda.setCmpDataFlightNumber((short)flight);
      // pourquoi pas plus tard en phase 2 (rounds) ou 3 (inscription) car il peut avoir changé entretemps ...
  ///    cda.setCmpDataPlayingHandicap((short)playingHandicap.getPlayingHandicap());
      // pourquoi pas le hcp ?
    // next line = set !!
      li.get(i).setCompetitionData(cda); // on remplace les valeurs de la liste avec set
         LOG.debug("cda out - liste get(i)" + li.get(i).toString());
 } // end for i

  li.forEach(item -> LOG.debug("cda completed Flight number and start time" + item.getCompetitionData().getCmpDataFlightNumber() + 
  " - Flight start time " + item.getCompetitionData().getCmpDataFlightStart()));
 // liste.forEach(item -> LOG.debug("Flight start time " + item.getCompetitionData().getCmpDataFlightStart()));

  // ici prendre action  ???

  return li;
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
     }
//}else{
 //   LOG.debug("escaped to " + methodName + "repetition thanks to lazy loading");
 //   return liste;  //plusieurs fois ??
//}
} //end method
   
  public boolean modifyCompetition (List<ECompetition> ec ,final @NotNull Connection conn) throws SQLException, Exception{
    //  public List<ECompetition> modifyStatus (List<ECompetition> liste ,final @NotNull Connection conn) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
 // execution : on fait l'update database
   try{
           LOG.debug(" ... entering " + methodName);

      // loop on competitionData update table competition_data
          for(int i=0; i < ec.size() ; i++){
             CompetitionData cda = ec.get(i).getCompetitionData();
             var v = ec.get(i).getCompetitionData();
             cda.setCmpDataPlayingHandicap(v.getCmpDataPlayingHandicap());
             cda.setCmpDataHandicap(v.getCmpDataHandicap());
             cda.setCmpDataFlightStart(v.getCmpDataFlightStart());
             cda.setCmpDataFlightNumber(v.getCmpDataFlightNumber());
             ec.get(i).setCompetitionData(cda); // on remplace les valeurs de la liste avec set
                if(new update.UpdateCompetitionData().update(ec.get(i).getCompetitionData(), conn)){
                    LOG.debug("competitionData is updated ! for i = " + i);
                    LOG.debug("for cda = " + ec.get(i).getCompetitionData());
     //               return true;
                }else{
                    String msg = "ModifyCompetitionData is NOT updated !for i = " + i;
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
             } // end loop update CompetitionData
       return true;
      /* update description
         CompetitionDescription cde = ec.get(0).getCompetitionDescription();
           LOG.debug(" ... for competition Description " + ec);
           LOG.debug(" ... status was = " + cde.getCompetitionStatus());
  
         cde.setCompetitionStatus("1");
           LOG.debug("we go to update description with status = " + cde.getCompetitionStatus());
         if(new update.UpdateCompetitionDescription().update(cde, conn)){
                LOG.debug("OK result of modify Competition Description");
             return true;
         }else{
                LOG.debug("KO KO  result of modify Competition Description");
             return false;
         }
*/
 }catch (Exception ex){
    String error = "Exception in " + methodName + " / " + ex;
    LOG.error(error);
    LCUtil.showMessageFatal(error);
    return false;
   }
//   return true;
} // end method
   
 public PlayingHandicap playingHandicap(final ECompetition competition, final List<Tee> tees) throws Exception {
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
 try {
      LOG.debug("starting " + methodName);
      LOG.debug("starting with tees = " + tees.toString()); //Arrays.deepToString(tees));
      LOG.debug("starting with seriesHandicap = " + Arrays.deepToString(competition.getCompetitionDescription().getSeriesHandicap()));
      LOG.debug("limit white = " + competition.getCompetitionDescription().getSeriesHandicap()[0][1]);

   String playerGender = competition.getCompetitionData().getCmpDataPlayerGender();
      LOG.debug("playerGender = " + playerGender); // est null !
   /// provisoirement

   String teeStart = "YELLOW"; // à modifier ultérieurement
    //    LOG.debug("new approach HandicapLimits = " 
    //        + Arrays.deepToString(competition.getCompetitionDescription().getSeriesHandicap()));
       LOG.debug("forced TeeStart = " + teeStart);
  
 /*
   List<Tee> selectedTee = tees.stream()
       .filter((Tee p) -> playerGender.equals(p.getTeeGender()) && teeStart.equals(p.getTeeStart()))
       .collect(Collectors.toList());
   selectedTee.forEach(item -> LOG.debug("Selected Tee = " + item));
   LOG.debug("line 04");
   */
   PlayingHandicap plh = new PlayingHandicap();
 // essayer https://www.oracle.com/technical-resources/articles/java/ma14-java-se-8-streams.html findAny
     Optional<Tee> tee = tees
             .stream()
             .filter((Tee p) -> playerGender.equals(p.getTeeGender()) && teeStart.equals(p.getTeeStart()))
             .findFirst();
   LOG.debug("line 05");
     if(tee.isPresent()) { 
            LOG.debug("is Present = " + tee.get()); // print
        }else{ 
            LOG.error("selectedTee : no value found");
            return plh; // est null
        } 
      LOG.debug("selectedTee = " + tee.get());
      
    double exact_hcp = competition.getCompetitionData().getCmpDataHandicap();
        LOG.debug("exact handicap = " + exact_hcp);
        plh.setHandicapPlayerEGA(exact_hcp);
    double slope = tee.get().getTeeSlope();
        LOG.debug("slope = " + slope);
        plh.setTeeSlope((int)slope);
    double rating = tee.get().getTeeRating().doubleValue(); //turn the BigDecimal object into a double
        LOG.debug("rating = " + rating);
        plh.setTeeRating(rating);
    double par = tee.get().getTeePar();
        LOG.debug("par = " + par);
        plh.setCoursePar((int)par);
    int category = 0;   // de 1 à 5 soit 1-5
  //  int nholes = round.getRoundHoles();
    int nholes = 18;
        LOG.debug("forced holes = " + nholes);
        plh.setRoundHoles((short)nholes);
    int playing_hcp = 0;
//        LOG.debug("tee start = " + tee.getTeeStart());
 //       LOG.debug("tee holes played = " + tee.getTeeHolesPlayed());
 //       LOG.debug("tee idtee = " + tee.getIdtee());
 //       LOG.debug("tee MasterTee = " + tee.getTeeMasterTee());
 //   if(exact_hcp == 54) //new 05/07/2016
    if(exact_hcp > 36 && exact_hcp < 55){ // new 01/05/2019 Club Handicap
         LOG.debug("Player category 6 - Club Handicap");
         category = 6;
    }else{
          LOG.debug("Player category 1-5");
          category = 15;
    }
  //  LOG.debug("clubhandicap = " + clubhandicap);
  if(category == 15 ){ // catégories de 1 à 5
      LOG.debug("calculating playing hcp for categories 1 to 5 = "); // + playing_hcp);
      
    if(nholes == 18){
        playing_hcp = (int) Math.round( (exact_hcp * (slope/113.0) ) + (rating-par) );
        LOG.debug("calculated new playing hcp for categories 1 to 5, 18 holes = "); // + playing_hcp);
    }else{ // 9 holes mod 30-08-2020
        playing_hcp = (int) Math.round( (exact_hcp*(slope/113.0))/2 + ((rating/2) - par) );
        // si on connait les infos 10-18 par exemple
        
 //       LOG.debug("old method calculated new playing hcp for categories 1 to 5, 9 holes = "); // + playing_hcp);
   //     playing_hcp = (int) Math.round( (exact_hcp*(slope/113.0))/2 + ((rating - par) /2));
        // si on divise par deux le résultat 18
        LOG.debug("new calcul method 9 holes = " + playing_hcp);
    }
  }
  plh.setPlayingHandicap(playing_hcp);
  return plh;
  //  return playing_hcp.shortValue();
  } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        }
    } //end method 

 @SuppressWarnings("unchecked")
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
      // list ECompetition à envoyer !
  // deserialize
  //   List<ECompetition> liec = new ArrayList<>();
     FileInputStream fileIn = new FileInputStream( "c:/log/inscriptionlist.ser" );
     ObjectInputStream in = new ObjectInputStream(fileIn);
     List<ECompetition> liec = (List<ECompetition>) in.readObject();
     fileIn.close();
     in.close();
// compléter executiontype !
      var lp = new CompetitionStartList().list(liec, conn); // proposition mod by netbeans
  //      LOG.debug("from main, after lp = " + lp);
  // serialize", 
      FileOutputStream fos= new FileOutputStream("c:/log/startlist.ser");
      ObjectOutputStream oos= new ObjectOutputStream(fos);
      oos.writeObject(lp);
      oos.close();
      fos.close();
        LOG.debug("working !");
        LOG.debug("printing list liec = " + Arrays.deepToString(lp.toArray()));

 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class