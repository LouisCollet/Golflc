package entite;

import java.io.Serializable;
import javax.inject.Named;
import javax.validation.constraints.*;
/**
 *
 * @author collet
 */
//  enlevé 04/05/2014  @javax.enterprise.context.SessionScoped   // added 05/10/2013 change quelque chose ???

@Named
public class Login implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;

@NotNull
    @Size(min = 3, max = 15, message = "Username must be between {min} and {max} characters")
    private String username;

    @NotNull(message="{password.notnull}")
    @Size(min = 5, max = 50, message = "Password must be between {min} and {max} characters")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        LOG.info("get password = " + password);
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        LOG.info("set password = " + this.getPassword());
    }

} // end class
