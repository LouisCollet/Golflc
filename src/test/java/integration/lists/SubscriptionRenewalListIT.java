package integration.lists;

import connection_package.JdbcConnectionProvider;
import static interfaces.Log.LOG;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test d'intégration — vérifie la requête SQL de SubscriptionRenewalList
 * contre la vraie base MySQL (lecture seule, aucun mail envoyé).
 *
 * Prérequis : variables d'environnement MYSQL_USERNAME et MYSQL_PASSWORD définies.
 * Lancer avec : mvn failsafe:integration-test -Pfast-it -Dit.test=SubscriptionRenewalListIT
 */
@Tag("integration")
public class SubscriptionRenewalListIT {

    // ✅ Référence la constante de production — pas de duplication SQL
    private static final String QUERY         = lists.SubscriptionRenewalList.QUERY;
    private static final String EXPLAIN_QUERY = "EXPLAIN ANALYZE " + QUERY;

    /**
     * La requête s'exécute sans erreur et chaque ligne a un idplayer valide.
     */
    @Test
    void query_realDB_executesWithoutError() throws Exception {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        LOG.info("Searching renewals for {}/{}", nextMonth.getMonthValue(), nextMonth.getYear());

        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY);
             ResultSet rs = ps.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                int idPlayer = rs.getInt("idplayer");
                String lastName = rs.getString("PlayerLastName");
                String endDate = rs.getString("SubscriptionEndDate");

                assertTrue(idPlayer > 0, "idplayer doit être > 0");
                assertNotNull(endDate, "SubscriptionEndDate ne doit pas être null");

                LOG.info("renewal candidate — idplayer={} name={} endDate={}", idPlayer, lastName, endDate);
                count++;
            }

            LOG.info("Total renewal candidates for {}/{} : {}", nextMonth.getMonthValue(), nextMonth.getYear(), count);
            // Pas d'assertion sur le nombre — dépend des données réelles
        }
    }

    /**
     * Vérifie que la requête ne retourne pas de joueurs inactifs (PlayerActivation != '1').
     */
    @Test
    void query_realDB_onlyActivePlayersReturned() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String activation = rs.getString("PlayerActivation");
                assertEquals("1", activation,
                        "Seuls les joueurs actifs (PlayerActivation='1') doivent apparaître");
            }
        }
    }

    /**
     * Vérifie le fix décembre : MONTH(CURRENT_DATE()) + 1 retournait 13.
     * La requête corrigée doit fonctionner quel que soit le mois courant.
     */
    @Test
    void query_decemberFix_noArithmeticOverflow() throws Exception {
        // Exécution sans exception = le fix DATE_ADD fonctionne correctement
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY);
             ResultSet rs = ps.executeQuery()) {

            // Vérifier que les dates retournées sont bien dans le mois suivant
            LocalDate nextMonth = LocalDate.now().plusMonths(1);
            while (rs.next()) {
                String endDateStr = rs.getString("SubscriptionEndDate");
                assertNotNull(endDateStr);

                // Extraire année et mois depuis la date
                int year  = rs.getTimestamp("SubscriptionEndDate").toLocalDateTime().getYear();
                int month = rs.getTimestamp("SubscriptionEndDate").toLocalDateTime().getMonthValue();

                assertEquals(nextMonth.getYear(),        year,
                        "L'année doit correspondre au mois suivant");
                assertEquals(nextMonth.getMonthValue(),  month,
                        "Le mois doit correspondre au mois suivant");
            }
        }
    }

    /**
     * Compte les abonnements expirant dans les 12 prochains mois (diagnostique).
     */
    @Test
    void query_next12Months_diagnosticCount() throws Exception {
        final String diagnosticQuery = """
                SELECT YEAR(SubscriptionEndDate)  AS yr,
                       MONTH(SubscriptionEndDate) AS mo,
                       COUNT(*)                   AS cnt
                FROM payments_subscription
                JOIN player
                   ON player.idplayer = payments_subscription.SubscriptionIdPlayer
                   AND PlayerActivation = '1'
                WHERE SubscriptionEndDate >= CURRENT_DATE()
                  AND SubscriptionEndDate <  DATE_ADD(CURRENT_DATE(), INTERVAL 12 MONTH)
                GROUP BY yr, mo
                ORDER BY yr, mo
                """;

        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(diagnosticQuery);
             ResultSet rs = ps.executeQuery()) {

            LOG.info("--- Abonnements à renouveler par mois (12 prochains mois) ---");
            while (rs.next()) {
                LOG.info("{}/{} : {} abonnements", rs.getInt("mo"), rs.getInt("yr"), rs.getInt("cnt"));
            }
            // Test diagnostique — pas d'assertion, sert à visualiser les données
        }
    }

    /**
     * EXPLAIN ANALYZE — affiche le plan d'exécution réel de la requête (MySQL 8.0+).
     * Permet de vérifier l'utilisation des index et détecter les full scans.
     */
    @Test
    void query_explainAnalyze_showsExecutionPlan() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(EXPLAIN_QUERY);
             ResultSet rs = ps.executeQuery()) {

            LOG.info("--- EXPLAIN ANALYZE SubscriptionRenewalList ---");
            while (rs.next()) {
                LOG.info("{}", rs.getString(1));
            }
            // Test diagnostique — pas d'assertion, sert à visualiser le plan
        }
    }

} // end class
