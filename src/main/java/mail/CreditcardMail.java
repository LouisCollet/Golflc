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
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import java.util.Collections;
import java.util.List;
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

            String cs = currSymbol(creditcard);
            StringBuilder lignes = new StringBuilder();
            if (tarif.getWorkItem() != null) {
                lignes.append("<tr><td>").append(tarif.getWorkItem()).append("</td>")
                      .append("<td align='right'>")
                      .append(String.format("%.2f %s", tarif.getPriceGreenfee(), cs))
                      .append("</td></tr>");
            }
            if (tarif.getEquipmentChoosen() != null) {
                for (entite.EquipmentsAndBasic e : tarif.getEquipmentChoosen()) {
                    if (e.getPrice() == null || e.getQuantity() == null || e.getQuantity() <= 0) continue;
                    lignes.append("<tr><td>").append(e.getItem()).append("</td>")
                          .append("<td align='right'>")
                          .append(String.format("%.2f %s", e.getPrice() * e.getQuantity(), cs))
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
                + "<td align='right'><b>" + String.format("%.2f %s", creditcard.getTotalPrice(), cs) + "</b></td></tr>"
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

            String cs = currSymbol(creditcard);
            String mail = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>✅ Confirmation de paiement abonnement — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p><b>" + player.getPlayerFirstName() + " " + player.getPlayerLastName() + "</b></p>"
                + "<p>Période : <b>" + startStr + " → " + endStr + "</b></p>"
                + "<hr/>"
                + "<table style='min-width:320px;border-collapse:collapse'>"
                + "<tr><td>" + desc + "</td>"
                + "<td align='right'><b>" + String.format("%.2f %s", creditcard.getTotalPrice(), cs) + "</b></td></tr>"
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

            String cs = currSymbol(creditcard);
            StringBuilder lignes = new StringBuilder();
            if (tarifMember != null) {
                for (EquipmentsAndBasicAndRange b : tarifMember.getBasicList()) {
                    if (b.getPrice() == null || b.getQuantity() == null || b.getQuantity() <= 0) continue;
                    String label = b.getQuantity() > 1
                        ? b.getItem() + " (" + b.getQuantity() + " × " + String.format("%.2f %s", b.getPrice(), cs) + ")"
                        : b.getItem();
                    lignes.append("<tr><td>").append(label).append("</td>")
                          .append("<td align='right'>")
                          .append(String.format("%.2f %s", b.getPrice() * b.getQuantity(), cs))
                          .append("</td></tr>");
                }
                for (EquipmentsAndBasic e : tarifMember.getEquipmentsList()) {
                    if (e.getPrice() == null || e.getQuantity() == null || e.getQuantity() <= 0) continue;
                    String label = e.getQuantity() > 1
                        ? e.getItem() + " (" + e.getQuantity() + " × " + String.format("%.2f %s", e.getPrice(), cs) + ")"
                        : e.getItem();
                    lignes.append("<tr><td>").append(label).append("</td>")
                          .append("<td align='right'>")
                          .append(String.format("%.2f %s", e.getPrice() * e.getQuantity(), cs))
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
                + "<td align='right'><b>" + String.format("%.2f %s", creditcard.getTotalPrice(), cs) + "</b></td></tr>"
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
        return sendMailGreenfee(player, creditcard, Collections.singletonList(greenfee), club);
    } // end method

    public Boolean sendMailGreenfee(Player player, Creditcard creditcard, List<Greenfee> greenfees, Club club)
            throws MessagingException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String cs = currSymbol(creditcard);
            double greenfeeTotal = greenfees.stream().mapToDouble(Greenfee::getPrice).sum();
            StringBuilder lignes = new StringBuilder();
            for (int i = 0; i < greenfees.size(); i++) {
                Greenfee gf = greenfees.get(i);
                if (i > 0) {
                    lignes.append("<tr><td colspan='2'><hr style='border:none;border-top:1px dashed #ccc;margin:4px 0'/></td></tr>");
                }
                String dateStr = gf.getRoundDate() != null ? gf.getRoundDate().format(ZDF_TIME_HHmm) : "?";
                String holes   = gf.getRoundHoles()  != null ? " — " + gf.getRoundHoles() + " trous" : "";
                lignes.append("<tr><td colspan='2' style='padding-top:4px;font-size:0.9em;color:#777'>Date : <b>")
                      .append(dateStr).append("</b></td></tr>");
                lignes.append("<tr><td>Greenfee").append(holes).append("</td>")
                      .append("<td align='right'>").append(String.format("%.2f %s", gf.getPrice(), cs)).append("</td></tr>");
                if (gf.getItems() != null && !gf.getItems().isBlank()) {
                    lignes.append("<tr><td colspan='2' style='font-size:0.9em;color:#555'>")
                          .append(gf.getItems()).append("</td></tr>");
                }
            }

            String mail = "<html><body style='font-family:Arial,sans-serif;max-width:600px'>"
                + "<h2>⛳ Confirmation de paiement greenfee — GolfLC</h2>"
                + "<p>" + LocalDateTime.now().format(ZDF_TIME) + "</p>"
                + "<p><b>" + player.getPlayerFirstName() + " " + player.getPlayerLastName() + "</b><br/>"
                + club.getClubName() + " — " + club.getAddress().getCity() + "</p>"
                + "<hr/>"
                + "<table style='min-width:320px;border-collapse:collapse'>"
                + lignes
                + "<tr><td colspan='2'><hr/></td></tr>"
                + "<tr><td><b>Total</b></td>"
                + "<td align='right'><b>" + String.format("%.2f %s", greenfeeTotal, cs) + "</b></td></tr>"
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

    private String currSymbol(Creditcard creditcard) {
        try {
            String code = creditcard.getCreditcardCurrency();
            if (code != null && !code.isBlank()) {
                return java.util.Currency.getInstance(code).getSymbol();
            }
        } catch (IllegalArgumentException ignored) { }
        return "€";
    } // end method

/*
void main() throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    // tests locaux
} // end main
*/

} // end class
