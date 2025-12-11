
package interfaces;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.primefaces.validate.bean.ClientValidationConstraint;

/**
 *
 * @author Collet
 */


public class ValidContactClientConstraint implements ClientValidationConstraint {
   public static final String MESSAGE_METADATA = "data-p-contact-msg";
 //  @SuppressWarnings("all")
   @Override
   public Map<String, Object> getMetadata(ConstraintDescriptor<?> constraintDescriptor) {
      Map<String, Object> metadata = new HashMap<>();
      Map<String,Object> attrs = constraintDescriptor.getAttributes();
      Object message = attrs.get("message");
      if (message != null) {
         metadata.put(MESSAGE_METADATA, message);
      }
      return metadata;
   }
   @Override
   public String getValidatorId() {
      return ValidContact.class.getSimpleName();
   }
}