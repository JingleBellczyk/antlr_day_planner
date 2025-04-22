package services;

import java.util.HashMap;
import java.util.Map;

public class CalendarColor {
    private static final Map<String, String> colorMap = new HashMap<>();

    static {
        colorMap.put("1", "red");
        colorMap.put("2", "orange");
        colorMap.put("3", "banana");
        colorMap.put("4", "basil");
        colorMap.put("5", "sage");
        colorMap.put("6", "grape");
        colorMap.put("7", "flamingo");
        colorMap.put("8", "blueberry");
        colorMap.put("9", "peacock");
        colorMap.put("10", "graphite");
        colorMap.put("11", "lavender");
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
