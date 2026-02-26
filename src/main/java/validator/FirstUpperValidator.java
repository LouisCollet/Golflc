package validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FirstUpperValidator implements ConstraintValidator<FirstUpperConstraint, String> {

    @Override
    public void initialize(FirstUpperConstraint constraintAnnotation) {
        // max supprimé — @Size gère la longueur
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;                                // null/empty considéré valide
        }

        if (Character.isUpperCase(value.charAt(0))) {
            return true;                                // ✅ première lettre majuscule
        }

        // Message dynamique — uniquement pour la majuscule
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                "Le nom doit commencer par une majuscule")
               .addConstraintViolation();
        return false;
    }
}