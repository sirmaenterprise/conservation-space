package com.sirma.itt.cmf.evaluators;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.evaluation.BaseEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Extractor class that can handle document extractions.
 * <p>
 * Possible formats:<br>
 * <code><pre> ${extract(section.fieldName[value].field)}
 * ${extract(section[value].field)}</pre></code> <br>
 * Selects from the given section Id a document that has 'value' set into the
 * fieldName and returns the value of the given field from the last modified
 * found document.<br>
 * The default fieldName is {@link DocumentProperties#TYPE}.
 *
 * @author BBonev
 */
public class DocumentExtractEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 353117297526680988L;

	/** The Constant VALUE_EXTRACTION. */
	private static final Pattern VALUE_EXTRACTION = Pattern.compile(EXPRESSION_START
			+ "\\{extract\\(([\\w,]+)?(\\.?\\w+?)?\\[(\\w+?)\\]\\.(\\w+?)\\)\\}");

	@Override
	protected Pattern getPattern() {
		return VALUE_EXTRACTION;
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {

		CaseInstance caseInstance = getTypedParameter(values, CaseInstance.class);
		// we need that argument so if not present then we a done
		if (caseInstance == null) {
			Serializable instance = getCurrentInstance(context, values);
			if (instance instanceof CaseInstance) {
				caseInstance = (CaseInstance) instance;
			} else if (instance instanceof Instance) {
				caseInstance = InstanceUtil.getParent(CaseInstance.class, (Instance) instance);
			}
			if (caseInstance == null) {
				// we could also throw an exception
				return null;
			}
		}
		// the section id/s to look for
		String sectionId = matcher.group(1);
		boolean allSections = StringUtils.isNullOrEmpty(sectionId);
		Set<String> sectionIds = Collections.emptySet();
		if (!allSections) {
			String[] split = sectionId.split(",");
			sectionIds = CollectionUtils.createLinkedHashSet(split.length);
			for (String string : split) {
				// check if the section ids are valid before adding them to the set
				if (StringUtils.isNotNullOrEmpty(string)) {
					sectionIds.add(string);
				}
			}
			// no valid sections found (probably only comma defined)
			if (sectionIds.isEmpty()) {
				allSections = true;
			}
		}

		// the selector field
		String documentFieldName = matcher.group(2);
		if (StringUtils.isNullOrEmpty(documentFieldName)) {
			documentFieldName = DocumentProperties.TYPE;
		} else {
			// remove the leading dot character
			documentFieldName = documentFieldName.substring(1);
		}
		// the selector value
		String fieldValue = matcher.group(3);
		// the field to copy
		String fieldName = matcher.group(4);

		List<DocumentInstance> documents = new LinkedList<DocumentInstance>();

		// collect all documents from described sections
		if (allSections) {
			for (SectionInstance sectionInstance : caseInstance.getSections()) {
				DocumentInstance documentInstance = findDocumentInSection(documentFieldName,
						fieldValue, sectionInstance);
				// we only select uploaded documents
				if ((documentInstance != null) && documentInstance.hasDocument()) {
					documents.add(documentInstance);
				}
			}
		} else {
			for (String id : sectionIds) {
				Node child = caseInstance.getChild(id);
				if (child instanceof SectionInstance) {
					DocumentInstance documentInstance = findDocumentInSection(documentFieldName, fieldValue, (SectionInstance) child);
					// we only select uploaded documents
					if ((documentInstance != null) && documentInstance.hasDocument()) {
						documents.add(documentInstance);
					}
				}
			}
		}

		// no document is matched
		if (documents.isEmpty()) {
			return null;
		}

		// we search for the first value found in the given list of documents based on the section
		// order
		Serializable value = null;
		for (DocumentInstance instance : documents) {
			value = instance.getProperties().get(fieldName);
			if (value != null) {
				break;
			}
		}
		return value;
	}

	/**
	 * Find document in a section.
	 *
	 * @param documentFieldName
	 *            the document field name
	 * @param fieldValue
	 *            the field value
	 * @param section
	 *            the section to search into
	 * @return the first found document instance
	 */
	private DocumentInstance findDocumentInSection(String documentFieldName, String fieldValue,
			SectionInstance section) {
		for (Instance documentInstance : section.getContent()) {
			Map<String, Serializable> properties = documentInstance.getProperties();
			Serializable selector = properties.get(documentFieldName);
			if (selector != null) {
				try {
					// get the original selector value and convert the
					// expression value to the same type
					Serializable convert = converter.convert(selector.getClass(), fieldValue);
					// now the can compare the two values
					if ((documentInstance instanceof DocumentInstance)
							&& EqualsHelper.nullSafeEquals(selector, convert)) {
						return (DocumentInstance) documentInstance;
					}
				} catch (TypeConversionException e) {
					logger.warn("Failed to convert expression value " + fieldValue + " to field's "
							+ documentFieldName + " value type" + selector.getClass() + " due to "
							+ e.getMessage());
				}
			}
		}
		return null;
	}

}
