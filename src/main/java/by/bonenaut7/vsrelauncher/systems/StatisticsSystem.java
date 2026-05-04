/**
 *  Copyright 2026 Matvey "bonenaut7" Zholudz
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package by.bonenaut7.vsrelauncher.systems;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import by.bonenaut7.vsrelauncher.AppContext;
import by.bonenaut7.vsrelauncher.GameState;
import by.bonenaut7.vsrelauncher.events.EventGameStateChange;
import by.bonenaut7.vsrelauncher.util.Utils;

public final class StatisticsSystem extends AbstractSystem {
	private volatile Instant timestampQueueJoin = null;
	private volatile Instant timestampGameJoin = null;
	
	public StatisticsSystem(AppContext ctx) {
		super(ctx);
	}

	@Override
	public void init() {
		bus.register(EventGameStateChange.class, this::onStateChange);
	}
	
	@Override
	public void shutdown() {
		addStatisticQueueTime(timestampQueueJoin, Instant.now());
		timestampQueueJoin = null;
		
		addStatisticPlayTime(timestampGameJoin, Instant.now());
		timestampGameJoin = null;
	}

	private void onStateChange(EventGameStateChange event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (event.getPreviousState() == event.getNewState()) {
			return;
		}
		
		if (event.getNewState() == GameState.IN_GAME) {
			timestampGameJoin = Instant.now();
		}
		
		switch (event.getNewState()) {
			case IN_MENU:
				switch (event.getPreviousState()) {
					case IN_QUEUE:
						addStatisticQueueTime(timestampQueueJoin, Instant.now());
						timestampQueueJoin = null;
						break;
						
					case IN_GAME:
						addStatisticPlayTime(timestampGameJoin, Instant.now());
						timestampGameJoin = null;
						break;
				}
				break;

			case IN_QUEUE:
				timestampQueueJoin = Instant.now();
				break;
				
			case IN_GAME:
				timestampGameJoin = Instant.now();
				break;
		}
	}
	
	private void addStatisticPlayTime(Instant from, Instant to) {
		if (from == null || to == null) {
			return;
		}
		
		ctx.config.stats_playTime += Utils.durationBetween(from, to, ChronoUnit.SECONDS);
		ctx.config.save();
	}
	
	private void addStatisticQueueTime(Instant from, Instant to) {
		if (from == null || to == null) {
			return;
		}
		
		ctx.config.stats_queueTime += Utils.durationBetween(from, to, ChronoUnit.SECONDS);
		ctx.config.save();
	}
}