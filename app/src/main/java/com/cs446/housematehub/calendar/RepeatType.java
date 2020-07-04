package com.cs446.housematehub.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RepeatType {
    NO_REPEAT("Does not repeat"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    BI_WEEKLY("Bi-Weekly"),
    MONTHLY("Monthly");

    private final String string;
    private static final Map<String, RepeatType> map = new HashMap<>(values().length, 1);

    static {
        for (RepeatType r : values()) map.put(r.string, r);
    }

    public String getString() {
        return this.string;
    }

    public static RepeatType getEnum(int i) {
        return RepeatType.values()[i];
    }
    public static RepeatType getEnum(String s) {
        RepeatType r = map.get(s);
        if (r == null) {
            throw new IllegalArgumentException("Invalid RepeatType name: " + s);
        }
        return r;
    }

    public static List<String> getValues() {
        List<String> vals = new ArrayList<>();
        for (RepeatType r : RepeatType.values()) {
            vals.add(r.getString());
        }
        return vals;
    }

    RepeatType(String string) { this.string = string; }
}
