package com.cs446.housematehub.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EventType {
    ERRAND("Errand"),
    RESERVE_AMENITY("Reserve Amenity"),
    CHORES("Chores"),
    GROUP_ACTIVITY("Group Activity");

    private final String string;
    private static final Map<String, EventType> map = new HashMap<>(values().length, 1);

    static {
        for (EventType e : values()) map.put(e.string, e);
    }

    public String getString() {
        return this.string;
    }

    public static EventType getEnum(int i) {
        return EventType.values()[i];
    }
    public static EventType getEnum(String s) {
        EventType e = map.get(s);
        if (e == null) {
            throw new IllegalArgumentException("Invalid EventType name: " + s);
        }
        return e;
    }

    public static List<String> getValues() {
        List<String> vals = new ArrayList<>();
        for (EventType e : EventType.values()) {
            vals.add(e.getString());
        }
        return vals;
    }

    EventType(String string) { this.string = string; }
}
