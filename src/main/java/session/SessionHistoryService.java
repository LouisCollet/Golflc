
package session;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;


// ==================== SESSION HISTORY SERVICE (FAKE) ====================

@ApplicationScoped
public class SessionHistoryService implements Serializable {
    
    /**
     * Retourne les N dernières sessions (fake data pour le moment)
     */
    public List<SessionHistory> getRecentSessions(int limit) {
        List<SessionHistory> sessions = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        cal.getTime().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        // Session 1 - Aujourd'hui
        sessions.add(new SessionHistory(
            1L,
            "john.doe",
            cal.getTime().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime(),
            "Desktop (Windows)",
            "Chrome",
            "192.168.1.100"
        ));
        
        // Session 2 - Hier
        cal.add(Calendar.DAY_OF_MONTH, -1);
        sessions.add(new SessionHistory(
            2L,
            "john.doe",
            cal.getTime().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime(),
            "Mobile (Android)",
            "Chrome",
            "192.168.1.101"
        ));
        
        // Session 3 - Il y a 2 jours
        cal.add(Calendar.DAY_OF_MONTH, -1);
        sessions.add(new SessionHistory(
            3L,
            "john.doe",
            cal.getTime().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime(),
            "Desktop (macOS)",
            "Safari",
            "192.168.1.102"
        ));
        
        // Session 4 - Il y a 3 jours
        cal.add(Calendar.DAY_OF_MONTH, -1);
        sessions.add(new SessionHistory(
            4L,
            "john.doe",
            cal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
            "Tablet (iOS)",
            "Safari",
            "192.168.1.103"
        ));
        
        // Session 5 - Il y a 5 jours
        cal.add(Calendar.DAY_OF_MONTH, -2);
        sessions.add(new SessionHistory(
            5L,
            "john.doe",
            cal.getTime().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime(),
            "Desktop (Linux)",
            "Firefox",
            "192.168.1.104"
        ));
        
        // Retourner seulement le nombre demandé
        return sessions.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Retourne toutes les sessions (fake data pour le moment)
     */
    public List<SessionHistory> getAllSessions() {
        return getRecentSessions(10); // Retourne 10 sessions fake
    }
    
    /**
     * Retourne les sessions dans une plage de dates (fake data pour le moment)
     */
    public List<SessionHistory> getSessionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Pour le moment, retourne juste les sessions récentes
        return getRecentSessions(5);
    }
    
    /**
     * Retourne la dernière session (fake data pour le moment)
     */
    public SessionHistory getLastSession() {
        List<SessionHistory> sessions = getRecentSessions(1);
        return sessions.isEmpty() ? null : sessions.get(0);
    }
    
    /**
     * Compte le nombre de sessions actives (fake pour le moment)
     */
    public long getActiveSessionsCount() {
        return 1L; // Toujours 1 session active pour le fake
    }
}

