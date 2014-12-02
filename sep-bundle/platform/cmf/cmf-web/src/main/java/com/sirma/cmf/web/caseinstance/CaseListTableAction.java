package com.sirma.cmf.web.caseinstance;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
import org.richfaces.component.UIExtendedDataTable;

import com.sirma.cmf.web.TableAction;
import com.sirma.cmf.web.caseinstance.tab.CaseTabConstants;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;

/**
 * Cases table action.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class CaseListTableAction extends TableAction implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -860396417277540657L;

	/**
	 * Current selection from the cases list.
	 */
	private Collection<Object> selection;

	/**
	 * Selection items list from the backed table.
	 */
	private List<CaseInstance> selectionItems = new ArrayList<CaseInstance>();

	/**
	 * Opens the selected case dashboard.
	 * 
	 * @param selectedCaseInstance
	 *            Selected {@link CaseInstance}.
	 * @return Navigation string.
	 */
	// TODO: Move in CaseAction
	public String open(final CaseInstance selectedCaseInstance) {
		// Set default tab as selected.
		getDocumentContext().setSelectedTab(CaseTabConstants.CASE_DEFAULT_TAB);
		CaseDefinition caseDefinition = dictionaryService.getDefinition(CaseDefinition.class,
				selectedCaseInstance.getIdentifier());
		getDocumentContext().populateContext(selectedCaseInstance, CaseDefinition.class,
				caseDefinition);

		if (log.isDebugEnabled()) {
			String msg = MessageFormat
					.format("CMFWeb: Executed CaseListTableAction.open - document ID:[{0}]. Selected tab:[{1}]",
							selectedCaseInstance.getDmsId(), getDocumentContext().getSelectedTab());
			log.debug(msg);
		}

		return CasesConstants.NAVIGATE_CASE_DETAILS;
	}

	/**
	 * Selection listener executed when the master table row is clicked.
	 * 
	 * @param event
	 *            The event provided by the trigger.
	 */
	public void selectionListener(AjaxBehaviorEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("CMFWeb: Executing listener CaseListTableAction.selectionListener");
		}

		UIExtendedDataTable dataTable = (UIExtendedDataTable) event.getComponent();
		Object originalKey = dataTable.getRowKey();
		selectionItems.clear();
		for (Object selectionKey : selection) {
			dataTable.setRowKey(selectionKey);
			if (dataTable.isRowAvailable()) {
				selectionItems.add((CaseInstance) dataTable.getRowData());
			}
		}

		dataTable.setRowKey(originalKey);
	}

	/**
	 * Gets the selection.
	 * 
	 * @return the selection
	 */
	public Collection<Object> getSelection() {
		return selection;
	}

	/**
	 * Sets the selection.
	 * 
	 * @param selection
	 *            the selection to set
	 */
	public void setSelection(Collection<Object> selection) {
		this.selection = selection;
	}

	/**
	 * Getter method for selectionItems.
	 * 
	 * @return the selectionItems
	 */
	public List<CaseInstance> getSelectionItems() {
		return selectionItems;
	}

	/**
	 * Setter method for selectionItems.
	 * 
	 * @param selectionItems
	 *            the selectionItems to set
	 */
	public void setSelectionItems(List<CaseInstance> selectionItems) {
		this.selectionItems = selectionItems;
	}

}
