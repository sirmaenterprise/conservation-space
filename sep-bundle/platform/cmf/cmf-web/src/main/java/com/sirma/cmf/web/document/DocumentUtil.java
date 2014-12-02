package com.sirma.cmf.web.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;

/**
 * The <b>DocumentUtil</b>, represent utility class that will holds command functionality that is
 * used in panels.
 * 
 * @author cdimitrov
 */
@ApplicationScoped
public class DocumentUtil {

	/**
	 * This method will simply extract documents from a list with cases.
	 * 
	 * @param caseInstanceList
	 *            list with cases instances that will hold documents
	 * @param resultMap
	 *            result map that will be returned from specific filter
	 * @return list with document instances
	 */
	public List<DocumentInstance> extractCaseDocuments(List<CaseInstance> caseInstanceList,
			Map<CaseInstance, List<DocumentInstance>> resultMap) {

		List<DocumentInstance> documentInstances = new ArrayList<DocumentInstance>();
		if (caseInstanceList == null || caseInstanceList.isEmpty()) {
			return documentInstances;
		}
		for (CaseInstance currentCase : caseInstanceList) {
			for (DocumentInstance currentDocument : resultMap.get(currentCase)) {
				if (currentDocument != null) {
					documentInstances.add(currentDocument);
				}
			}
		}
		return documentInstances;
	}
}
