package com.sirma.itt.seip.eai.content.tool.controller;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.eai.content.tool.service.RuntimeSettings;
import com.sirma.itt.seip.eai.content.tool.service.SpreadsheetProcessorTask;

import javafx.application.Platform;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

/**
 * FileFolderSelectorController builds the tree view of the file system using lazy mechanism for loading children on
 * request. Generated tree view contains tree cells with checkboxes. During runtime currently selected files could be
 * obtained using {@link #getSelected()}
 * 
 * @author bbanchev
 */
public class FileFolderSelectorController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final ExecutorService TASK_EXECUTOR = Executors.newSingleThreadExecutor();
	private static final File ROOT_FILE = new File("all");

	@FXML
	private TreeView<File> fileSelector;
	@FXML
	private TextField urlSelector;
	@FXML
	private Button process;
	@FXML
	private Button reset;
	/** The JavaFX observable collection that is responsible to trigger and handle events. */
	private ObservableSet<LazyFileTreeNode> observableSelection;
	/** Holds the already created node for given file. */
	private Map<File, LazyFileTreeNode> cachedNodes = new HashMap<>();

	/**
	 * Method called by the FXMLLoader when initialization is complete
	 */
	@FXML
	void initialize() {
		process.setOnAction(this::handleProcess);
		reset.setOnAction(this::handleReset);
		urlSelector.setOnKeyReleased(this::handleURLSelection);
		TASK_EXECUTOR.execute(this::initModel);
	}

	private void handleProcess(ActionEvent event) {
		Button source = (Button) event.getSource();
		try {

			source.disableProperty().set(true);
			SpreadsheetProcessorTask task = new SpreadsheetProcessorTask(getSelected());
			ProgressController.getProgressBar().progressProperty().bind(task.progressProperty());
			Label progressInfo = ProgressController.getProgressInfo();
			progressInfo.textProperty().bind(task.messageProperty());
			progressInfo.setTextFill(Color.BLACK);
			task.setOnSucceeded((WorkerStateEvent event1) -> progressInfo.setTextFill(Color.GREEN));
			task.setOnFailed((WorkerStateEvent event1) -> progressInfo.setTextFill(Color.RED));
			TASK_EXECUTOR.execute(task);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			source.disableProperty().set(false);
		}
	}

	private void handleReset(@SuppressWarnings("unused") ActionEvent event) { // NOSONAR
		reset();
	}

	private void handleURLSelection(KeyEvent event) {
		if (event.getCode() != KeyCode.ENTER) {
			return;
		}
		String text = urlSelector.getText().trim();
		if (text.isEmpty()) {
			return;
		}
		File selected = new File(text);
		if (selected.exists()) {
			createAndSelectNodeByPath(getCachedNode(ROOT_FILE), selected);
			urlSelector.setText("");
		}
	}

	private class InitSelectionTask extends Task<Void> {

		private FileFolderSelectorController controller;

		InitSelectionTask(FileFolderSelectorController controller) {
			this.controller = controller;
		}

		@Override
		protected Void call() throws Exception {
			// give a chance other ui threads to finish before running init task - this prevents ui freezing
			Thread.sleep(500);
			Platform.runLater(() -> {
				controller.disableUIControlls(true);
				controller.setProgress(Cursor.WAIT, ProgressIndicator.INDETERMINATE_PROGRESS);
				controller.initView();
				controller.setProgress(Cursor.DEFAULT, 0);
				controller.disableUIControlls(false);
			});
			return null;
		}

	}

	void disableUIControlls(boolean disable) {
		process.disableProperty().set(disable);
		reset.disableProperty().set(disable);
		fileSelector.disableProperty().set(disable);
	}

	/**
	 * Initialize the tree model using all system root structures.
	 * 
	 * @param fileTree
	 *            is the ui component to build tree for
	 */
	private void initModel() {
		observableSelection = FXCollections.observableSet();
		observableSelection.addListener((SetChangeListener<LazyFileTreeNode>) change -> {
			if (change.wasAdded()) {
				add(change.getElementAdded());
			} else {
				remove(change.getElementRemoved());
			}
		});
		fileSelector.setCellFactory(this::createCell);
		TASK_EXECUTOR.execute(new InitSelectionTask(this));
	}

	protected LazyFileTreeNode initView() {
		fileSelector.setShowRoot(false);
		LazyFileTreeNode root = getCachedNode(ROOT_FILE);
		appendChildren(root, File.listRoots());
		HashSet<File> rawSelectedValues = getFileSelection();
		rawSelectedValues.stream().sorted((o1, o2) -> o1.getAbsolutePath().compareTo(o2.getAbsolutePath())).forEach(
				file -> createAndSelectNodeByPath(root, file));
		fileSelector.setRoot(root);
		return root;
	}

	private static HashSet<File> getFileSelection() {
		return RuntimeSettings.INSTANCE.get(RuntimeSettings.LAST_FILE_SELECTED, new HashSet<File>());
	}

	private void createAndSelectNodeByPath(LazyFileTreeNode rootNode, File file) {
		File rootFile = file.toPath().getRoot().toFile();
		LazyFileTreeNode node = appendChild(rootNode, rootFile);
		Iterator<Path> pathIterator = file.toPath().iterator();
		while (pathIterator.hasNext()) {
			setIndeterminateMode(node);
			setDeselectedMode(node);
			setExpandMode(node);
			rootFile = new File(rootFile, pathIterator.next().toFile().getName());
			node = appendChild(node, rootFile); // NOSONAR
		}
		if (node != null) {
			setSelectedMode(node);
		}
	}

	void setProgress(Cursor cursor, double progress) {
		if (fileSelector.getScene() != null) {
			fileSelector.getScene().setCursor(cursor);
		}
		ProgressBar progressBar = ProgressController.getProgressBar();
		if (progressBar != null && !progressBar.progressProperty().isBound()) {
			progressBar.progressProperty().set(progress);
		}
		// non ui mode
	}

	private CheckBoxTreeCell<File> createCell(@SuppressWarnings("unused") TreeView<File> param) { // NOSONAR
		CheckBoxTreeCell<File> checkBoxTreeCell = new CheckBoxTreeCell<>();
		checkBoxTreeCell.converterProperty().set(new StringConverter<TreeItem<File>>() {

			@Override
			public TreeItem<File> fromString(String string) {
				return getCachedNode(new File(string));
			}

			@Override
			public String toString(TreeItem<File> object) {
				if (object.getValue() != null) {
					String simpleName = object.getValue().getName();
					if (!simpleName.isEmpty()) {
						return simpleName;
					}
					return object.getValue().toString();
				}
				return object.toString();
			}
		});
		return checkBoxTreeCell;
	}

	LazyFileTreeNode appendChildren(LazyFileTreeNode root, File[] children) {
		if (children == null) {
			return root;
		}
		try {
			setProgress(Cursor.WAIT, ProgressIndicator.INDETERMINATE_PROGRESS);
			Stream.of(children).sorted((file1, file2) -> {
				if (file1.isDirectory() && file2.isFile())
					return -1;
				if (file1.isFile() && file2.isDirectory())
					return 1;
				return 0;
			}).forEach(child -> appendChild(root, child));

		} finally {
			setProgress(Cursor.DEFAULT, 0);
		}
		return root;
	}

	private LazyFileTreeNode appendChild(LazyFileTreeNode root, File file) {
		if (!isAcceptedFile(file)) {
			return null;
		}
		LazyFileTreeNode childNode = getCachedNode(file);
		if (!root.getChildren().contains(childNode)) {
			root.getChildren().add(childNode);
		}
		return childNode;
	}

	LazyFileTreeNode getCachedNode(File file) {
		return cachedNodes.computeIfAbsent(file, this::createNodeForFile);
	}

	private static boolean isAcceptedFile(File file) {
		try {
			Path path = file.toPath();
			DosFileAttributes attrs = Files.readAttributes(path, DosFileAttributes.class);
			if (path.getNameCount() > 0 && (attrs.isSystem() || attrs.isHidden())) {
				return false;
			}
		} catch (@SuppressWarnings("unused") Exception e) {// NOSONAR
			// just skip any error reading file attributes
		}
		return true;
	}

	LazyFileTreeNode createNodeForFile(File file) {
		LazyFileTreeNode fileTreeNode = new LazyFileTreeNode(file, this);
		fileTreeNode.setIndependent(true);
		fileTreeNode.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.booleanValue()) {
				observableSelection.add(fileTreeNode);
			} else {
				observableSelection.remove(fileTreeNode);
			}
		});
		fileTreeNode.setGraphic(getIcon(fileTreeNode.getValue()));
		return fileTreeNode;
	}

	private void add(LazyFileTreeNode addedNode) {
		createAndSelectNodeByPath(getCachedNode(ROOT_FILE), addedNode.getValue());
		new HashSet<>(observableSelection).stream().forEach(selectedNode -> {
			if (isSubFileSelected(addedNode, selectedNode)) {
				setIndeterminateMode(selectedNode);
				setDeselectedMode(selectedNode);
			}
			if (isSubFileSelected(selectedNode, addedNode)) {
				setDeselectedMode(selectedNode);
			}
		});
		getFileSelection().add(addedNode.getValue());
	}

	private void remove(LazyFileTreeNode removedNode) {
		getFileSelection().remove(removedNode.getValue());
		setDeselectedMode(removedNode);
		IntegerBinding detectInvalidLevels = detectInvalidLevels(removedNode, new SimpleIntegerProperty(0).add(0));
		LazyFileTreeNode current = removedNode;
		while ((detectInvalidLevels = detectInvalidLevels.subtract(1)).greaterThan(0).get()) {
			current = (LazyFileTreeNode) current.getParent();
			if (current == null) {
				break;
			}
			setDeterminateMode(current);
		}
	}

	IntegerBinding detectInvalidLevels(LazyFileTreeNode fileTreeNode, IntegerBinding level) {
		if (fileTreeNode == null) {
			return level;
		}
		if (isSubFileSelected(fileTreeNode)) {
			return level;
		}
		return detectInvalidLevels((LazyFileTreeNode) fileTreeNode.getParent(), level.add(1));
	}

	private boolean isSubFileSelected(LazyFileTreeNode addedNode) {
		for (LazyFileTreeNode lazyFileTreeNode : observableSelection) {
			if (isSubFileSelected(lazyFileTreeNode, addedNode)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isSubFileSelected(LazyFileTreeNode selectedNode, LazyFileTreeNode addedNode) {
		return selectedNode.getValue().toPath().startsWith(addedNode.getValue().toPath())
				&& !selectedNode.equals(addedNode);
	}

	private static void setIndeterminateMode(LazyFileTreeNode node) {
		if (node != null) {
			node.indeterminateProperty().set(true);
		}
	}

	private static void setDeterminateMode(LazyFileTreeNode node) {
		if (node != null) {
			node.indeterminateProperty().set(false);
		}
	}

	private static void setSelectedMode(LazyFileTreeNode node) {
		if (node != null) {
			node.selectedProperty().set(true);
		}
	}

	private static void setDeselectedMode(LazyFileTreeNode node) {
		if (node != null) {
			node.selectedProperty().set(false);
		}
	}

	private static void setExpandMode(LazyFileTreeNode node) {
		if (node != null) {
			node.expandedProperty().set(true);
		}
	}

	private static ImageView getIcon(File file) {
		Icon fileIcon = FileSystemView.getFileSystemView().getSystemIcon(file);
		Image awtImage = null;
		if (fileIcon instanceof ImageIcon) {
			awtImage = ((ImageIcon) fileIcon).getImage();
		}
		BufferedImage bImg = null;
		if (awtImage instanceof BufferedImage) {
			bImg = (BufferedImage) awtImage;
		} else if (awtImage != null) {
			bImg = new BufferedImage(awtImage.getWidth(null), awtImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = bImg.createGraphics();
			graphics.drawImage(awtImage, 0, 0, null);
			graphics.dispose();
		}
		if (bImg == null) {
			return null;
		}
		WritableImage fxImage = SwingFXUtils.toFXImage(bImg, null);
		return new ImageView(fxImage);
	}

	/**
	 * Resets the selection and clears all currently selected data
	 */
	public void reset() {
		observableSelection.clear();
	}

	/**
	 * Gets all checked files as set. Might contain files and folders
	 * 
	 * @return the selected files. Might be empty set, never null
	 */
	public Set<File> getSelected() {
		return observableSelection.stream().map(LazyFileTreeNode::getValue).collect(
				Collectors.toCollection(HashSet::new));
	}
}
