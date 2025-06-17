package pl.zzpj.dealmate.userservice.dto;

import pl.zzpj.dealmate.userservice.model.ECountryCodes;

public record UpdateUserRequest(
        String username,
        String firstName,
        String lastName,
        ECountryCodes countryCode
) {
}
