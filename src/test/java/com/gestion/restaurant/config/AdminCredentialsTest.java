package com.gestion.restaurant.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminCredentialsTest {

    @Test
    void encodeForStorage_plain_devientBcrypt() {
        String encoded = AdminCredentials.encodeForStorage("MotDePasseFort12");
        assertThat(encoded).startsWith("{bcrypt}");
        assertThat(PasswordEncoderFactories.createDelegatingPasswordEncoder()
                .matches("MotDePasseFort12", encoded)).isTrue();
    }

    @Test
    void encodeForStorage_conservePrefixExistant() {
        assertThat(AdminCredentials.encodeForStorage("{noop}test")).isEqualTo("{noop}test");
    }

    @Test
    void assertProductionPasswordAcceptable_refuseFaible() {
        assertThatThrownBy(() -> AdminCredentials.assertProductionPasswordAcceptable("changeme"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> AdminCredentials.assertProductionPasswordAcceptable("{noop}changeme"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> AdminCredentials.assertProductionPasswordAcceptable("court"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void assertProductionPasswordAcceptable_accepteFort() {
        AdminCredentials.assertProductionPasswordAcceptable("RestaurantNosyBe2026!");
        String bcrypt = "{bcrypt}" + new BCryptPasswordEncoder().encode("RestaurantNosyBe2026!");
        AdminCredentials.assertProductionPasswordAcceptable(bcrypt);
    }
}
