package mail;

import entite.Club;
import entite.Cotisation;
import entite.Creditcard;
import entite.EquipmentsAndBasic;
import entite.EquipmentsAndBasicAndRange;
import entite.Greenfee;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.Subscription;
import entite.TarifGreenfee;
import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.io.Serializable;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreditcardMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private MailSender mailSender;
    @Inject private entite.Settings settings;

    public CreditcardMail() { }

    public Boolean sendMailGreenfee(Player player, Creditcard creditcard, TarifGreenfee tarif, Round round,
            Inscription inscription) throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String dateStr = round.getRoundDate() != null ? round.getRoundDate().format(ZDF_TIME_DAY) : "?";
            String holes   = round.getRoundHoles() != null ? round.getRoundHoles() + " trous" : "";
            String game    = round.getRoundGame()  != null ? " — " + round.getRoundGame() : "";

            StringBuilder lignes = new StringBuilder();
            if (tarif.getWorkItem() != null) {
                lignes.append("<tr><td>").append(tarif.getWorkItem()).append("</td>")
                      .append("<td align='right'>")
                      .append(String.format("%.2f €", tarif.getPriceGreenfee()))
                      .append("</td></tr>");
            }
            if (tarif.getEquipmentChoosen() != null) {
                for (entite.EquipmentsAndBasic e : tarif.getEquipmentChoosen()) {
                    if (e.getPrice() == null || e.getQuantity() == null || e.getQuantity() <= 0) continue;
                    lignes.append("<tr><td>").append(e.getItem()).append("</td>")
                          .append("<td align='right'>")
                          .append(String.format("%.2f €", e.getPrice() * e.getQuantity()))
                          .append("</td></tr>");
                }
            }

            String mail = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>⛳ Confirmation de paiement greenfee — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p><b>" + player.getPlayerFirstName() + " " + player.getPlayerLastName() + "</b></p>"
                + "<p>Date : <b>" + dateStr + "</b> — " + holes + game + "</p>"
                + "<hr/>"
                + "<table style='min-width:320px;border-collapse:collapse'>"
                + lignes
                + "<tr><td colspan='2'><hr/></td></tr>"
                + "<tr><td><b>Total</b></td>"
                + "<td align='right'><b>" + String.format("%.2f €", creditcard.getTotalPrice()) + "</b></td></tr>"
                + "</table>"
                + "<hr/>"
                + "<p>" + creditcard.getCreditcardIssuer() + " " + creditcard.getCreditCardNumberSecret() + "</p>"
                + "<p>Référence : " + creditcard.getCreditcardPaymentReference() + "</p>"
                + "<br/><p>Merci !<br/>L'équipe GolfLC</p>"
                + "</body></html>";

            String to = settings.getProperty("SMTP_USERNAME");
            mailSender.sendHtmlMailAsync("⛳ Confirmation greenfee — GolfLC", mail, to, null, player.getPlayerLanguage());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendMailSubscription(Player player, Creditcard creditcard, Subscription subscription)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String startStr = subscription.getStartDate() != null ? subscription.getStartDate().format(ZDF_TIME_DAY) : "?";
            String endStr   = subscription.getEndDate()   != null ? subscription.getEndDate().format(ZDF_TIME_DAY)   : "?";
            String desc     = subscription.getCommunication() != null ? subscription.getCommunication() : subscription.getSubCode();

            String mail = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>✅ Confirmation de paiement abonnement — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p><b>" + player.getPlayerFirstName() + " " + player.getPlayerLastName() + "</b></p>"
                + "<p>Période : <b>" + startStr + " → " + endStr + "</b></p>"
                + "<hr/>"
                + "<table style='min-width:320px;border-collapse:collapse'>"
                + "<tr><td>" + desc + "</td>"
                + "<td align='right'><b>" + String.format("%.2f €", creditcard.getTotalPrice()) + "</b></td></tr>"
                + "</table>"
                + "<hr/>"
                + "<p>" + creditcard.getCreditcardIssuer() + " " + creditcard.getCreditCardNumberSecret() + "</p>"
                + "<p>Référence : " + creditcard.getCreditcardPaymentReference() + "</p>"
                + "<br/><p>Merci !<br/>L'équipe GolfLC</p>"
                + "</body></html>";

            String to = settings.getProperty("SMTP_USERNAME");
            mailSender.sendHtmlMailAsync("✅ Confirmation abonnement — GolfLC", mail, to, null, player.getPlayerLanguage());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendMailCotisation(Player player, Creditcard creditcard, Cotisation cotisation,
            Club club, TarifMember tarifMember) throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String sujet = "✅ Confirmation de cotisation — " + club.getClubName();

            String startStr = cotisation.getCotisationStartDate() != null
                    ? cotisation.getCotisationStartDate().format(ZDF_TIME_DAY) : "?";
            String endStr   = cotisation.getCotisationEndDate()   != null
                    ? cotisation.getCotisationEndDate().format(ZDF_TIME_DAY)   : "?";

            // Détail des lignes (cotisation de base + équipements sélectionnés)
            StringBuilder lignes = new StringBuilder();
            if (tarifMember != null) {
                for (EquipmentsAndBasicAndRange b : tarifMember.getBasicList()) {
                    if (b.getPrice() == null || b.getQuantity() == null || b.getQuantity() <= 0) continue;
                    lignes.append("<tr><td>").append(b.getItem()).append("</td>")
                          .append("<td align='right'>")
                          .append(String.format("%.2f €", b.getPrice() * b.getQuantity()))
                          .append("</td></tr>");
                }
                for (EquipmentsAndBasic e : tarifMember.getEquipmentsList()) {
                    if (e.getPrice() == null || e.getQuantity() == null || e.getQuantity() <= 0) continue;
                    lignes.append("<tr><td>").append(e.getItem()).append("</td>")
                          .append("<td align='right'>")
                          .append(String.format("%.2f €", e.getPrice() * e.getQuantity()))
                          .append("</td></tr>");
                }
            }

            String mail = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>✅ Confirmation de paiement — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p><b>" + player.getPlayerFirstName() + " " + player.getPlayerLastName() + "</b><br/>"
                + club.getClubName() + " — " + club.getAddress().getCity() + "</p>"
                + "<p>Période : <b>" + startStr + " → " + endStr + "</b></p>"
                + "<hr/>"
                + "<table style='min-width:320px;border-collapse:collapse'>"
                + lignes
                + "<tr><td colspan='2'><hr/></td></tr>"
                + "<tr><td><b>Total</b></td>"
                + "<td align='right'><b>" + String.format("%.2f €", creditcard.getTotalPrice()) + "</b></td></tr>"
                + "</table>"
                + "<hr/>"
                + "<p>" + creditcard.getCreditcardIssuer() + " " + creditcard.getCreditCardNumberSecret() + "</p>"
                + "<p>Référence : " + creditcard.getCreditcardPaymentReference() + "</p>"
                + "<br/><p>Merci !<br/>L'équipe GolfLC</p>"
                + "</body></html>";

            String to = settings.getProperty("SMTP_USERNAME");
            mailSender.sendHtmlMailAsync(sujet, mail, to, null, player.getPlayerLanguage());
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public Boolean sendMailGreenfee(Player player, Creditcard creditcard, Greenfee greenfee, Club club)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String dateStr = greenfee.getRoundDate() != null ? greenfee.getRoundDate().format(ZDF_TIME_DAY) : "?";
            String holes   = greenfee.getRoundHoles() != null ? greenfee.getRoundHoles() + " trous" : "";

            StringBuilder lignes = new StringBuilder();
            lignes.append("<tr><td>Greenfee").append(holes.isEmpty() ? "" : " — " + holes).append("</td>")
                  .append("<td align='right'>")
                  .append(String.format("%.2f €", greenfee.getPrice()))
                  .append("</td></tr>");
            if (greenfee.getItems() != null && !greenfee.getItems().isBlank()) {
                lignes.append("<tr><td colspan='2' style='font-size:0.9em;color:#555'>")
                      .append(greenfee.getItems()).append("</td></tr>");
            }

            String mail = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>⛳ Confirmation de paiement greenfee — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p><b>" + player.getPlayerFirstName() + " " + player.getPlayerLastName() + "</b><br/>"
                + club.getClubName() + " — " + club.getAddress().getCity() + "</p>"
                + "<p>Date : <b>" + dateStr + "</b></p>"
                + "<hr/>"
                + "<table style='min-width:320px;border-collapse:collapse'>"
                + lignes
                + "<tr><td colspan='2'><hr/></td></tr>"
                + "<tr><td><b>Total</b></td>"
                + "<td align='right'><b>" + String.format("%.2f €", creditcard.getTotalPrice()) + "</b></td></tr>"
                + "</table>"
                + "<hr/>"
                + "<p>" + creditcard.getCreditcardIssuer() + " " + creditcard.getCreditCardNumberSecret() + "</p>"
                + "<p>Référence : " + creditcard.getCreditcardPaymentReference() + "</p>"
                + "<br/><p>Merci !<br/>L'équipe GolfLC</p>"
                + "</body></html>";

            String to = settings.getProperty("SMTP_USERNAME");
            mailSender.sendHtmlMailAsync("⛳ Confirmation greenfee — " + club.getClubName(), mail, to, null, player.getPlayerLanguage());
            LOG.debug("HTML Mail async dispatched");
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

/*
void main() throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    // tests locaux
} // end main
*/

} // end class
