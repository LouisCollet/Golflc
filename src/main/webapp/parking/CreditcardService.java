
package dto.service;
import dto.CreditcardDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import entite.Creditcard;

@ApplicationScoped
public class CreditcardService {

    @Inject
        dto.mapper.CreditcardMapperDTO mapper;
/*
    public CreditcardDTO convertToDto(Creditcard entity) {
        return mapper.toDto(entity);
    }

    public Creditcard convertToEntity(CreditcardDTO dto) {
        return mapper.toEntity(dto);
    }
*/
} // end class