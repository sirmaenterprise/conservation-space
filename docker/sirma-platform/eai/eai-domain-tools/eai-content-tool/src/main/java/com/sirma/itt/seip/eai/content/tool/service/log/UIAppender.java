package com.sirma.itt.seip.eai.content.tool.service.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * UI logger that prints message to provided console
 * 
 * @param <E>
 *            the event types
 * @author bbanchev
 */
public class UIAppender<E> extends OutputStreamAppender<E> {
	private static TextFlow console;

	/**
	 * Initialize the UI appender with UI control to append to
	 * 
	 * @param uiConsole
	 *            is the ui component to log to
	 */
	public static void init(TextFlow uiConsole) {
		UIAppender.console = uiConsole;
	}

	@Override
	public void start() {
		started = true;
	}

	@Override
	protected void subAppend(E event) {
		if (!isStarted() || console == null) {
			return;
		}
		if (event instanceof DeferredProcessingAware) {
			((DeferredProcessingAware) event).prepareForDeferredProcessing();
		}
		// we initialize color with default text color black
		Paint color = Color.BLACK;
		if (event instanceof ILoggingEvent) {
			Level level = ((ILoggingEvent) event).getLevel();
			if (level == Level.INFO || level == Level.WARN || level == Level.ERROR) {
				color = setLevelColor(level);
			} else {
				// skip trace and debug messages
				return;
			}
		}
		byte[] byteArray = this.encoder.encode(event);
		Text text = new Text();
		text.setText(new String(byteArray));
		text.setFill(color);
		// run in ui thread
		Platform.runLater(() -> console.getChildren().add(text));
	}

	private static Paint setLevelColor(Level level) {
		if (level == Level.DEBUG) {
			return Color.DODGERBLUE;
		} else if (level == Level.ERROR) {
			return Color.RED;
		} else if (level == Level.WARN) {
			return Color.ORANGE;
		} else if (level == Level.TRACE) {
			return Color.DARKGRAY;
		}
		return Color.BLACK;
	}
}
