package com.sirma.itt.seip.eai.content.tool.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * {@link ProgressController} is controller to handle UI progress and UI progress messages
 *
 * @author bbanchev
 */
public class ProgressController {
	private static ProgressController progressController;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label progressInfo;

	@FXML
	void initialize() {
		// initialize the static access
		ProgressController.progressController = this; // NOSONAR
	}

	/**
	 * Gets the progress bar
	 *
	 * @return the progress bar
	 */
	public static ProgressBar getProgressBar() {
		if (progressController == null) {
			return null;
		}
		return progressController.progressBar;
	}

	/**
	 * Gets the progress info holder
	 *
	 * @return the progress info
	 */
	public static Label getProgressInfo() {
		if (progressController == null) {
			return null;
		}
		return progressController.progressInfo;
	}

}
