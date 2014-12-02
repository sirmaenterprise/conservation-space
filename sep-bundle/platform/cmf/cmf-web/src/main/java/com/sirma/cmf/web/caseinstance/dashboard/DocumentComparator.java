package com.sirma.cmf.web.caseinstance.dashboard;

import java.util.Comparator;
import java.util.Date;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * This class represent custom document comparator. Will compare instances of type
 * {@link DocumentInstance} based on last modification.
 * 
 * @author cdimitrov
 */
public class DocumentComparator implements Comparator<DocumentInstance> {

	@Override
	public int compare(DocumentInstance first, DocumentInstance second) {
		Date firstDocumentDate = (Date) first.getProperties().get(DefaultProperties.MODIFIED_ON);

		Date secondDocumentDate = (Date) second.getProperties().get(DefaultProperties.MODIFIED_ON);
		return firstDocumentDate.compareTo(secondDocumentDate);
	}
}
