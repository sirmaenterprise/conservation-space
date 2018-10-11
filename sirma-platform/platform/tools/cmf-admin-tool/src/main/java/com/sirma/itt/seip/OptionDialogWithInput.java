package com.sirma.itt.seip;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * The Class OptionDialogWithInput.
 */
public class OptionDialogWithInput extends JDialog implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5647069638800386028L;

	/** The checks. */
	private List<JCheckBox> checks = new ArrayList<JCheckBox>();

	/** The content panel. */
	private JPanel contentPanel;

	/** The text field. */
	private JTextField textField;

	/** The options panel. */
	private JPanel optionsPanel;

	/** The input label. */
	private JLabel inputLabel;

	/** The input value. */
	private String inputValue;

	/** The selection. */
	private List<String> selection;

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("Cancel".equals(e.getActionCommand())) {
			inputValue = null;
			selection = Collections.emptyList();
		} else if ("OK".equals(e.getActionCommand())) {
			inputValue = textField.getText();
			selection = new ArrayList<String>(checks.size());
			for (JCheckBox box : checks) {
				if (box.isSelected()) {
					selection.add(box.getText());
				}
			}
		}
		dispose();
	}

	/**
	 * Gets the input value.
	 *
	 * @return the input value
	 */
	public String getInputValue() {
		return inputValue;
	}

	/**
	 * Gets the selection.
	 *
	 * @return the selection
	 */
	public List<String> getSelection() {
		return selection;
	}

	/**
	 * Instantiates a new option dialog with input.
	 *
	 * @param windowForComponent
	 *            the window for component
	 */
	public OptionDialogWithInput(Window windowForComponent) {
		super(windowForComponent);
	}

	/**
	 * Inits the.
	 *
	 * @param dialog
	 *            the dialog
	 */
	public void init(JDialog dialog) {
		dialog.setLayout(new BorderLayout());
		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		dialog.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		optionsPanel = new JPanel();
		optionsPanel.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(optionsPanel);
		//
		JPanel panel = new JPanel();
		contentPanel.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(0, 2, 0, 0));
		inputLabel = new JLabel();
		panel.add(inputLabel);
		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(10);
		//
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		dialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		dialog.getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		cancelButton.addActionListener(this);
	}

	/**
	 * Gets the window for component.
	 *
	 * @param parentComponent
	 *            the parent component
	 * @return the window for component
	 * @throws HeadlessException
	 *             the headless exception
	 */
	static Window getWindowForComponent(Component parentComponent) throws HeadlessException {
		if (parentComponent == null) {
			return JOptionPane.getRootFrame();
		}
		if (parentComponent instanceof Frame || parentComponent instanceof Dialog) {
			return (Window) parentComponent;
		}
		return getWindowForComponent(parentComponent.getParent());
	}

	/**
	 * Show.
	 *
	 * @param parent
	 *            the parent
	 * @param title
	 *            the title
	 * @param inputTitle
	 *            the input title
	 * @param options
	 *            the options
	 * @param initial
	 *            the initial
	 * @return the option dialog with input
	 */
	public static OptionDialogWithInput show(Component parent, String title, String inputTitle, String[] options,
			String initial) {
		OptionDialogWithInput dialog = new OptionDialogWithInput(getWindowForComponent(parent));
		dialog.setModal(true);
		dialog.init(dialog);
		dialog.setTitle(title);
		dialog.inputLabel.setText(inputTitle);
		for (String string : options) {
			JCheckBox box = new JCheckBox(string);
			dialog.checks.add(box);
			dialog.optionsPanel.add(box);
		}
		dialog.textField.setText(initial);
		return dialog;

	}

}
