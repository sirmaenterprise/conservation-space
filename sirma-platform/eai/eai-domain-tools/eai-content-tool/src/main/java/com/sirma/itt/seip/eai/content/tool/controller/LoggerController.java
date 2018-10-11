package com.sirma.itt.seip.eai.content.tool.controller;

import com.sirma.itt.seip.eai.content.tool.service.log.UIAppender;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * {@link LoggerController} is controller to handle UI console log messages. It initialize the {@link UIAppender}
 * 
 * @author bbanchev
 */
public class LoggerController {

	@FXML
	private TextFlow console;

	/**
	 * Method called by the FXMLLoader when initialization is complete
	 */
	@FXML
	void initialize() {
		UIAppender.init(console);
		final ContextMenu contextMenu = new ContextMenu();
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		MenuItem copy = new MenuItem("Copy All");
		copy.setOnAction(e -> copyText(clipboard));
		contextMenu.getItems().add(copy);

		MenuItem clearMenu = new MenuItem("Clear");
		clearMenu.setOnAction(e -> console.getChildren().clear());
		contextMenu.getItems().add(clearMenu);
		console.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				contextMenu.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
			}
		});
	}

	private void copyText(Clipboard clipboard) {
		final ClipboardContent content = new ClipboardContent();
		ObservableList<Node> children = console.getChildren();
		StringBuilder copy = new StringBuilder();
		for (Node node : children) {
			copy.append(((Text) node).getText());
		}
		content.putString(copy.toString());
		clipboard.setContent(content);
	}

}
