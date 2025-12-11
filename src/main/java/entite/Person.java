
package entite;

import java.time.LocalDate;

public record Person(
    Long id,
    String email,
    LocalDate dateOfBirth
) {}

