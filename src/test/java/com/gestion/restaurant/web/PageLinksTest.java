package com.gestion.restaurant.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PageLinksTest {

    private final PageLinks pageLinks = new PageLinks();

    @Test
    void url_conserveFiltresEtRemplacePage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ingredients");
        request.setParameter("nom", "Riz");
        request.setParameter("page", "2");
        request.setParameter("size", "10");
        request.setParameter("idCategorie", "3");

        String url = pageLinks.url(request, 0, 20);

        assertThat(url).startsWith("/ingredients?");
        assertThat(url).contains("nom=Riz");
        assertThat(url).contains("idCategorie=3");
        assertThat(url).contains("page=0");
        assertThat(url).contains("size=20");
        assertThat(url).doesNotContain("page=2");
    }
}
