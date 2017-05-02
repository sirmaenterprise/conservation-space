package com.sirma.cmf.web.form;

import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.cmf.web.form.builder.ActualEffortBuilder;
import com.sirma.cmf.web.form.builder.CheckboxFieldBuilder;
import com.sirma.cmf.web.form.builder.DateFieldBuilder;
import com.sirma.cmf.web.form.builder.DateRangeFieldBuilder;
import com.sirma.cmf.web.form.builder.FormBuilder;
import com.sirma.cmf.web.form.builder.LogWorkWidgetBuilder;
import com.sirma.cmf.web.form.builder.MultyLineFieldBuilder;
import com.sirma.cmf.web.form.builder.RelationsWidgetBuilder;
import com.sirma.cmf.web.form.builder.SingleLineFieldBuilder;
import com.sirma.cmf.web.form.builder.UsernameFieldBuilder;
import com.sirma.itt.emf.web.util.DateUtil;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.PropertyModel;

/**
 * Provides base functionality for definition reader.
 *
 * @author svelikov
 */
public class DefinitionReaderBase {

	static final String SINGLE_LINE_FIELD_MAX_LENGTH = "singleLineFieldMaxLength";

	@Inject
	protected Logger log;

	@Inject
	protected LabelProvider labelProvider;

	@Inject
	private CodelistService codelistService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private FacesContext facesContext;

	@Inject
	private DateUtil dateUtil;

	protected final Map<BuilderType, FormBuilder> builders = new HashMap<>();

	private String maxLengthForSingleLineField;

	/**
	 * Current instance class name with uncapitalized first letter. If rootInstanceName is passed then it is used
	 * instead (instanceName = rootInstanceName + "." + modelInstanceName)
	 */
	protected String instanceName;

	/** The current instance that we are building a form for. */
	private PropertyModel instance;

	/**
	 * The root instance name as is passed to the read method through the rootInstanceName argument.
	 */
	protected String baseInstanceName;

	/**
	 * Initialize the reader.
	 */
	public void initReader() {
		if (builders.isEmpty()) {
			log.debug("CMFWeb: Init DefinitionReader");

			builders.put(BuilderType.SINGLE_LINE_FIELD, new SingleLineFieldBuilder(labelProvider, codelistService));
			builders.put(BuilderType.MULTY_LINE_FIELD, new MultyLineFieldBuilder(labelProvider, codelistService));
			builders.put(BuilderType.CHECKBOX_FIELD, new CheckboxFieldBuilder(labelProvider, codelistService));
			builders.put(BuilderType.DATE_FIELD, new DateFieldBuilder(labelProvider, codelistService, dateUtil));
			builders.put(BuilderType.USER, new UsernameFieldBuilder(labelProvider, codelistService));
			builders.put(BuilderType.DATERANGE, new DateRangeFieldBuilder(labelProvider, codelistService, dateUtil));
			builders.put(BuilderType.RELATIONS_WIDGET, new RelationsWidgetBuilder(labelProvider, codelistService));
			builders.put(BuilderType.LOG_WORK_WIDGET, new LogWorkWidgetBuilder(labelProvider, codelistService));
			builders.put(BuilderType.ACTUAL_EFFORT, new ActualEffortBuilder(labelProvider, codelistService));
		}
	}

	/**
	 * Set style class that container is rendered.
	 *
	 * @param container
	 *            Container instance.
	 */
	@SuppressWarnings("static-method")
	protected void setContainerDoneStyleClass(final UIComponent container) {
		String panelStyleClass = " rendered";
		String styleClass = (String) container.getAttributes().get("styleClass");
		if (styleClass == null) {
			styleClass = panelStyleClass;
		} else {
			styleClass += panelStyleClass;
		}
		container.getAttributes().put("styleClass", styleClass);
	}

	/**
	 * Checks if the property content value has length greater than predefined value length.
	 *
	 * @param property
	 *            the property
	 * @return true if the property max length is set to be greater than predefined maximum allowed length for single
	 *         line fields. {@link PropertyDefinition}
	 */
	protected boolean isMultyline(PropertyDefinition property) {
		return property.getMaxLength() > Integer.valueOf(getMaxLengthForSingleLineField());
	}

	/**
	 * Getter method for maxLengthForSingleLineField.
	 *
	 * @return the maxLengthForSingleLineField
	 */
	public String getMaxLengthForSingleLineField() {
		if (maxLengthForSingleLineField == null) {
			maxLengthForSingleLineField = facesContext
					.getExternalContext()
						.getInitParameter(SINGLE_LINE_FIELD_MAX_LENGTH);
		}
		return maxLengthForSingleLineField;
	}

	/**
	 * Setter method for maxLengthForSingleLineField.
	 *
	 * @param maxLengthForSingleLineField
	 *            the maxLengthForSingleLineField to set
	 */
	public void setMaxLengthForSingleLineField(String maxLengthForSingleLineField) {
		this.maxLengthForSingleLineField = maxLengthForSingleLineField;
	}

	/**
	 * Initialize a builder.
	 *
	 * @param builderType
	 *            Required builder type to be created.
	 * @param container
	 *            Container component where the builded fileds will be placed.
	 * @param property
	 *            The property definition.
	 * @param propertyValue
	 *            The property value.
	 * @param formViewMode
	 *            ViewMode
	 * @param isRequired
	 *            if the fields that is going to be build is dynamically required
	 * @return Concrete {@link FormBuilder} instance.
	 */
	public FormBuilder initBuilder(BuilderType builderType, UIComponent container, PropertyDefinition property,
			Object propertyValue, FormViewMode formViewMode, boolean isRequired) {
		FormBuilder builder = builders.get(builderType);

		builder.setBuilderType(builderType);
		builder.setInstanceName(instanceName);
		builder.setInstance(getInstance());
		builder.setBaseInstanceName(baseInstanceName);
		builder.setViewMode(formViewMode);
		builder.setContainer(container);
		builder.setPropertyDefinition(property);
		builder.setPropertyValue(propertyValue);
		builder.setRequiredDynamically(isRequired);
		builder.setDictionaryService(dictionaryService);

		return builder;
	}

	/**
	 * Getter method for builders.
	 *
	 * @return the builders
	 */
	public Map<BuilderType, FormBuilder> getBuilders() {
		return builders;
	}

	/**
	 * Getter method for instanceName.
	 *
	 * @return the instanceName
	 */
	public String getInstanceName() {
		return instanceName;
	}

	/**
	 * Setter method for instanceName.
	 *
	 * @param instanceName
	 *            the instanceName to set
	 */
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	/**
	 * Getter method for baseInstanceName.
	 *
	 * @return the baseInstanceName
	 */
	public String getBaseInstanceName() {
		return baseInstanceName;
	}

	/**
	 * Setter method for baseInstanceName.
	 *
	 * @param baseInstanceName
	 *            the baseInstanceName to set
	 */
	public void setBaseInstanceName(String baseInstanceName) {
		this.baseInstanceName = baseInstanceName;
	}

	/**
	 * Getter method for instance.
	 *
	 * @return the instance
	 */
	public PropertyModel getInstance() {
		return instance;
	}

	/**
	 * Setter method for instance.
	 *
	 * @param instance
	 *            the instance to set
	 */
	public void setInstance(PropertyModel instance) {
		this.instance = instance;
	}

}
