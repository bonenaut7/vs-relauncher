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
package by.bonenaut7.vsrelauncher.util;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class Utils {
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException exception) {
			// NO-OP
		}
	}
	
	public static void sneakyThrows(ExceptionalRunnable runnable) {
		sneakyThrows(runnable, null);
	}
	
	public static <T> T sneakyCallable(Callable<T> callable) {
		return sneakyCallable(callable, null);
	}
	
	public static void sneakyThrows(ExceptionalRunnable runnable, Consumer<Exception> exceptionHandler) {
		try {
			runnable.run();
		} catch (Exception exception) {
			if (exceptionHandler != null) {
				exceptionHandler.accept(exception);
			}
		}
	}
	
	public static <T> T sneakyCallable(Callable<T> callable, Consumer<Exception> exceptionHandler) {
		try {
			return callable.call();
		} catch (Exception exception) {
			if (exceptionHandler != null) {
				exceptionHandler.accept(exception);
			}
			
			return null;
		}
	}
	
	public static boolean isLongNumber(String argument) {
		try {
			Long.valueOf(argument);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}
	
	public static String formatTimeMinutes(long minutes) {
		return formatTime(minutes * 60);
	}
	
	public static String formatTime(long secondsCount) {
		final long days = secondsCount / 86400;
		final long hours = secondsCount % 86400 / 3600;
		final long minutes = secondsCount % 3600 / 60;
		final long seconds = secondsCount % 60;
		
		String str = "";
		if (days > 0) {
			str += days + (days == 1 ? " day " : " days ");
		}
		
		if (hours > 0) {
			str += hours + (hours == 1 ? " hour " : " hours ");
		}
		
		if (minutes > 0) {
			str += minutes + " min. ";
		}
		
		if (seconds > 0) {
			str += seconds + " sec. ";
		}
		
		if (str.length() == 0) {
			str = "None";
		}
		
		return str.trim();
	}
	
	public static long durationBetween(Instant first, Instant second, TemporalUnit unit) {
		return first.isBefore(second) ? first.until(second, unit) : second.until(first, unit);
	}
	
	public static boolean hasPassed(Instant instant, long value, TemporalUnit unit) {
		return instant != null && instant.until(Instant.now(), unit) >= value;
	}
	
	@FunctionalInterface
	public static interface ExceptionalRunnable {
		void run() throws Exception;
	}
}
