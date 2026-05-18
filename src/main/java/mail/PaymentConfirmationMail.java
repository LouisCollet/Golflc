package mail;

import entite.Club;
import entite.Cotisation;
import entite.Creditcard;
import entite.EquipmentsAndBasic;
import entite.EquipmentsAndBasicAndRange;
import entite.Greenfee;
import entite.Lesson;
import entite.Player;
import entite.Professional;
import entite.Subscription;
import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class PaymentConfirmationMail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private MailSender mailSender;
    @Inject private entite.Settings settings;

    public PaymentConfirmationMail() { }

    public Boolean send(Player player, Creditcard creditcard, Club club,
            List<Greenfee> greenfees, List<Lesson> lessons, Professional professional,
            Cotisation cotisation, TarifMember tarifMember,
            Subscription subscription) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String cs = currSymbol(creditcard);
            boolean hasGreenfee     = greenfees    != null && !greenfees.isEmpty();
            boolean hasLesson       = lessons      != null && !lessons.isEmpty();
            boolean hasCotisation   = cotisation   != null;
            boolean hasSubscription = subscription != null;
            int sectionCount = (hasGreenfee ? 1 : 0) + (hasLesson ? 1 : 0)
                             + (hasCotisation ? 1 : 0) + (hasSubscription ? 1 : 0);

            StringBuilder body = new StringBuilder();
            body.append("<html><body style='font-family:Arial,sans-serif;max-width:600px'>")
                .append("<h2>✅ Confirmation de paiement — GolfLC</h2>")
                .append("<p>").append(LocalDateTime.now().format(ZDF_TIME)).append("</p>")
                .append("<p><b>").append(player.getPlayerFirstName()).append(" ").append(player.getPlayerLastName()).append("</b></p>")
                .append("<p>").append(clubBlock(club)).append("</p>");

            if (hasGreenfee) {
                double greenfeeTotal = greenfees.stream().mapToDouble(Greenfee::getPrice).sum();
                body.append("<hr/>");
                if (sectionCount > 1) body.append("<h3>⛳ Greenfee</h3>");
                body.append("<table style='min-width:320px;border-collapse:collapse'>");
                for (int i = 0; i < greenfees.size(); i++) {
                    Greenfee gf = greenfees.get(i);
                    if (i > 0) {
                        body.append("<tr><td colspan='2'><hr style='border:none;border-top:1px dashed #ccc;margin:4px 0'/></td></tr>");
                    }
                    String dateStr = gf.getRoundDate()  != null ? gf.getRoundDate().format(ZDF_TIME_HHmm) : "?";
                    String holes   = gf.getRoundHoles() != null ? " — " + gf.getRoundHoles() + " trous" : "";
                    body.append("<tr><td colspan='2' style='padding-top:4px;font-size:0.9em;color:#777'>Date : <b>")
                        .append(dateStr).append("</b></td></tr>")
                        .append("<tr><td>Greenfee").append(holes).append("</td>")
                        .append("<td align='right'>")
                        .append(String.format("%.2f %s", gf.getPrice(), cs))
                        .append("</td></tr>");
                    if (gf.getItems() != null && !gf.getItems().isBlank()) {
                        body.append("<tr><td colspan='2' style='font-size:0.9em;color:#555'>")
                            .append(gf.getItems()).append("</td></tr>");
                    }
                }
                body.append("<tr><td colspan='2'><hr/></td></tr>")
                    .append("<tr><td><b>").append(sectionCount > 1 ? "Sous-total" : "Total").append("</b></td>")
                    .append("<td align='right'><b>").append(String.format("%.2f %s", greenfeeTotal, cs)).append("</b></td></tr>")
                    .append("</table>");
            }

            if (hasCotisation) {
                String startStr = cotisation.getCotisationStartDate() != null
                    ? cotisation.getCotisationStartDate().format(ZDF_TIME_DAY) : "?";
                String endStr = cotisation.getCotisationEndDate() != null
                    ? cotisation.getCotisationEndDate().format(ZDF_TIME_DAY) : "?";
                body.append("<hr/>");
                if (sectionCount > 1) body.append("<h3>🏌️ Cotisation</h3>");
                body.append("<p>Période : <b>").append(startStr).append(" → ").append(endStr).append("</b></p>")
                    .append("<table style='min-width:320px;border-collapse:collapse'>");
                if (tarifMember != null) {
                    for (EquipmentsAndBasicAndRange b : tarifMember.getBasicList()) {
                        if (b.getPrice() == null || b.getQuantity() == null || b.getQuantity() <= 0) continue;
                        String label = b.getQuantity() > 1
                            ? b.getItem() + " (" + b.getQuantity() + " × " + String.format("%.2f %s", b.getPrice(), cs) + ")"
                            : b.getItem();
                        body.append("<tr><td>").append(label).append("</td>")
                            .append("<td align='right'>")
                            .append(String.format("%.2f %s", b.getPrice() * b.getQuantity(), cs))
                            .append("</td></tr>");
                    }
                    for (EquipmentsAndBasic e : tarifMember.getEquipmentsList()) {
                        if (e.getPrice() == null || e.getQuantity() == null || e.getQuantity() <= 0) continue;
                        String label = e.getQuantity() > 1
                            ? e.getItem() + " (" + e.getQuantity() + " × " + String.format("%.2f %s", e.getPrice(), cs) + ")"
                            : e.getItem();
                        body.append("<tr><td>").append(label).append("</td>")
                            .append("<td align='right'>")
                            .append(String.format("%.2f %s", e.getPrice() * e.getQuantity(), cs))
                            .append("</td></tr>");
                    }
                }
                body.append("<tr><td colspan='2'><hr/></td></tr>")
                    .append("<tr><td><b>").append(sectionCount > 1 ? "Sous-total" : "Total").append("</b></td>")
                    .append("<td align='right'><b>").append(String.format("%.2f %s", cotisation.getPrice(), cs)).append("</b></td></tr>")
                    .append("</table>");
            }

            if (hasSubscription) {
                String startStr = subscription.getStartDate() != null ? subscription.getStartDate().format(ZDF_TIME_DAY) : "?";
                String endStr   = subscription.getEndDate()   != null ? subscription.getEndDate().format(ZDF_TIME_DAY)   : "?";
                String desc = subscription.getCommunication() != null ? subscription.getCommunication() : subscription.getSubCode();
                body.append("<hr/>");
                if (sectionCount > 1) body.append("<h3>📅 Abonnement</h3>");
                body.append("<p>Période : <b>").append(startStr).append(" → ").append(endStr).append("</b></p>")
                    .append("<table style='min-width:320px;border-collapse:collapse'>")
                    .append("<tr><td>").append(desc).append("</td>")
                    .append("<td align='right'>").append(String.format("%.2f %s", subscription.getSubscriptionAmount(), cs)).append("</td></tr>")
                    .append("</table>");
            }

            if (hasLesson) {
                String proName  = !lessons.isEmpty() ? lessons.get(0).getProName() : "";
                double lessonTotal = lessons.stream()
                    .mapToDouble(l -> l.getLessonAmount() != null ? l.getLessonAmount().doubleValue() : 0.0).sum();
                body.append("<hr/>");
                if (sectionCount > 1) body.append("<h3>🎓 Leçon(s)</h3>");
                if (proName != null && !proName.isBlank()) {
                    body.append("<p>Professionnel : <b>").append(proName).append("</b></p>");
                }
                body.append("<table style='min-width:400px;border-collapse:collapse'>");
                for (Lesson lesson : lessons) {
                    String start = lesson.getEventStartDate() != null ? lesson.getEventStartDate().format(ZDF_TIME_HHmm) : "?";
                    String end   = lesson.getEventEndDate()   != null ? lesson.getEventEndDate().format(ZDF_TIME_HHmm)   : "?";
                    body.append("<tr><td>🎓 ").append(lesson.getEventTitle()).append("</td>")
                        .append("<td style='color:#555'>").append(start).append(" → ").append(end).append("</td>")
                        .append("<td align='right'>")
                        .append(String.format("%.2f %s", lesson.getLessonAmount() != null ? lesson.getLessonAmount().doubleValue() : 0.0, cs))
                        .append("</td></tr>");
                }
                body.append("<tr><td colspan='3'><hr/></td></tr>")
                    .append("<tr><td colspan='2'><b>").append(sectionCount > 1 ? "Sous-total" : "Total").append("</b></td>")
                    .append("<td align='right'><b>").append(String.format("%.2f %s", lessonTotal, cs)).append("</b></td></tr>")
                    .append("</table>");
            }

            if (sectionCount > 1) {
                body.append("<hr/>")
                    .append("<table style='min-width:320px;border-collapse:collapse'>")
                    .append("<tr><td><b>Total global</b></td>")
                    .append("<td align='right'><b>")
                    .append(String.format("%.2f %s", creditcard.getTotalPrice(), cs))
                    .append("</b></td></tr></table>");
            }

            body.append("<hr/>")
                .append("<p>").append(creditcard.getCreditcardIssuer()).append(" ").append(creditcard.getCreditCardNumberSecret()).append("</p>")
                .append("<p>Référence : ").append(creditcard.getCreditcardPaymentReference()).append("</p>")
                .append("<br/><p>Merci !<br/>L'équipe GolfLC</p>")
                .append("</body></html>");

            String sujet = buildSubject(sectionCount, hasGreenfee, hasCotisation, hasSubscription, hasLesson, club);
            String to = settings.getProperty("SMTP_USERNAME");
            mailSender.sendHtmlMailAsync(sujet, body.toString(), to, null, player.getPlayerLanguage());
            LOG.info("payment confirmation mail enqueued to={}", to);
            return true;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    private String clubBlock(entite.Club club) {
        if (club == null) return "";
        StringBuilder sb = new StringBuilder();
        if (club.getClubName() != null) sb.append("<b>").append(club.getClubName()).append("</b><br/>");
        entite.Address addr = club.getAddress();
        if (addr != null) {
            if (addr.getStreet() != null && !addr.getStreet().isBlank())
                sb.append(addr.getStreet()).append("<br/>");
            String cityLine = (addr.getZipCode() != null ? addr.getZipCode() + " " : "")
                            + (addr.getCity() != null ? addr.getCity() : "");
            if (!cityLine.isBlank()) sb.append(cityLine).append("<br/>");
            if (addr.getCountry() != null && addr.getCountry().getName() != null)
                sb.append(addr.getCountry().getName()).append("<br/>");
        }
        if (club.getClubWebsite() != null && !club.getClubWebsite().isBlank())
            sb.append("<a href='").append(club.getClubWebsite()).append("' style='color:#0066cc'>")
              .append(club.getClubWebsite()).append("</a>");
        return sb.toString();
    } // end method

    private String buildSubject(int sectionCount, boolean hasGreenfee, boolean hasCotisation,
            boolean hasSubscription, boolean hasLesson, Club club) {
        if (sectionCount > 1) return "✅ Confirmation de paiement — GolfLC";
        String clubName = (club != null && club.getClubName() != null) ? club.getClubName() : "GolfLC";
        if (hasGreenfee)     return "⛳ Confirmation greenfee — " + clubName;
        if (hasCotisation)   return "✅ Confirmation de cotisation — " + clubName;
        if (hasSubscription) return "✅ Confirmation abonnement — " + clubName;
        return "🎓 Confirmation leçon — GolfLC";
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

} // end class
