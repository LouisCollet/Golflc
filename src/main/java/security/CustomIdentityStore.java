package security;

import static interfaces.Log.LOG;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import static jakarta.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;

/**
 * Jakarta Security Identity Store.
 * ✅ Security audit 2026-03-19 — removed hardcoded credentials (adminLC/pwd1)
 *
 * Note: The app currently uses selectPlayerById() for login (not Jakarta Security).
 * This store is a placeholder — returns INVALID_RESULT until Jakarta Security auth is activated.
 * When activated, implement DB-backed validation here.
 */
@ApplicationScoped
public class CustomIdentityStore implements IdentityStore {

    @Inject
    private IdentityStoreHandler idStoreHandler;

    public CustomIdentityStore() {} // end constructor

    public CredentialValidationResult validate(UsernamePasswordCredential userCredential) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // ✅ No hardcoded credentials — always reject until DB auth is implemented
        LOG.warn(methodName + " - Jakarta Security auth not yet implemented, rejecting");
        return INVALID_RESULT;
    } // end method

    @Override
    public int priority() {
        return 70;
    } // end method

    @Override
    public Set<ValidationType> validationTypes() {
        return null;
    } // end method

} // end class
