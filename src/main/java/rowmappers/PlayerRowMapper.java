
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
            player.setPlayerModificationDate(getTimestamp(rs,"PlayerModificationDate"));

            return player;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}


/*
import entite.Address;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerRowMapper implements RowMapper<Player> {
 
    @Override
   public Player map(ResultSet rs) throws SQLException {
       final String methodName = utils.LCUtil.getCurrentMethodName();  
   try{  
        Player player = new Player();
//   LOG.debug("entering mapPlayer");
        player.setIdplayer(rs.getInt("idplayer"));
        player.setPlayerFirstName(rs.getString("PlayerFirstName"));
        player.setPlayerLastName(rs.getString("PlayerLastName"));
     ///   Address address = Address.mapPlayer(rs);
        ///   player.setAddress(address);
        RowMapper<Address> mapper = new AddressPlayerRowMapper();
        player.setAddress(mapper.map(rs)); // mod 15-10-2024 non testé
        player.setPlayerBirthDate(rs.getTimestamp("PlayerBirthDate").toLocalDateTime());
        player.setPlayerGender(rs.getString("playergender"));
        player.setPlayerHomeClub(rs.getInt("playerhomeclub"));
        player.setPlayerLanguage(rs.getString("playerLanguage"));
        player.setPlayerEmail(rs.getString("PlayerEmail"));
        player.setPlayerPhotoLocation(rs.getString("PlayerPhotoLocation"));
        player.setPlayerRole(rs.getString("PlayerRole"));
        player.setPlayerModificationDate(rs.getTimestamp("PlayerModificationDate")); // new 20-10-2023
        // 28-06-2023 pour tester ...https://www.baeldung.com/jdbc-resultset
   //     examineRs(rs);
   //      LOG.debug("end of examineRs with player = " + player);
        
   //   LOG.debug("end of mapPlayer with player = " + player);
   return player;
 }catch(Exception e){
        handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class*/