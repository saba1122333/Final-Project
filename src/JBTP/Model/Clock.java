package JBTP.Model;

/**
 * Represents a chess clock with hours, minutes, and seconds.
 * Handles time tracking for each player in a chess game.
 */
public class Clock {
    private int hh; // hours
    private int mm; // minutes
    private int ss; // seconds

    /**
     * Creates a new chess clock with the specified time.
     *
     * @param hh Initial hours
     * @param mm Initial minutes
     * @param ss Initial seconds
     */
    public Clock(int hh, int mm, int ss) {
        this.hh = Math.max(0, hh);
        this.mm = Math.max(0, Math.min(59, mm));
        this.ss = Math.max(0, Math.min(59, ss));
    }

    /**
     * Checks if the clock has run out of time.
     *
     * @return True if the clock is at 0:00:00
     */
    public boolean outOfTime() {
        return (hh == 0 && mm == 0 && ss == 0);
    }

    /**
     * Decrements the clock by one second.
     * Maintains proper time format with cascading decrements.
     */
    public void decr() {
        if (outOfTime()) {
            return;
        }

        if (this.mm == 0 && this.ss == 0) {
            this.ss = 59;
            this.mm = 59;
            this.hh--;
        } else if (this.ss == 0) {
            this.ss = 59;
            this.mm--;
        } else {
            this.ss--;
        }
    }

    /**
     * Gets the current time as a formatted string.
     *
     * @return Time in "HH:MM:SS" format
     */
    public String getTime() {
        String fHrs = String.format("%02d", this.hh);
        String fMins = String.format("%02d", this.mm);
        String fSecs = String.format("%02d", this.ss);
        String fTime = fHrs + ":" + fMins + ":" + fSecs;
        return fTime;
    }

    /**
     * Gets the hours component of the remaining time.
     *
     * @return Hours remaining
     */
    public int getHours() {
        return hh;
    }

    /**
     * Gets the minutes component of the remaining time.
     *
     * @return Minutes remaining
     */
    public int getMinutes() {
        return mm;
    }

    /**
     * Gets the seconds component of the remaining time.
     *
     * @return Seconds remaining
     */
    public int getSeconds() {
        return ss;
    }

    /**
     * Adds time to the clock (useful for increment/delay time controls).
     *
     * @param additionalSeconds Seconds to add
     */
    public void addTime(int additionalSeconds) {
        int totalSeconds = toTotalSeconds() + additionalSeconds;
        fromTotalSeconds(totalSeconds);
    }

    /**
     * Converts the time to total seconds.
     *
     * @return Total seconds represented by the current time
     */
    private int toTotalSeconds() {
        return hh * 3600 + mm * 60 + ss;
    }

    /**
     * Sets the time from total seconds.
     *
     * @param totalSeconds Total seconds to convert to HH:MM:SS
     */
    private void fromTotalSeconds(int totalSeconds) {
        totalSeconds = Math.max(0, totalSeconds);
        hh = totalSeconds / 3600;
        totalSeconds %= 3600;
        mm = totalSeconds / 60;
        ss = totalSeconds % 60;
    }
}