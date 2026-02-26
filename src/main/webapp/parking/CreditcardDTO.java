package dto;

public class CreditcardDTO {
    public record Creditcard(
            Double totalPrice,
            Integer creditCardIdPlayer,
            String creditcardHolde,
            String creditcardNumber,
            String communication,
            String creditCardExpirationDate,  // nom imposé par Amazone Inc!!
            Short creditcardVerificationCode, 
            String creditcardType, // input from end user
            String creditcardIssuer,  // calculated for validation equality with creditCardType
            String creditcardCurrency 

// pas de virgule au dernier
    ) {}
} //end class