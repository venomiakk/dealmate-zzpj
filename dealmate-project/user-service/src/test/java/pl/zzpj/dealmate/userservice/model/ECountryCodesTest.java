package pl.zzpj.dealmate.userservice.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;


class ECountryCodesTest {

    @Test
    void shouldHaveValuesAndBeLoadable() {
        // GIVEN & WHEN
        ECountryCodes[] allCodes = ECountryCodes.values();

        // THEN
        assertThat(allCodes).isNotEmpty();
    }

    @Test
    void shouldAllowFindingEnumByStringName() {
        // GIVEN
        String validCode = "PL";

        // WHEN & THEN
        assertThatCode(() -> {
            ECountryCodes country = ECountryCodes.valueOf(validCode);
            assertThat(country).isEqualTo(ECountryCodes.PL);
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionForInvalidName() {
        // GIVEN
        String invalidCode = "XX";

        // WHEN & THEN
        assertThatThrownBy(() -> ECountryCodes.valueOf(invalidCode))
                .isInstanceOf(IllegalArgumentException.class);
    }
}