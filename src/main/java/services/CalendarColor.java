package services;

import java.util.HashMap;
import java.util.Map;

public class CalendarColor {
    private static final Map<String, String> colorMap = new HashMap<>();

    static {
        colorMap.put("1", "violet");
        colorMap.put("2", "green");
        colorMap.put("3", "grape");
        colorMap.put("4", "flamingo");
        colorMap.put("5", "yellow");
        colorMap.put("6", "orange");
        colorMap.put("7", "blue");
        colorMap.put("8", "graphite");
        colorMap.put("9", "blueberry");
        colorMap.put("10", "basil");
        colorMap.put("11", "red");
    }

    public static String getNameById(String id) {

        return colorMap.getOrDefault(id, "unknown");
    }

    public static String getIdByName(String name) {

        for (Map.Entry<String, String> entry : colorMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
