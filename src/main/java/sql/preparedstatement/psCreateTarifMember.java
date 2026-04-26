package sql.preparedstatement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.TarifMember;
import static interfaces.Log.LOG;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import static utils.LCUtil.getCurrentMethodName;

/**
 * PreparedStatement mapper pour tarif_members (INSERT).
 *
 * Colonnes (ordre INSERT généré par SqlFactory.generateInsertQuery) :
 *  1. TarifMemberId           — NULL (autoincrement)
 *  2. TarifMemberStartDate    — TIMESTAMP
 *  3. TarifMemberEndDate      — TIMESTAMP
 *  4. TarifMemberIdClub       — INT
 *  5. TarifMemberJson         — LONGTEXT (sérialisation Jackson de TarifMember)
 *  6. TarifMemberCreationDate — TIMESTAMP
 */
public class psCreateTarifMember {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * Remplit le PreparedStatement pour un INSERT dans tarif_members.
     *
     * @param ps    PreparedStatement déjà préparé
     * @param tarif objet TarifMember à insérer
     */
    public static void mapCreate(PreparedStatement ps,
                                 TarifMember tarif) throws Exception {
        try {
            LOG.debug("entering mapCreate for TarifMember idClub={}", tarif.getTarifMemberIdClub());
            final String methodName = utils.LCUtil.getCurrentMethodName();
            String json = OBJECT_MAPPER.writeValueAsString(tarif);
            LOG.debug("mapCreate — json length={}", json.length());

            ps.setNull     (1, java.sql.Types.INTEGER);                   // TarifMemberId (autoincrement)
            ps.setTimestamp(2, Timestamp.valueOf(tarif.getStartDate()));   // TarifMemberStartDate
            ps.setTimestamp(3, Timestamp.valueOf(tarif.getEndDate()));     // TarifMemberEndDate
            ps.setInt      (4, tarif.getTarifMemberIdClub());              // TarifMemberIdClub
            ps.setString   (5, json);                                      // TarifMemberJson
            ps.setTimestamp(6, Timestamp.from(Instant.now()));             // TarifMemberCreationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);

        } catch (Exception e) {
            String msg = "Exception in mapCreate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    } // end method

} // end class
