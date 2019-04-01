package load;

import entite.Club;
import entite.Cotisation;
import entite.Player;
import entite.TarifMember;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.Arrays;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import static utils.LCUtil.showMessageFatal;

@Named
@SessionScoped
public class LoadCotisation implements Serializable{

public Cotisation load(TarifMember tarifMember, Cotisation cotisation, Club club, Player player) throws Exception{
try{
        LOG.info("entering LoadCotisation");
      cotisation.setStartDate(tarifMember.getMemberStartDate());
      cotisation.setEndDate(tarifMember.getMemberEndDate());
      cotisation.setIdclub(club.getIdclub());
      cotisation.setCommunication("Cotisation comme membre du club " + club.getClubName());
      double price = new calc.CalcTarifCotisation().findTarif(tarifMember, player);
      cotisation.setPrice(price);
      if(cotisation.getPrice() == 0.0){
            String msgerr = "Le total est zéro - il faut choisir au moins un item !!!";
            LOG.error(msgerr);
            showMessageFatal(msgerr);
   //         throw new Exception(msgerr);
         }
      // ici ajouter les données à enregistrer dans la table cotisation
  // créer la liste des items pour stockage DB
       StringBuilder sb = new StringBuilder("");
 //      cotisation.setStatus("N"); // utilisé par savoir facilement si le member est en ordre pour la période
       for(int i = 0 ; i < tarifMember.getMembersBase().length ; i++) {
    //        LOG.info("i = " + i);
    //        LOG.info("base for items = " + Arrays.toString(tarifMember.getMembersBase()[i]));
    //        LOG.info("choice for items = " + tarifMember.getMembersChoice()[i]);
            if(tarifMember.getMembersChoice()[i] > 0){ // item sélectionné
    //            LOG.info("item sélectionné = " + tarifMember.getMembersBase()[i][0]);
                sb.append(tarifMember.getMembersBase()[i][0]) // item
                  .append(" (")
                  .append(tarifMember.getMembersBase()[i][1]) //prix
                  .append("),");
     //           LOG.info("after append sb = " + sb.toString());
          
          //          if(! tarifMember.getMembersBase()[i][0].startsWith("A-")){ // item abonnement car Accessoires commencent par "A-"
                        LOG.info("Status set to Y for = " + Arrays.toString(tarifMember.getMembersBase()[i]));
                        cotisation.setStatus("Y"); // le player a payé sa cotisation et est abonné pour la période
           //         } //end if 2
              } //end if 2
    //        LOG.info("going back");
         } //end for
   //    LOG.info("after for loop");
     /// constitution items pour equipments
       for(int i = 0 ; i < tarifMember.getPriceEquipments().length ; i++) {
    //        LOG.info("i = " + i);
    //        LOG.info("base for items = " + Arrays.toString(tarifMember.getMembersBase()[i]));
    //        LOG.info("choice for items = " + tarifMember.getMembersChoice()[i]);
            if(tarifMember.getEquipmentsChoice()[i] > 0){ // item sélectionné
    //            LOG.info("item sélectionné = " + tarifMember.getMembersBase()[i][0]);
                sb.append(tarifMember.getPriceEquipments()[i][0]) // item
                  .append(" (")
                  .append(tarifMember.getPriceEquipments()[i][1]) //prix
                  .append("),");
     //           LOG.info("after append sb = " + sb.toString());
              } //end if 
         } //end for
   
       sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
            LOG.info("final selectedItems = " + sb);
      cotisation.setItems(sb.toString());

  return cotisation;
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
