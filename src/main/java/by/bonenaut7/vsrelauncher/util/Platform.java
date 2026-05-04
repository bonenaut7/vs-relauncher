package by.bonenaut7.vsrelauncher.util;

public enum Platform {
	WINDOWS,
	LINUX,
	FREEBSD,
	MACOSX,
	UNKNOWN;
	
	private static final Platform platform;
	
	static {
		final String osName = System.getProperty("os.name");
		
        if (osName.startsWith("Windows")) {
        	platform = WINDOWS;
        } else if (osName.startsWith("FreeBSD")) {
        	platform = FREEBSD;
        } else if (osName.startsWith("Linux") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
        	platform = LINUX;
        } else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
        	platform = MACOSX;
        } else {
        	platform = UNKNOWN;
        }
	}
	
	public static Platform get() {
		return platform;
	}
}
