package com.sirma.itt.seip.eai.content.tool;

import static com.sirma.itt.seip.eai.content.tool.params.ParametersProvider.PARAM_CONTENT_URI;
import static com.sirma.itt.seip.eai.content.tool.params.ParametersProvider.get;

import java.io.File;

import com.sirma.itt.seip.eai.content.tool.params.ParametersProvider;
import com.sirma.itt.seip.eai.content.tool.service.RuntimeSettings;
import com.sirma.itt.seip.eai.content.tool.service.io.LocalFileService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * {@link Main} is the main entry point for the application. Configures and starts the JavaFx application.
 * 
 * @author bbanchev
 */
public class Main extends Application {

	/**
	 * The application entry point
	 * 
	 * @param args
	 *            are the starting arguments
	 */
	public static void main(String[] args) {
		Application.launch(Main.class, args);
	}

	@Override
	public void stop() throws Exception {
		// store the model after app has exit
		RuntimeSettings.INSTANCE.storeModel();
		Platform.exit();
		// needed to terminate all threads and to free any allocated browser/local resources
		System.exit(0);// NOSONAR
	}

	@Override
	public void start(final Stage stage) throws Exception {
		initialize();
		final Parent rootGroup = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/eai_main.fxml"));
		Scene scene = new Scene(rootGroup, Color.AQUA);
		scene.getStylesheets().add("styles/application.css");
		stage.setScene(scene);
		stage.setTitle("Content import tool. Instance: " + get(PARAM_CONTENT_URI));
		stage.getIcons().add(new Image(Main.class.getResourceAsStream("logo.png")));
		stage.show();
	}

	private void initialize() {
		LocalFileService.init(new File(System.getProperty("user.home")));
		ParametersProvider.setParameters(this.getParameters());
	}

}
