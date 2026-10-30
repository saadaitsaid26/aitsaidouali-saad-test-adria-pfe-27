// DTO de réponse d'un compte (P4)
package com.bank.account.service.dto;

import java.util.UUID;

/** Vue de lecture d'un compte renvoyée par toutes les couches web (REST/GraphQL/MCP). */
public record AccountResponse(
        UUID id,
        String customerReference,
        String status,
        String currency,
        double balance
) {
}
