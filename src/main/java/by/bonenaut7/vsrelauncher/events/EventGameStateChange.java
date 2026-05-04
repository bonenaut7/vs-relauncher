package by.bonenaut7.vsrelauncher.events;

import by.bonenaut7.uebus.Event;
import by.bonenaut7.vsrelauncher.GameState;

public final class EventGameStateChange extends Event {
	private final GameState previousState;
	private GameState newState;
	
	public EventGameStateChange(GameState previousState, GameState newState) {
		this.previousState = previousState;
		this.newState = newState;
	}
	
	public GameState getPreviousState() {
		return previousState;
	}
	
	public GameState getNewState() {
		return newState;
	}
	
	public void setNewState(GameState state) {
		newState = state;
	}
	
	@Override
	public boolean isCancellable() {
		return true;
	}
}
