/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import entite.Player;
import java.sql.Connection;
import java.util.List;

/**
 *
 * @author Collet
 */
public interface PlayerDao {
    public boolean createPlayer(final entite.Player player, final entite.Handicap handicap,final java.sql.Connection conn, final String batch) throws Exception;
    public boolean modifyPlayer(final Player player, final Connection conn) throws Exception;
    public String deletePlayerAndChilds(final int idplayer,final Connection conn) throws Exception;
    public List<Player> getListAllPlayers(final Connection conn) throws Exception;
    // pas de find
    // pas de load
}
