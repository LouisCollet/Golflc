
package rowmappers;

import entite.Address;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerRowMapper extends AbstractRowMapper<Player> {

    @Override
    public Player map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            Player player = new Player();
            player.setIdplayer(getInteger(rs,"idplayer"));
            player.setPlayerFirstName(getString(rs, "PlayerFirstName"));
            player.setPlayerLastName(getString(rs, "PlayerLastName"));

            RowMapper<Address> mapper = new AddressPlayerRowMapper();
            player.setAddress(mapper.map(rs));

            player.setPlayerBirthDate(getLocalDateTime(rs, "PlayerBirthDate"));
            player.setPlayerGender(getString(rs, "playergender"));
            player.setPlayerHomeClub(getInteger(rs,"playerhomeclub"));
            player.setPlayerLanguage(getString(rs, "playerLanguage"));
            player.setPlayerEmail(getString(rs, "PlayerEmail"));
            player.setPlayerPhotoLocation(getString(rs, "PlayerPhotoLocation"));
            player.setPlayerRole(getString(rs, "PlayerRole"));
            player.setPlayerModificationDate(getLocalDateTime(rs, "PlayerModificationDate"));

            return player;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
} // end class
