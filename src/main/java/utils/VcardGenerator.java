package utils;

// import connection_package.DBConnection; // removed 2026-02-26 — CDI migration
import Controllers.LanguageController;
import entite.Player;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
// import java.sql.Connection; // removed 2026-02-26
// import java.sql.SQLException; // removed 2026-02-26
import java.util.ArrayList;
import java.util.List;

import manager.PlayerManager;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.util.MapTimeZoneCache;
import net.fortuna.ical4j.vcard.Group;
//import net.fortuna.ical4j.vcard.Property;

@RequestScoped
public class VcardGenerator {
  //   invalidated on 12-11-2024 nouvelle version 2.0.0
//     @SuppressWarnings("deprecation") à remplacer par @Deprecated ??
 @Inject
    private PlayerManager playerManager;
// public static Path create(Player player){
    public static Path create(Player player){

//    try{
      LOG.debug("entering VcardGenerator.create");
    System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());

//    System.setProperty("-Dical4j.validation.relaxed","true");
    
  //  https://www.javatips.net/api/net.fortuna.ical4j.vcard.vcard
  
    LOG.debug("player = " + player); 

    List<Parameter> parameter = new ArrayList<>();
//    parameter.add(Language.LANGUAGE);
   // List<Property> property = new ArrayList<>();
    
//    https://github.com/ical4j/ical4j-vcard/blob/develop/src/main/java/net/fortuna/ical4j/vcard/parameter/Label.java
  //  props.add(new N(player.getPlayerLastName(), player.getPlayerFirstName(), null, null, null));
  // ajouter une Locale
//  Locale locale = LanguageController.getLocale();
//  parameter.add(Language.Factory.classLANGUAGE ("fr"));
  /*
    props.add(new Gender("Male"));
    props.add(new Role("role " + player.getPlayerRole()));
    props.add(new Org("this is the Company"));
    props.add(new Title("this is the title"));
     Locale loc = new Locale(player.getPlayerLanguage(),player.getAddress().getCountry().getCode());
  //  loc.getDisplayCountry();
 //   props.add(new Address("pobox","extended", "street", player.getPlayerCity(),"region", "postcode", loc.getDisplayCountry()));
 //   props.add(new Address(null,null, null, player.getPlayerCity(),null, null, loc.getDisplayCountry()));
    props.add(new Address(Group.WORK,null,null, null, player.getAddress().getCity(),null, null, loc.getDisplayCountry(), Type.WORK));
    props.add(new Email(player.getPlayerEmail()));

   if (player.getPlayerBirthDate() != null) {
       // à corriger 13-04-2022
//        Date iCalDate = new Date(player.getPlayerBirthDate().getTime());
//        props.add(new BDay(iCalDate));
    }
   props.add(new Telephone("this is the tel number"));
//   props.add(new Telephone(Group.WORK,"this is the tel number", Type.WORK));
   props.add(new Version("version number"));
   props.add(new ProdId("ProdId - Business Manager"));
   props.add(new Fn("Fn -Business Manager"));
   props.add(new Note("This is the note"));
/* attention error :   5864 2021-10-10T14:46:44,856 23175114  DEBUG utils.VcardGenerator . create 90 :
   Could not write vcard to outputstream. Error was:  
net.fortuna.ical4j.validate.ValidationException: Property [FN] must be specified once
   ==< certaines fields sont OBLIGATOIRES : mais c'est pas très clair !!
 

   VCard vCard = new VCard(props);
   LOG.debug("vCard properties = \n" + vCard.getProperties().toString());
 //  for(Property property : vCard.getProperties()){
  //     PropertyId propertyId = property.getId();
 //      LOG.debug("property = " + propertyId.getPropertyName);


    Path temp = Files.createTempFile("Ical", ".vcf");
           LOG.debug("Ical Temp file : " + temp);
      try (
//    LOG.debug("line 11");
           FileOutputStream fout = new FileOutputStream(temp.toFile())
      ) {
          OutputStreamWriter writer = new OutputStreamWriter(fout, Charset.forName("UTF-8"));
          new VCardOutputter(true).output(vCard, writer);  // specifies whether to validate vCard objects prior to output
             LOG.debug("fout file : " + fout.toString());    
             
 // enlevé 10-10-2021         fout.flush();
   //   }
 
//    OutputStream out = new ByteArrayOutputStream();
 //   try {
 //       OutputStreamWriter writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
 //       VCardOutputter vco = new VCardOutputter();
  //      vco.output(vCard, writer);

    } catch (Exception e) {
        LOG.debug("Could not write vcard to outputstream. Error was: ", e);
    }
        LOG.debug("exiting vcardGenerator with temp Path  : " + temp);
return temp;

} catch (Exception e) {
            String msg = "£££ Exception in VcardGenerator = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
} //end method 
*/
return null; // fake
  }
 /*
 void main() {
     final String methodName = utils.LCUtil.getCurrentMethodName();
     LOG.debug("entering " + methodName);
     // requires CDI container — cannot run standalone
 } // end main
 */
} // end class
