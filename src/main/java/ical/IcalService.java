package ical;

import Controller.refact.PlayerController;
import context.ApplicationContext;
import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.*;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.util.RandomUidGenerator;
// ✅ SUPPRIMÉ : import connection_package.DBConnection;
// ✅ SUPPRIMÉ : import java.sql.Connection;
// ✅ SUPPRIMÉ : import java.sql.SQLException;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class IcalService {

    @Inject
    private mail.EmailService mailService;

    // ✅ AJOUTÉ : injection CDI ReadClub
    @Inject
    private read.ReadClub readClubService;

    // @Inject
    // private PlayerController playerC;

    @Inject
    private ApplicationContext appContext;

    @Inject
    private entite.Settings settings;

    public byte[] generateIcs(Player player,
            Player invitedBy,
            Round round,
            Club club,
            Course course,
            boolean isMeetingInvite) {

        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            LOG.debug("entering IcalGeneratorv4.create using version 4.0.0");
            Objects.requireNonNull(club,   "club is null");
            Objects.requireNonNull(round,  "round is null");
            Objects.requireNonNull(course, "course is null");
            Objects.requireNonNull(player, "player is null");

            long startNanos = System.nanoTime();
            List<Player> attendees = round.getPlayers();
         //   LOG.debug("attendees = " + attendees.toString());
            round.getPlayers().forEach(item -> LOG.debug("existing players - round.getPlayers = " + item + "/"));
            LOG.debug("list invitedBy DroppedPlayers = " + invitedBy.getDroppedPlayers());

            CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
            Calendar calendar = new Calendar();
            calendar.add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
            calendar.add(ImmutableVersion.VERSION_2_0);
            calendar.add(ImmutableCalScale.GREGORIAN);

            // ---- ZoneId ----
            String zoneIdValue = club.getAddress().getZoneId();
            if (zoneIdValue == null || zoneIdValue.isBlank()) {
                throw new IllegalStateException("ZoneId manquant pour le club");
            }
            ZoneId javaZoneId = null;
            try {
                javaZoneId = ZoneId.of(zoneIdValue);
            } catch (DateTimeException e) {
                throw new IllegalArgumentException("javaZoneId invalide : " + javaZoneId, e);
            }

            // ---- Dates ----
            ZonedDateTime start = ZonedDateTime.of(round.getRoundDate(), javaZoneId);
            ZonedDateTime end = start.plusHours(5);

            String actionType = isMeetingInvite ? "Réservation" : "Cancellation";
            String summary = String.format(
                    "Golf Round %s : %s - %s",
                    actionType,
                    club.getClubName(),
                    course.getCourseName()
            );

            VEvent event = new VEvent(start, end, summary);

            event.add(new Location(String.format(
                    "%s, %s, %s",
                    club.getClubName(),
                    club.getAddress().getStreet(),
                    club.getAddress().getCity()
            )));

            event.add(new Description(
                    round.getRoundGame() + " " +
                    course.getCourseName() + NEW_LINE +
                    "Les autres joueurs du flight sont : " +
                    round.getPlayersString()
            ));

            // ALARM
            VAlarm alarm = new VAlarm();
            alarm.add(new Trigger(Duration.ofHours(-24)));
            alarm.add(new Action("DISPLAY"));
            alarm.add(new Description("First Reminder"));
            event.add(alarm);
            // 2e alarme new 31-12-2025
            alarm = new VAlarm();
            alarm.add(new Trigger(Duration.ofMinutes(-30)));
            alarm.add(new Action("DISPLAY"));
            alarm.add(new Description("Second Reminder"));
            event.add(alarm);

            event.add(new RandomUidGenerator().generateUid());
            event.add(new Priority(5));
            event.add(new Sequence(1));

            // ---- Attendees ----
            for (Player info : attendees) {
                Attendee attendee = new Attendee(URI.create("mailto:" + info.getPlayerEmail()));
                attendee.add(Role.REQ_PARTICIPANT);
                attendee.add(PartStat.NEEDS_ACTION);
                //attendee.add(info.isRequired()
                //            ? Role.REQ_PARTICIPANT
                //            : Role.OPT_PARTICIPANT);
                attendee.add(Rsvp.TRUE);
                attendee.add(new Cn(info.getPlayerLastName() + ", " + info.getPlayerFirstName()));
                event.add(attendee);
            }

            // ---- Organizer ----
            String hostEmail = settings.getProperty("SMTP_USERNAME");
            if (hostEmail == null || hostEmail.isBlank()) {
                throw new IllegalStateException("SMTP_USERNAME non défini");
            }
            Organizer organizer = new Organizer(URI.create("mailto:" + hostEmail));
            organizer.add(Role.CHAIR);
            organizer.add(new Cn("Louis Collet"));
            organizer.add(new SentBy(URI.create("mailto:" + hostEmail)));
            event.add(organizer);

            calendar.add(event);

            LOG.debug("line 08 VERSION FINALE after organizer = \n" + event);

            /* new 25-11-2024
            /Outlook uses a custom property to display HTML called the X-ALT-DESC property
            ...
            */

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                new CalendarOutputter().output(calendar, baos);
                long elapsedNanos = System.nanoTime() - startNanos;
                LOG.debug("Elapsed time Nanos: " + elapsedNanos);
                double elapsedMillis = elapsedNanos / 1_000_000.0;
                LOG.debug("Elapsed time Millis: " + elapsedMillis + " ms");
                return baos.toByteArray();
            }

        } catch (Exception e) {
            String msg = "££ Exception in generateIcs = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            handleGenericException(e, methodName);
            return null;
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