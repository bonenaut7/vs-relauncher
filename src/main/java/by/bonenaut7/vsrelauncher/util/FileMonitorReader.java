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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class FileMonitorReader extends AbstractScheduledTask {
	private static final String LINE_SEPARATOR = System.lineSeparator();
	private static final int LINE_SEPARATOR_LENGTH = LINE_SEPARATOR.length();
	
	private final Consumer<List<String>> initReadCallback;
	private final Consumer<String> newReadsCallback;
	private final ByteBuffer readBuffer;
	private final CharBuffer charBuffer;
	
	private volatile Path path;
	private volatile CharsetDecoder decoder;
	private volatile StringBuilder pendingString;
	
	public FileMonitorReader(String name, long delayMs, Consumer<List<String>> initReadCallback, Consumer<String> newReadsCallback, int bufferSize) {
		super(name, delayMs);
		this.task = this::run;
		this.initReadCallback = initReadCallback;
		this.newReadsCallback = newReadsCallback;
		this.readBuffer = ByteBuffer.allocate(bufferSize);
		this.charBuffer = CharBuffer.allocate(bufferSize);
		
		this.decoder = StandardCharsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
		this.pendingString = new StringBuilder();
	}
	
	public boolean start(Path filePath) {
		if (canStart() && filePath.toFile().exists()) {
			path = filePath;
			
			return start();
		}

		return false;
	}
	
	public boolean stop() {
		if (super.stop()) {
			path = null;
			readBuffer.clear();
			charBuffer.clear();
			
			if (pendingString.length() > 0) {
				pendingString.delete(0, pendingString.length() - 1);
			}
			
			
			return true;
		}
		
		return false;
	}
	
	public void restart(Path filePath) {
		stop();
		start(filePath);
	}
	
	// ASYNC!
	private void run() {
		try {
			final FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
			
			// Read whole file if possible
			if (initReadCallback != null) {
				readFileAtInit(channel);
			} else {
				channel.position(channel.size());
			}
			
			while (running) {
				int readBytes = channel.read(readBuffer);
				if (readBytes < 1) {
					Utils.sleep(delayMs);
					continue;
				}
				
				// Prepaere read buffer
				readBuffer.flip();
				
				// Decode
				// TODO check for decode success
				final CoderResult result = decoder.decode(readBuffer, charBuffer, false);
				charBuffer.flip();
				
				// Append stuff we read
				pendingString.append(charBuffer.toString());
				
				// Ehh... Do some weird shit to make it split in lines... yeah...
				int separatorIndex = 0;
				while ((separatorIndex = pendingString.indexOf(LINE_SEPARATOR)) != -1) {
					final String line = pendingString.substring(0, separatorIndex);
					pendingString.delete(0, separatorIndex + LINE_SEPARATOR_LENGTH);
					
					// Send our line we read to the callback
					newReadsCallback.accept(line);
				}
				
				// Clear buffers
				readBuffer.clear();
				charBuffer.clear();
			}
			
			channel.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	private void readFileAtInit(FileChannel channel) throws IOException {
		final List<String> readMessages = new ArrayList<>();
		final int bufferCapacity = readBuffer.capacity();
		long bytesToRead = channel.size();
		
		while (bytesToRead > 0) {
			final int readLimit = Math.min(bufferCapacity, (int)Math.max(bytesToRead, Integer.MAX_VALUE));
			readBuffer.limit(readLimit);
			charBuffer.limit(readLimit);
			
			int readBytes = channel.read(readBuffer);
			bytesToRead -= readBytes;
			
			readBuffer.flip();
			
			// TODO check for decode success
			final CoderResult result = decoder.decode(readBuffer, charBuffer, false);
			charBuffer.flip();
			pendingString.append(charBuffer.toString());
			
			int separatorIndex = 0;
			while ((separatorIndex = pendingString.indexOf(LINE_SEPARATOR)) != -1) {
				final String line = pendingString.substring(0, separatorIndex);
				pendingString.delete(0, separatorIndex + LINE_SEPARATOR_LENGTH);
				
				readMessages.add(line);
			}
			
			readBuffer.clear();
			charBuffer.clear();
		}
		
		initReadCallback.accept(readMessages);
	}
}
