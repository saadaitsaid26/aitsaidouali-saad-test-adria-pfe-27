// DTO de lecture du solde d'un compte (P4)
package com.bank.account.service.dto;

import java.util.UUID;

/** Vue compacte du solde : GET /api/accounts/{id}/balance. */
public record BalanceResponse(
        UUID accountId,
        String status,
        double balance
) {
}
