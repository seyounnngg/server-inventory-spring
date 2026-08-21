package com.example.serverinventory.web.dto;

import com.example.serverinventory.domain.ServerStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ServerFormValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private ServerForm validForm() {
        ServerForm form = new ServerForm();
        form.setHostname("lab-server-01");
        form.setIpAddress("192.168.0.10");
        form.setPort(22);
        form.setStatus(ServerStatus.RUNNING);
        return form;
    }

    @Test
    void validFormShouldHaveNoViolations() {
        assertThat(validator.validate(validForm())).isEmpty();
    }

    @Test
    void blankHostnameShouldBeRejected() {
        ServerForm form = validForm();
        form.setHostname(" ");

        Set<ConstraintViolation<ServerForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("hostname"));
    }

    @Test
    void malformedIpAddressShouldBeRejected() {
        ServerForm form = validForm();
        form.setIpAddress("999.999.999.999");

        Set<ConstraintViolation<ServerForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("ipAddress"));
    }

    @Test
    void portOutOfRangeShouldBeRejected() {
        ServerForm form = validForm();
        form.setPort(70000);

        Set<ConstraintViolation<ServerForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("port"));
    }

    @Test
    void nullPortShouldBeAllowedBecausePortIsOptional() {
        ServerForm form = validForm();
        form.setPort(null);

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    void missingStatusShouldBeRejected() {
        ServerForm form = validForm();
        form.setStatus(null);

        Set<ConstraintViolation<ServerForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("status"));
    }
}
