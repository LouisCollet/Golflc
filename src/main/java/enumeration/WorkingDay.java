package enumeration;

import java.time.DayOfWeek;
import java.util.EnumSet;

public enum WorkingDay {
    MONDAY   (DayOfWeek.MONDAY,    1),
    TUESDAY  (DayOfWeek.TUESDAY,   2),
    WEDNESDAY(DayOfWeek.WEDNESDAY, 4),
    THURSDAY (DayOfWeek.THURSDAY,  8),
    FRIDAY   (DayOfWeek.FRIDAY,   16),
    SATURDAY (DayOfWeek.SATURDAY, 32),
    SUNDAY   (DayOfWeek.SUNDAY,   64);

    private final DayOfWeek dayOfWeek;
    private final int mask;

    WorkingDay(DayOfWeek dayOfWeek, int mask) {
        this.dayOfWeek = dayOfWeek;
        this.mask = mask;
    }

    public int mask() {
        return mask;
    }

    public DayOfWeek dayOfWeek() {
        return dayOfWeek;
    }

    /** FullCalendar day index: Sunday=0, Monday=1, ..., Saturday=6
     * @return  */
    public int fullCalendarIndex() {
        return dayOfWeek.getValue() % 7; // ISO: Mon=1..Sun=7 → Mon=1..Sat=6, Sun=0
    }

    public static WorkingDay from(DayOfWeek dow) {
        for (WorkingDay d : values()) {
            if (d.dayOfWeek == dow) {
                return d;
            }
        }
        throw new IllegalArgumentException("Unknown day: " + dow);
    }
    
    /** Retourne la clé du bundle msg pour ce jour — ex: "tarif.days.monday"
     * @return  */
    public String getLabelKey() {
        return "tarif.days." + name().toLowerCase();
    } // end method

    /** Property accessor pour EL — Enum.name() final non accessible via #{day.name()}
     * @return  */
    public String getName() {
        return name();
    } // end method

     public String getLabel() {
        return switch (this) {
            case MONDAY -> "Lundi";
            case TUESDAY -> "Mardi";
            case WEDNESDAY -> "Mercredi";
            case THURSDAY -> "Jeudi";
            case FRIDAY -> "Vendredi";
            case SATURDAY -> "Samedi";
            case SUNDAY -> "Dimanche";
            default -> name();
        };
    }
    // --- Bitmask utilities (moved from schedule.WorkScheduleUtils) ---

    public static int toMask(EnumSet<WorkingDay> workingDays) {
        int mask = 0;
        for (WorkingDay d : workingDays) { mask |= d.mask(); }
        return mask;
    } // end method

    public static EnumSet<WorkingDay> fromMask(int mask) {
        EnumSet<WorkingDay> set = EnumSet.noneOf(WorkingDay.class);
        for (WorkingDay d : WorkingDay.values()) {
            if ((mask & d.mask()) != 0) { set.add(d); }
        }
        return set;
    } // end method

    private static String printBits(int mask) {
        return String.format("%7s", Integer.toBinaryString(mask)).replace(' ', '0');
    } // end method

    public static String printWorkingDays(int mask) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mask binaire : ").append(printBits(mask)).append("\n");
        for (WorkingDay day : WorkingDay.values()) {
            if ((mask & day.mask()) != 0) { sb.append(day.name()).append(" activé").append("\n"); }
        }
        return sb.toString();
    } // end method

    public static String printWorkingDaysLine(int mask) {
        StringBuilder sb = new StringBuilder();
        for (WorkingDay day : WorkingDay.values()) {
            if ((mask & day.mask()) != 0) {
                if (sb.length() > 0) { sb.append(", "); }
                sb.append(day.getLabel());
            }
        }
        return sb.length() > 0 ? sb.toString() : "Aucun jour";
    } // end method

} // end class
