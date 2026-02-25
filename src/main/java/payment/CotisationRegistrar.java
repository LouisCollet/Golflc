
package payment;

import entite.Club;
import entite.Player;
import entite.Creditcard;
// import java.sql.Connection; // removed 2026-02-25

public final class CotisationRegistrar
        implements PaymentRegistrar<CotisationPayment> {

    private final Creditcard creditcard;
    private final Player player;
    private final Club club;

    public CotisationRegistrar(Creditcard creditcard, Player player, Club club) { // conn removed 2026-02-25
        this.creditcard = creditcard;
        this.player = player;
        this.club = club;
    }

    @Override
    public boolean register(CotisationPayment payment) throws Exception {
        // Exemple : définir une référence et éventuellement utiliser les autres objets
        payment.setPaymentReference("REF123");
        // Ici, tu peux ajouter l'enregistrement réel dans la base via conn
        return true;
    }
}
