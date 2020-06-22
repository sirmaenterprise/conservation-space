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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

/**
 * The Class MultipleInput.
 */
public class MultipleInput extends JDialog implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5647069638800386028L;

	/** The checks. */
	private List<JTextComponent> values = new ArrayList<JTextComponent>();

	/** The content panel. */
	private JPanel contentPanel;

	/** The options panel. */
	private JPanel optionsPanel;

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
		if ("OK".equals(e.getActionCommand())) {
			selection = new ArrayList<String>(values.size());
			for (JTextComponent field : values) {
				selection.add(field.getText());
			}
		} else {
			selection = null;
		}
		dispose();
	}

	/**
	 * Gets the selection.
	 *
	 * @return the selection
	 */
	public List<String> getInputValues() {
		return selection;
	}

	/**
	 * Instantiates a new option dialog with input.
	 *
	 * @param windowForComponent
	 *            the window for component
	 */
	public MultipleInput(Window windowForComponent) {
		super(windowForComponent);
	}

	/**
	 * Inits the.
	 *
	 * @param dialog
	 *            the dialog
	 * @param rows
	 *            the rows
	 */
	public void init(JDialog dialog, int rows) {
		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		dialog.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout());
		optionsPanel = new JPanel();
		optionsPanel.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPanel.add(optionsPanel, BorderLayout.CENTER);
		optionsPanel.setLayout(new GridLayout(rows, 2, 0, 50));
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
	 * @param options
	 *            the options
	 * @param cls
	 *            the cls
	 * @return the option dialog with input
	 */
	public static MultipleInput show(Component parent, String title, String[] options,
			Class<? extends JTextComponent>[] cls) {
		MultipleInput dialog = new MultipleInput(getWindowForComponent(parent));
		dialog.setModal(true);
		dialog.init(dialog, cls.length);
		dialog.setTitle(title);
		for (int i = 0; i < options.length; i++) {
			JTextComponent comp = null;
			try {
				comp = cls[i].newInstance();
				dialog.values.add(comp);
				dialog.optionsPanel.add(new JLabel(options[i]));
				dialog.optionsPanel.add(comp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dialog;

	}

}
