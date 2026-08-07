package com.gestion.restaurant.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Construit des URLs de pagination en conservant les paramètres de filtre courants.
 */
@Component("pageLinks")
public class PageLinks {

    public String url(HttpServletRequest request, int page, int size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(request.getRequestURI());
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("page".equals(key) || "size".equals(key)) {
                continue;
            }
            String[] values = entry.getValue();
            if (values == null) {
                continue;
            }
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    builder.queryParam(key, value);
                }
            }
        }
        builder.queryParam("page", Math.max(page, 0));
        builder.queryParam("size", Math.max(size, 1));
        return builder.build().encode().toUriString();
    }
}
