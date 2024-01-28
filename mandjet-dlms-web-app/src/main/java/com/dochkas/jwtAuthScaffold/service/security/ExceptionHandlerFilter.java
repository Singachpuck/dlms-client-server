package com.dochkas.jwtAuthScaffold.service.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dochkas.jwtAuthScaffold.model.message.DefaultErrorMessage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BadCredentialsException e) {
            DefaultErrorMessage errorResponse = new DefaultErrorMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase(), e.getMessage());
            response.setHeader("Content-Type", "application/json");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(convertObjectToJson(errorResponse));
        } catch (RuntimeException e) {
            DefaultErrorMessage errorResponse = new DefaultErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getMessage());
            response.setHeader("Content-Type", "application/json");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write(convertObjectToJson(errorResponse));
        }
    }

    public String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}