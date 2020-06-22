package com.sirma.itt.seip;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.text.JTextComponent;

/**
 * The Class CustomFrames.
 */
public class CustomFrames {

	/**
	 * Show option dialog with input.
	 *
	 * @param parentComponent
	 *            the parent component
	 * @param message
	 *            the message
	 * @param title
	 *            the title
	 * @param messageType
	 *            the message type
	 * @param icon
	 *            the icon
	 * @param selectionValues
	 *            the selection values
	 * @param initialSelectionValue
	 *            the initial selection value
	 * @return the string
	 * @throws HeadlessException
	 *             the headless exception
	 */
	public static Object[] showOptionDialogWithInput(Component parentComponent, Object message, String title,
			int messageType, Icon icon, String[] selectionValues, Object initialSelectionValue)
					throws HeadlessException {

		OptionDialogWithInput dialog = OptionDialogWithInput.show(parentComponent, title, message.toString(),
				selectionValues, initialSelectionValue.toString());
		dialog.setPreferredSize(new Dimension(300, 170));
		dialog.setSize(new Dimension(300, 170));
		dialog.pack();
		dialog.setLocationRelativeTo(parentComponent);
		dialog.setVisible(true);
		dialog.setResizable(false);
		return new Object[] { dialog.getInputValue(), dialog.getSelection() };
	}

	/**
	 * Show multiple input.
	 *
	 * @param parentComponent
	 *            the parent component
	 * @param title
	 *            the title
	 * @param values
	 *            the values
	 * @param cls
	 *            the cls
	 * @return the list
	 */
	public static List<String> showMultipleInput(Component parentComponent, String title, String[] values,
			Class<? extends JTextComponent>[] cls) {
		if (values == null || cls == null || values.length != cls.length) {
			throw new RuntimeException("Initialization for component is invalid");
		}
		MultipleInput dialog = MultipleInput.show(parentComponent, title, values, cls);
		dialog.setPreferredSize(new Dimension(300, 150));
		dialog.setSize(new Dimension(400, 400));
		dialog.setLocationRelativeTo(parentComponent);
		dialog.setVisible(true);
		dialog.setResizable(false);
		return dialog.getInputValues();
	}
}
