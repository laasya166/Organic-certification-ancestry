package com.soa.common.dto;

/**
 * CONTRACT: Lot validity response from certificate-service.
 * GET /certificates/validity?lotIds=A,B,C&on=YYYY-MM-DD
 */
public record LotValidityResponse(
        String lotId,
        boolean valid,
        String reason
) {}
