package com.foodsaver.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReminderFrequency {
    EVERYDAY,
    EVERY_THREE_DAYS,
    ONCE_A_WEEK,
    EVERY_TWO_WEEKS,
    ONCE_A_MONTH,
    NEVER;
}
