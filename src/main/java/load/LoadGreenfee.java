package load;

import entite.Club;
import entite.Greenfee;
import entite.Round;
import entite.TarifGreenfee;
import static interfaces.Log.LOG;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class LoadGreenfee implements Serializable{

public Greenfee load(TarifGreenfee tarifGreenfee, Greenfee greenfee, Club club, Round round) throws Exception{
try{
        LOG.info("entering LoadGreenfee - load");
     double d = new calc.CalcPrixGreenfee().calc(tarifGreenfee);
        LOG.info("le prix du greenfee est " + d);
     greenfee.setPrice(d);
     greenfee.setCommunication("Greenfee parcours " + club.getClubName());
     greenfee.setIdclub(club.getIdclub());
     greenfee.setIdround(round.getIdround());
     greenfee.setRoundDate(round.getRoundDate());
     greenfee.setStatus("N"); // sera mis à Y ci-après si dans le paiement est compris un greenfee (et pas uniquement des equipements
     if(greenfee.getPrice() == 0.0){
            String msgerr = "Le total est zéro - il faut choisir au moins un item !!!";
            LOG.error(msgerr);
            utils.LCUtil.showMessageFatal(msgerr);
            throw new Exception(msgerr);
         }
  // créer la liste des items pour stockage DB
  // à faire ultérieurement : noter les quantités : 2 greenfees, 3 buggys, etc...
    StringBuilder sb = new StringBuilder("");
      if(tarifGreenfee.getInputtype().equals("DA")){
           LOG.info("inputtype = DA");
         greenfee.setStatus("Y");
           LOG.info("status changed to  = " + greenfee.getStatus());
         sb.append("Greenfee (")
         .append(tarifGreenfee.getPriceGreenfee())
         .append("),");
      }
/// constitution items pour greenfee
       for(int i = 0 ; i < tarifGreenfee.getPriceGreenfees().length ; i++) {
    //        LOG.info("i = " + i);
    //        LOG.info("base for items = " + Arrays.toString(tarifMember.getMembersBase()[i]));
    //        LOG.info("choice for items = " + tarifMember.getMembersChoice()[i]);
            if(tarifGreenfee.getGreenfeesChoice()[i] > 0){ // item sélectionné
    //            LOG.info("item sélectionné = " + tarifMember.getMembersBase()[i][0]);
                sb.append(tarifGreenfee.getPriceGreenfees()[i][0]) // item
                  .append(" (")
                  .append(tarifGreenfee.getPriceGreenfees()[i][2]) //prix
                  .append("),");
     //           LOG.info("after append sb = " + sb.toString());
                        greenfee.setStatus("Y"); // le player a payé son greenfee et pas uniquement des équipements
              } //end if
         } //end for
 //      sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
            LOG.info("final Items for greenfee = " + sb);
            
  /// constitution items pour equipments
       for(int i = 0 ; i < tarifGreenfee.getPriceEquipments().length ; i++) {
    //        LOG.info("i = " + i);
    //        LOG.info("base for items = " + Arrays.toString(tarifMember.getMembersBase()[i]));
    //        LOG.info("choice for items = " + tarifMember.getMembersChoice()[i]);
            if(tarifGreenfee.getEquipmentsChoice()[i] > 0){ // item sélectionné
    //            LOG.info("item sélectionné = " + tarifMember.getMembersBase()[i][0]);
                sb.append(tarifGreenfee.getPriceEquipments()[i][0]) // item
                  .append(" (")
                  .append(tarifGreenfee.getPriceEquipments()[i][1]) //prix
                  .append("),");
     //           LOG.info("after append sb = " + sb.toString());
              } //end if 
         } //end for
   //    LOG.info("after for loop");
       sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
            LOG.info("final Items for greenfee and equipments = " + sb);

      greenfee.setItems(sb.toString());
  return greenfee;
}catch (Exception ex){
    LOG.error("Exception in LoadCotisation ! " + ex);
    utils.LCUtil.showMessageFatal("Exception in LoadCotisation = " + ex.toString() );
    return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
  //  DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

} //end method

public static void main(String[] args) throws Exception // testing purposes
{
  //  DBConnection dbc = new DBConnection();
  //  Connection conn = dbc.getConnection();
  //  LoadClub lc = new LoadClub();
  //  Club club = lc.LoadClub(conn, 104);
  //     LOG.info(" club = " + club.toString());
//for (int x: par )
//        LOG.info(x + ",");
//DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class
