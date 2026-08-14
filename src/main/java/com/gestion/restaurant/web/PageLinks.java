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
        return url(request, page, size, null);
    }

    public String sortUrl(HttpServletRequest request, String property) {
        String currentSort = request.getParameter("sort");
        String direction = currentSort != null && currentSort.equalsIgnoreCase(property + ",asc")
                ? "desc" : "asc";
        int size;
        try { size = Integer.parseInt(request.getParameter("size")); }
        catch (Exception ignored) { size = 10; }
        return url(request, 0, size, Map.of("sort", property + "," + direction));
    }

    private String url(HttpServletRequest request, int page, int size, Map<String, String> replacements) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(request.getRequestURI());
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("page".equals(key) || "size".equals(key)
                    || (replacements != null && replacements.containsKey(key))) {
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
        if (replacements != null) {
            replacements.forEach(builder::queryParam);
        }
        return builder.build().encode().toUriString();
    }
}
