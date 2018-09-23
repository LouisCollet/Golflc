
package SmartCard;

import be.belgium.eid.eidlib.BeID;
import be.belgium.eid.exceptions.EIDException;
import be.belgium.eid.objects.IDAddress;
import be.belgium.eid.objects.IDData;
import be.belgium.eid.objects.IDPhoto;
import entite.CardBelgium;
import java.io.Serializable;
import lc.golfnew.Constants;
import utils.LCUtil;

public class FilleIDcardPlayer implements Serializable, interfaces.Log, interfaces.GolfInterface
{
  public CardBelgium formatPlayer(final BeID eID)// throws EIDException, UnsupportedEncodingException, IOException        
 {
  //   CardBelgium card = new CardBelgium();
    try {
        LOG.info("starting registereIDPlayer with : ");
            // We fetch the information (see also InformationRetrieval)
                LOG.info(" -- eID information = " + System.getProperty("file.encoding"));
            IDData e = eID.getIDData();
                LOG.info(" -- IDData retrieved = " + NEW_LINE + e.toString() );
            CardBelgium card = new CardBelgium();

            card.setCardNumber(e.getCardNumber());
                LOG.info(" -- eID card number = " + card.getCardNumber());

            card.setNationalNumber(e.getNationalNumber());
                LOG.info(" -- eID national number = " + e.getNationalNumber());

            card.setFirstname1(e.get1stFirstname());
                LOG.info(" -- eID player_first = " + e.get1stFirstname());
            byte[] b = card.getFirstname1().getBytes("Windows-1252");  // au départ
            card.setFirstname1(new String(b, "utf-8") ); // à  l'arrivée
                LOG.info(" -- player_first encoded  = " + card.getFirstname1() );

            card.setName(e.getName());
            LOG.info(" -- eID player_last = " + card.getName() );

            card.setBirthDate(e.getBirthDate());
                LOG.info(" -- eID birth_date = " + card.getBirthDate() );

            char c = e.getSex();
            String player_gender = Character.toString(c);
            if ("F".equals(player_gender) || "V".equals(player_gender)) // français
            {
                player_gender = "L";
            }  // from Ladies
            //   if ("V".equals(player_gender)) //nederlands 15/09/2013
            //           {player_gender = "L";}  // from Ladies

                LOG.info(" -- eID player_gender = " + player_gender);
            card.setSex(player_gender);

            String player_city = e.getMunicipality();
                LOG.info(" -- eID player_city = " + player_city);

            IDAddress a = eID.getIDAddress();
            String player_zip = a.getZipCode();
                LOG.info(" -- player_zip = " + player_zip);
            player_city = "B - " + player_zip + " " + player_city;
                LOG.info(" -- player_city = " + player_city);
            card.setCity(player_city);
            
            card.setCountry("BE");
            
            String ap = Constants.photos_library;
                LOG.info("ap images_library = " + ap);
            String name = card.getCardNumber();
            IDPhoto p = eID.getIDPhoto();
                LOG.info("eID photo name = " + name);
            p.writeToFile(ap + name); // .jpeg is autom appended
                LOG.info("photo file written = " + ap + name + ".jpeg");

            card.setPhotoLocation(name + ".jpeg");
            
        return card;
//   } catch (CardNotFoundException ie) {
//            String msg = "Â£Â£Â£ CardBelgium not found Exception = " + ie.getMessage();
//            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
//            return null; // indicates that the same view should be redisplayed     
   } catch (EIDException ie) {
            String msg = "£££ EIDException in formatPlayer = " + ie.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
//          String msg = "£££ NumberFormatException in formatPlayer = " + nfe.getMessage();
  //          LOG.error(msg);
    //        LCUtil.showMessageFatal(msg);
      //      return null;
   } catch (Exception ie) {
            String msg = "£££ Exception in formatPlayer = " + ie.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
   } finally {
        }

} //end formatplayer
} //end Class