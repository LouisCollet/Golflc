package SmartCard;

import be.belgium.eid.eidlib.BeID;
import be.belgium.eid.exceptions.EIDException;
import entite.CardBelgium;
import entite.Player;
import utils.LCUtil;

public class HandleSmartCard implements interfaces.Log {
    
  //  private static Player player = null;
    
    public String formatPlayer() // throws javax.smartcardio.CardException // enlevé 03-12-2017 car bloquait le fonctionnement de Wildfly 11 !!!!!
            // Not generating any bean definitions from lc.golfnew.CourseController because of underlying class loading error:
            //Type javax.smartcardio.CardException
 {
    try {
            LOG.info("starting registereIDPlayer ... "); // from course controller
            BeID eID = new BeID(true); // true : We allow information to be fetched from test cards
                LOG.info("debug 01");
            eID.connect(); // not necessary : implicit execution
                LOG.info("after connect eID " );
                
            SmartCard.FilleIDcardPlayer fip = new SmartCard.FilleIDcardPlayer();
            CardBelgium c = fip.formatPlayer(eID);
            
            Player player = null;
                LOG.info("CardBelgium = " + c.toString());
                
    // ici les move de CardBelgium vers Player  !!
                player.setPlayerFirstName(c.getFirstname1() );
                player.setPlayerLastName(c.getName() );
                player.setPlayerCity(c.getCity() );
            //    player.setPlayerZoneId(c.getCity() );
                player.setPlayerCountry(c.getCountry() );
                player.setPlayerBirthDate(c.getBirthDate() );
                player.setPlayerGender(c.getSex());
                player.seteID(Boolean.TRUE); // new player enregistré via eID
                player.setPlayerPhotoLocation(c.getPhotoLocation() );
                    LOG.info(" eIDplayer filled by eid = " + player.toString() );
            return "player.xhtml?faces-redirect=true";
        } catch (EIDException ie) {
            String msg = "EIDException in registereIDPlayer = " + ie.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(ie.getMessage());
            return null; // indicates that the same view should be redisplayed
//        } catch (CardException ie) {
//            String msg = "Â£ CardException in registereIDPlayer = " + ie.getMessage();
//            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
//            return null; // indicates that the same view should be redisplayed
////          String msg = "££ Exception in registereIDPlayer = " + e.getMessage();
   //         LOG.error(msg);
    //        LCUtil.showMessageFatal(msg);
  //          return null; // indicates that the same view should be redisplayed
        } finally {

        }
    } //end method
    
    public boolean isSmartcardPlugged() throws Exception
  {

    return false;
  } //end method

} //end class