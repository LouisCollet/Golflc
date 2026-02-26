package mail;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * @deprecated MailSender.sendHtmlMailAsync() provides the same functionality via ManagedExecutorService.
 * This class is kept for backward compatibility but should not be used for new code.
 */
@Deprecated
@ApplicationScoped
public class MailServiceAsync implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private MailSender mailSender;

    public interface MailCallback {
        void onSuccess(String recipient);
        void onFailure(String recipient, Exception e);
    }

    public MailServiceAsync() { }

    public void sendHtmlMailAsync(final String title,
                                  final String content,
                                  final String recipient,
                                  final byte[] pathICS,
                                  final byte[] pathQRC,
                                  final String targetLanguage,
                                  final MailCallback callback) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        mailSender.sendHtmlMailAsync(title, content, recipient, pathICS, pathQRC, targetLanguage)
            .thenRun(() -> {
                LOG.debug("Email envoye avec succes a " + recipient);
                if (callback != null) {
                    callback.onSuccess(recipient);
                }
            })
            .exceptionally(e -> {
                LOG.error("Echec de l'envoi de l'email a " + recipient, e);
                if (callback != null) {
                    callback.onFailure(recipient, (Exception) e.getCause());
                }
                return null;
            });
    } // end method

} // end class
