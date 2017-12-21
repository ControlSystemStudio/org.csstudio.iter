/**
 *  Copyright (c) 2015-2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.tm4e.core.logger;

/**
 * Abstract class for logger.
 * 
 */
public abstract class AbstractLogger implements ILogger {

	private boolean enabled;

	@Override
	public void log(String message) {
		log(message, null, LogLevel.INFO);
	}

	@Override
	public void log(String message, Throwable exception) {
		log(message, exception, LogLevel.ERROR);
	}

	@Override
	public void log(String message, Throwable exception, LogLevel level) {
		if (!isEnabled()) {
			// Ignore log
			return;
		}
		switch (level) {
		case INFO:
			logInfo(message);
			break;
		case WARN:
			logWarn(message, exception);
			break;
		case ERROR:
			logError(message, exception);
			break;
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Log message as information.
	 * 
	 * @param message
	 */
	protected abstract void logInfo(String message);

	/**
	 * Log message as warning.
	 * 
	 * @param message
	 * @param exception
	 */
	protected abstract void logWarn(String message, Throwable exception);

	/**
	 * Log message as error.
	 * 
	 * @param message
	 * @param exception
	 */
	protected abstract void logError(String message, Throwable exception);
}
