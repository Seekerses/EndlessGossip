package io.seekerses.endlessgossip.util;

public class DayTime {
	private final int hour;
	private final int minute;
	private final long tick;

	public DayTime(long daytime) {
		this.tick = daytime % 24000;
		this.hour = (int) ((tick / 1000 + 6) % 24);
		this.minute = (int) ((tick % 1000) * 60 / 1000);
	}

	public static DayTime of(long daytime) {
		return new DayTime(daytime);
	}

	public boolean between(int startHour, int startMinute, int endHour, int endMinute) {
		int currentTime = hour * 100 + minute;
		return startHour * 100 + startMinute <= currentTime
			   && endHour * 100 + endMinute >= currentTime;
	}

	public boolean after(DayTime other) {
		return hour * 100 + minute > other.hour * 100 + minute;
	}

	public boolean isOdd() {
		return hour % 2 == 0;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public long getTick() {
		return tick;
	}
}
