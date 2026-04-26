package sql.preparedstatement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import entite.TarifGreenfee;
import static interfaces.Log.LOG;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import static utils.LCUtil.getCurrentMethodName;

/**
 * PreparedStatement mapper pour tarif_greenfee (INSERT).
 *
 * Colonnes (ordre INSERT généré par LCUtil.generateInsertQuery) :
 *  1. TarifId           — NULL (autoincrement)
 *  2. TarifYear         — INT  (année de début de la première période)
 *  3. TarifStartDate    — TIMESTAMP (premier jour de la première période)
 *  4. TarifEndDate      — TIMESTAMP (premier jour de la dernière période)
 *  5. TarifCourseId     — INT
 *  6. TarifHoles        — TINYINT (9 ou 18)
 *  7. TarifJson         — LONGTEXT (sérialisation Jackson de TarifGreenfee)
 *  8. TarifCurrency        — VARCHAR(3)
 *  9. TarifModificationDate — TIMESTAMP (auto-update)
 */
public class psCreateTarifGreenfee {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Remplit le PreparedStatement pour un INSERT dans tarif_greenfee.
     *
     * @param ps    PreparedStatement déjà préparé
     * @param tarif objet TarifGreenfee à insérer (doit avoir au moins une période)
     * @param club  club propriétaire (pour le code devise)
     */
    public static void mapCreate(PreparedStatement ps,
                                 TarifGreenfee tarif,
                                 Club club,
                                 int courseId,
                                 int holesValue) throws Exception {
        try {
            final String methodName = utils.LCUtil.getCurrentMethodName();
            LOG.debug("entering mapCreate for tarif courseId={}", courseId);

            LocalDateTime lddeb = tarif.getDatesSeasonsList().get(0).getStartDate().truncatedTo(ChronoUnit.DAYS);
            int last = tarif.getDatesSeasonsList().size() - 1;
            LocalDateTime ldfin = tarif.getDatesSeasonsList().get(last).getEndDate().truncatedTo(ChronoUnit.DAYS);

            String json = OBJECT_MAPPER.writeValueAsString(tarif);
            LOG.debug("mapCreate — json length={}", json.length());

            ps.setNull     (1, java.sql.Types.INTEGER);                        // TarifId (autoincrement)
            ps.setInt      (2, lddeb.getYear());                               // TarifYear
            ps.setTimestamp(3, Timestamp.valueOf(lddeb));                      // TarifStartDate
            ps.setTimestamp(4, Timestamp.valueOf(ldfin));                      // TarifEndDate
            ps.setInt      (5, courseId);                                      // TarifCourseId
            ps.setInt      (6, holesValue);                                    // TarifHoles (9 ou 18) — reçu en paramètre
            ps.setString   (7, json);                                          // TarifJson
            ps.setString   (8, club.getAddress().getCountry().getCurrency());  // TarifCurrency
            ps.setTimestamp(9, Timestamp.from(Instant.now())); 
            sql.PrintWarnings.print(ps.getWarnings(), methodName);// TarifModificationDate
            utils.LCUtil.logps(ps);

        } catch (Exception e) {
            String msg = "Exception in mapCreate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    } // end method

} // end class
