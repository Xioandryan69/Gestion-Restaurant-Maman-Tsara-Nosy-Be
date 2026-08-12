package com.gestion.restaurant.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ThymeleafModelAttributes {

    @ModelAttribute("request")
    public HttpServletRequest httpServletRequest(HttpServletRequest request) {
        return request;
    }
}
