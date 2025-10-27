package com.pagodirecto.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * PagoDirecto CRM/ERP System - Main Application
 *
 * Enterprise ERP/CRM system built with Clean/Hexagonal architecture
 * and Domain-Driven Design principles.
 *
 * @author PagoDirecto Development Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.pagodirecto")
@EnableJpaRepositories(basePackages = "com.pagodirecto")
@EntityScan(basePackages = "com.pagodirecto")
@EnableJpaAuditing
public class PagoDirectoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagoDirectoApplication.class, args);
    }
}
