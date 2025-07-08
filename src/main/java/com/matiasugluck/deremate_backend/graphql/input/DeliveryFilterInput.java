package com.matiasugluck.deremate_backend.graphql.input;

import com.matiasugluck.deremate_backend.enums.DeliveryStatus;

// Puede ser una clase o un record
public record DeliveryFilterInput(
        DeliveryStatus status,
        Long assignedToUserId,
        Boolean isAssigned
) {}
