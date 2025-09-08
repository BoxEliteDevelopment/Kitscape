package tiie.kitscape.utils;

import java.util.Map;

public class timeformat {

    private long computeMillis(Map<String,Long> cd) {
        long total = 0;
        for (var e : cd.entrySet()) {
            switch (e.getKey()) {
                case "seconds":     total += e.getValue() * 1000; break;
                case "minutes":     total += e.getValue() * 60 * 1000; break;
                case "hours":       total += e.getValue() * 3600 * 1000; break;
                case "days":        total += e.getValue() * 86400 * 1000; break;
                case "months":      total += e.getValue() * 30 * 86400 * 1000; break;
                case "years":       total += e.getValue() * 365 * 86400 * 1000; break;
                case "oneTimeUse":  if (e.getValue() > 0) return Long.MAX_VALUE;
            }
        }
        return total;
    }

    /** Format ms into "[Hh ][Mm ]Ss" */
    private String formatRemaining(long ms) {
        long totalSeconds = ms / 1000;
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0)   sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }
}
