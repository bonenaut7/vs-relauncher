package by.bonenaut7.vsrelauncher.notification;

public final class NotificationsStub implements Notifications {
	private String appTitle;
	
	@Override
	public void init(String appTitle) {
		this.appTitle = appTitle;
	}

	@Override
	public void show(NotificationType type, String text) {
		System.out.printf("[%s] %s%s", appTitle, text, System.lineSeparator());
	}
	
	@Override
	public void show(NotificationType type, int expirationTimeMs, String text) {
		show(type, text);
	}
}
