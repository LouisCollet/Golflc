package ical;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import utils.LCUtil;
import static utils.LCUtil.getCurrentClassName;

/**
 * Service pour la génération de fichiers iCalendar (.ics) pour les réservations de golf.
 * <p>
 * Ce service utilise la bibliothèque iCal4j version 4.0.0+ pour créer des invitations
 * de calendrier compatibles avec les clients email standards (Outlook, Gmail, etc.).
 * </p>
 */
// non teté, non en production !
@ApplicationScoped
@Named // à compléter
public class improved_icalservice {
    
    private static final String CLASSNAME = getCurrentClassName();
    private static final String ICAL4J_VERSION = "4.0.0";
    
    // Constantes pour la configuration de l'événement
    private static final Duration ROUND_DURATION = Duration.ofHours(5);
    private static final Duration FIRST_REMINDER = Duration.ofHours(-24);
    private static final Duration SECOND_REMINDER = Duration.ofMinutes(-30);
    private static final int EVENT_PRIORITY = 5; // RFC 5545: 0-9
    private static final int EVENT_SEQUENCE = 1;
    
    // Constantes pour le format
    private static final String PROD_ID = "-//Ben Fortuna//iCal4j 1.0//EN";
    private static final String ORGANIZER_NAME = "Louis Collet";
    private static final String SUMMARY_FORMAT = "Golf Round %s : %s - %s";
    private static final String LOCATION_FORMAT = "%s, %s, %s";
    
    // Variables d'environnement
    private static final String ENV_SMTP_USERNAME = "SMTP_USERNAME";
    
    @Inject
    private mail.EmailService mailService;
    
    /**
     * Génère un fichier iCalendar (.ics) pour une réservation de golf.
     * <p>
     * Cette méthode crée un événement de calendrier complet avec :
     * <ul>
     *   <li>Informations sur le round (date, lieu, parcours)</li>
     *   <li>Liste des participants (attendees)</li>
     *   <li>Deux rappels (24h avant et 30min avant)</li>
     *   <li>Organisateur de l'événement</li>
     * </ul>
     * </p>
     *
     * @param player le joueur recevant l'invitation
     * @param invitedBy le joueur ayant invité (peut être null)
     * @param round les détails du round de golf
     * @param club le club de golf
     * @param course le parcours
     * @param isMeetingInvite true pour une réservation, false pour une annulation
     * @return le contenu du fichier .ics sous forme de byte array
     * @throws IllegalArgumentException si un paramètre requis est null ou invalide
     * @throws IllegalStateException si la configuration est incomplète (SMTP_USERNAME, ZoneId)
     * @throws IOException si l'écriture du calendrier échoue
     */
    public byte[] generateIcs(
            Player player,
            Player invitedBy,
            Round round,
            Club club,
            Course course,
            boolean isMeetingInvite) throws IOException {
        
        final String methodName = LCUtil.getCurrentMethodName();
        long startNanos = System.nanoTime();
        
        try {
            LOG.debug("Entering IcalService.generateIcs using iCal4j version {}", ICAL4J_VERSION);
            
            // Validation des paramètres requis
            validateInputs(player, round, club, course);
            
            // Configuration de la compatibilité Outlook
            CompatibilityHints.setHintEnabled(
                CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, 
                true
            );
            
            // Création du calendrier de base
            Calendar calendar = createBaseCalendar();
            
            // Récupération et validation du fuseau horaire
            ZoneId zoneId = getValidatedZoneId(club);
            
            // Création de l'événement
            VEvent event = createEvent(round, club, course, zoneId, isMeetingInvite);
            
            // Ajout des alarmes/rappels
            addReminders(event);
            
            // Ajout des métadonnées de l'événement
            addEventMetadata(event);
            
            // Ajout des participants
            addAttendees(event, round.getPlayers());
            
            // Ajout de l'organisateur
            addOrganizer(event);
            
            // Ajout de l'événement au calendrier
            calendar.add(event);
            
            LOG.debug("Event created successfully:\n{}", event);
            
            // Génération du fichier .ics
            byte[] icsContent = serializeCalendar(calendar);
            
            logPerformance(startNanos);
            
            return icsContent;
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOG.error("Validation error in generateIcs: {}", e.getMessage());
            handleGenericException(e, methodName);
            throw e;
            
        } catch (Exception e) {
            LOG.error("Unexpected error in generateIcs: {}", e.getMessage(), e);
            handleGenericException(e, methodName);
            throw new IOException("Failed to generate ICS file", e);
        }
    }
    
    /**
     * Valide les paramètres d'entrée requis.
     */
    private void validateInputs(Player player, Round round, Club club, Course course) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(round, "round cannot be null");
        Objects.requireNonNull(club, "club cannot be null");
        Objects.requireNonNull(course, "course cannot be null");
        
        if (round.getRoundDate() == null) {
            throw new IllegalArgumentException("round date cannot be null");
        }
        
        if (club.getAddress() == null) {
            throw new IllegalArgumentException("club address cannot be null");
        }
        
