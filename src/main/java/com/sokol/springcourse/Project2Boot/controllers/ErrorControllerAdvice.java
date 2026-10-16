package com.sokol.springcourse.Project2Boot.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

/** Renders controlled failures. */
@ControllerAdvice
public class ErrorControllerAdvice {
    /** Preserves domain status. */
    @ExceptionHandler(ResponseStatusException.class)
    public String domainError(ResponseStatusException exception, HttpServletResponse response, Model model) {
        int status = exception.getStatusCode().value();
        response.setStatus(status);
        model.addAttribute("status", status);
        model.addAttribute("error", HttpStatus.valueOf(status).getReasonPhrase());
        model.addAttribute("guidance", exception.getReason());
        return "error";
    }

    /** Hides database details. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String integrityError(HttpServletResponse response, Model model) {
        response.setStatus(HttpStatus.CONFLICT.value());
        model.addAttribute("status", HttpStatus.CONFLICT.value());
        model.addAttribute("error", "Conflict");
        model.addAttribute("guidance", "This change conflicts with an existing reader or loan");
        return "error";
    }
}
