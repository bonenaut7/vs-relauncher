package by.bonenaut7.vsrelauncher.util;

public enum Platform {
	WINDOWS,
	LINUX,
	FREEBSD,
	MACOSX,
	UNKNOWN;
	
	private static final Platform platform;
	
	static {
		platform = switch (System.getProperty("os.name")) {
			case "Windows" -> WINDOWS;
			case "FreeBSD" -> FREEBSD;
			case "Linux", "SunOS", "Unix" -> LINUX;
			case "Mac OS X", "Darwin" -> MACOSX;
			default -> UNKNOWN;
		};
	}
	
	public static Platform get() {
		return platform;
	}
}
