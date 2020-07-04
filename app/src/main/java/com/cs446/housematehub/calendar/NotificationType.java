package com.cs446.housematehub.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum NotificationType {
    NO_NOTIFICATION("No Notification"),
    FIVE_MIN("5 Minutes"),
    THIRTY_MIN("30 Minutes"),
    ONE_HOUR("1 Hour"),
    ONE_DAY("1 Day");

    private final String string;
    private static final Map<String, NotificationType> map = new HashMap<>(values().length, 1);

    static {
        for (NotificationType n : values()) map.put(n.string, n);
    }

    public String getString() {
        return this.string;
    }

    public static NotificationType getEnum(int i) {
        return NotificationType.values()[i];
    }
    public static NotificationType getEnum(String s) {
        NotificationType n = map.get(s);
        if (n == null) {
            throw new IllegalArgumentException("Invalid NotificationType name: " + s);
        }
        return n;
    }

    public static List<String> getValues() {
        List<String> vals = new ArrayList<>();
        for (NotificationType n : NotificationType.values()) {
            vals.add(n.getString());
        }
        return vals;
    }

    NotificationType(String string) { this.string = string; }
}
