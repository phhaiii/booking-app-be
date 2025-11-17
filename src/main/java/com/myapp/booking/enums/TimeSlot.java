package com.myapp.booking.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.Arrays;

/**
 * Enum representing the 4 fixed time slots available for booking
 */
@Getter
@RequiredArgsConstructor
public enum TimeSlot {
    SLOT_10_12(0, LocalTime.of(10, 0), LocalTime.of(12, 0), "10:00 - 12:00"),
    SLOT_12_14(1, LocalTime.of(12, 0), LocalTime.of(14, 0), "12:00 - 14:00"),
    SLOT_14_16(2, LocalTime.of(14, 0), LocalTime.of(16, 0), "14:00 - 16:00"),
    SLOT_16_18(3, LocalTime.of(16, 0), LocalTime.of(18, 0), "16:00 - 18:00");

    private final int index;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String displayText;

    /**
     * Get TimeSlot by index
     */
    public static TimeSlot fromIndex(int index) {
        return Arrays.stream(values())
                .filter(slot -> slot.getIndex() == index)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid slot index: " + index));
    }

    /**
     * Get TimeSlot by start time
     */
    public static TimeSlot fromStartTime(LocalTime startTime) {
        return Arrays.stream(values())
                .filter(slot -> slot.getStartTime().equals(startTime))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid start time: " + startTime));
    }

    /**
     * Check if a given time falls within this slot
     */
    public boolean containsTime(LocalTime time) {
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }

    /**
     * Get all available time slots
     */
    public static TimeSlot[] getAllSlots() {
        return values();
    }

    /**
     * Get total number of available slots
     */
    public static int getTotalSlots() {
        return values().length;
    }
}
