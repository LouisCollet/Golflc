package rowmappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Address;
import entite.Club;
import entite.Player;
import entite.UnavailablePeriod;
import entite.UnavailableStructure;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClubRowMapper extends AbstractRowMapper<Club> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public Club map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            // Vérification / debug du ResultSet
            Player.examineRs(rs);

            var club = new Club();

            // Champs simples
            club.setIdclub(getInteger(rs, "idclub"));
           // club.setClubName(getStringOrDefault(rs, "clubName", "<structure UNKNOWN>"));
            club.setClubName(getString(rs, "clubName"));
            club.setClubWebsite(getStringOrDefault(rs, "ClubWebsite", "<website UNKNOWN>"));
            club.setClubLocalAdmin(getInteger(rs, "ClubLocalAdmin"));

            // Adresse via RowMapper dédié
            RowMapper<Address> addressMapper = new AddressClubRowMapper();
            club.setAddress(addressMapper.map(rs));

            // JSON → UnavailableStructure (null-safe + fallback)
          //  club.setUnavailableStructure(getUnavailableStructure(rs, "ClubUnavailableStructure"));
    // ??       RowMapper<UnavailablePeriod> periodMapper = new UnavailablePeriodRowMapper();
           RowMapper<UnavailableStructure> structureMapper = new UnavailableStructureRowMapper();
          
         //  club.setUnavailablePeriod(periodMapper.map(rs)); //unavailableStructure);setUnavailableStructure(rs,"ClubUnavailableStructure");
           club.setUnavailableStructure(structureMapper.map(rs));
            return club;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }

    /* =======================
       Helper JSON sécurisé
       ======================= */
    private UnavailableStructure getUnavailableStructure(ResultSet rs, String column) {
        try {
            // Si la colonne est absente ou vide, retour d'un objet par défaut
            if (!hasColumn(rs, column) || rs.getString(column) == null || rs.getString(column).isBlank()) {
                UnavailableStructure fallback = new UnavailableStructure();
           //     fallback.setStructureName("structure unknown");
                return fallback;
            }

            String json = rs.getString(column);
            return OBJECT_MAPPER.readValue(json, UnavailableStructure.class);

        } catch (Exception e) {
            // En cas de JSON invalide, on retourne également le fallback
            UnavailableStructure fallback = new UnavailableStructure();
        //    fallback.setStructureName("structure unknown");
            return fallback;
        }
    }
}