        List<Player> players = round.getPlayers();
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("round must have at least one player");
        }
        
        LOG.debug("Input validation passed - {} attendees", players.size());
    }
    
    /**
     * Crée et configure le calendrier de base.
     */
    private Calendar createBaseCalendar() {
        Calendar calendar = new Calendar();
        calendar.add(new ProdId(PROD_ID));
        calendar.add(ImmutableVersion.VERSION_2_0);
        calendar.add(ImmutableCalScale.GREGORIAN);
        return calendar;
    }
    
    /**
     * Récupère et valide le fuseau horaire du club.
     */
    private ZoneId getValidatedZoneId(Club club) {
        String zoneIdValue = club.getAddress().getZoneId();
        
        if (zoneIdValue == null || zoneIdValue.isBlank()) {
            throw new IllegalStateException(
                "ZoneId is missing for club: " + club.getClubName()
            );
        }
        
        try {
            ZoneId zoneId = ZoneId.of(zoneIdValue);
            LOG.debug("Using timezone: {}", zoneId);
            return zoneId;
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(
                "Invalid ZoneId: " + zoneIdValue, 
                e
            );
        }
    }
    
    /**
     * Crée l'événement principal avec ses propriétés de base.
     */
    private VEvent createEvent(
            Round round,
            Club club,
            Course course,
            ZoneId zoneId,
            boolean isMeetingInvite) {
        
        // Calcul des dates de début et fin
        ZonedDateTime start = ZonedDateTime.of(round.getRoundDate(), zoneId);
        ZonedDateTime end = start.plus(ROUND_DURATION);
        
        LOG.debug("Event time: {} to {} ({})", start, end, ROUND_DURATION);
        
        // Création du titre
        String actionType = isMeetingInvite ? "Réservation" : "Cancellation";
        String summary = String.format(
            SUMMARY_FORMAT,
            actionType,
            club.getClubName(),
            course.getCourseName()
        );
        
        // Création de l'événement
        VEvent event = new VEvent(start, end, summary);
        
        // Ajout du lieu
        String location = String.format(
            LOCATION_FORMAT,
            club.getClubName(),
            club.getAddress().getStreet(),
            club.getAddress().getCity()
        );
        event.add(new Location(location));
        
        // Ajout de la description
        String description = String.format(
            "%s %s%sLes autres joueurs du flight sont : %s",
            round.getRoundGame(),
            course.getCourseName(),
            NEW_LINE,
            round.getPlayersString()
        );
        event.add(new Description(description));
        
        return event;
    }
    
    /**
     * Ajoute les rappels/alarmes à l'événement.
     */
    private void addReminders(VEvent event) {
        // Premier rappel : 24 heures avant
        VAlarm firstReminder = new VAlarm();
        firstReminder.add(new Trigger(FIRST_REMINDER));
        firstReminder.add(new Action("DISPLAY"));
        firstReminder.add(new Description("First Reminder"));
        event.add(firstReminder);
        
        // Second rappel : 30 minutes avant
        VAlarm secondReminder = new VAlarm();
        secondReminder.add(new Trigger(SECOND_REMINDER));
        secondReminder.add(new Action("DISPLAY"));
        secondReminder.add(new Description("Second Reminder"));
        event.add(secondReminder);
        
        LOG.debug("Added {} reminders to event", 2);
    }
    
    /**
     * Ajoute les métadonnées de l'événement (UID, priorité, séquence).
     */
    private void addEventMetadata(VEvent event) {
        event.add(new RandomUidGenerator().generateUid());
        event.add(new Priority(EVENT_PRIORITY));
        event.add(new Sequence(EVENT_SEQUENCE));
    }
    
    /**
     * Ajoute les participants à l'événement.
     */
    private void addAttendees(VEvent event, List<Player> players) {
        for (Player player : players) {
            if (player.getPlayerEmail() == null || player.getPlayerEmail().isBlank()) {
                LOG.warn("Skipping player {} - no email address", player.getIdplayer());
                continue;
            }
            
            Attendee attendee = new Attendee(
                URI.create("mailto:" + player.getPlayerEmail())
            );
            attendee.add(Role.REQ_PARTICIPANT);
            attendee.add(PartStat.NEEDS_ACTION);
            attendee.add(Rsvp.TRUE);
            
            String displayName = String.format(
                "%s, %s",
                player.getPlayerLastName(),
                player.getPlayerFirstName()
            );
            attendee.add(new Cn(displayName));
            
            event.add(attendee);
        }
        
        LOG.debug("Added {} attendees to event", players.size());
    }
    
    /**
     * Ajoute l'organisateur de l'événement.
     */
    private void addOrganizer(VEvent event) {
        String hostEmail = System.getenv(ENV_SMTP_USERNAME);
        
        if (hostEmail == null || hostEmail.isBlank()) {
            throw new IllegalStateException(
                ENV_SMTP_USERNAME + " environment variable is not defined"
            );
        }
        
        Organizer organizer = new Organizer(URI.create("mailto:" + hostEmail));
        organizer.add(Role.CHAIR);
        organizer.add(new Cn(ORGANIZER_NAME));
        organizer.add(new SentBy(URI.create("mailto:" + hostEmail)));
        
        event.add(organizer);
        
        LOG.debug("Added organizer: {}", ORGANIZER_NAME);
    }
    
    /**
     * Sérialise le calendrier en byte array.
     */
    private byte[] serializeCalendar(Calendar calendar) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            new CalendarOutputter().output(calendar, baos);
            return baos.toByteArray();
        }
    }
    
    /**
     * Log les métriques de performance.
     */
    private void logPerformance(long startNanos) {
        long elapsedNanos = System.nanoTime() - startNanos;
        double elapsedMillis = elapsedNanos / 1_000_000.0;
        LOG.debug("ICS generation completed in {:.2f} ms", elapsedMillis);
    }
}
