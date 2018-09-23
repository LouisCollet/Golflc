/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package custom_validations;

/**
 *
 * @author collet
 */
import java.util.HashMap;
import java.util.Map;
import javax.validation.metadata.ConstraintDescriptor;
import org.primefaces.validate.bean.ClientValidationConstraint;
 
/**
 
 * ClientValidationConstraint for @Email annotation
 
 */
 
public class EmailClientValidationConstraint implements ClientValidationConstraint {

    public static final String MESSAGE_METADATA = "data-p-email-msg";
 
    @Override
    public Map<String, Object> getMetadata(ConstraintDescriptor constraintDescriptor) {
        Map<String,Object> metadata = new HashMap<>();
        Map attrs = constraintDescriptor.getAttributes();
        Object message = attrs.get("message");    
        if(message != null) {
            metadata.put(MESSAGE_METADATA, message);
        }
        
        return metadata;
    }
    @Override
    public String getValidatorId() {
        return Email.class.getSimpleName();
 
    }
}