
package interfaces;

import entite.Player;
import java.sql.Connection;
import java.util.List;

/**
 *
 * @author Collet
 */
//public interface PlayerInterface {  final Player player, final HandicapIndex handicapIndex, final Connection conn,
public interface PlayerInterface {  // all methods are implicitly public
   boolean create(final entite.Player player, final entite.Handicap handicap,final java.sql.Connection conn, final String batch) throws Exception;
 //  boolean modifyPlayer(final Player player, final Connection conn) throws Exception;
//  String deletePlayerAndChilds(final int idplayer,final Connection conn) throws Exception;
 //  List<Player> getListAllPlayers(final Connection conn) throws Exception;
    // pas de find
    // pas de load
}
