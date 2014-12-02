package com.sirma.itt.objects.web.library;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.cmf.web.rest.SearchQueryParameters;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Action bean responsible for some actions behind the object library page.
 * 
 * @author svelikov
 */
@Named
@RequestScoped
public class ObjectsLibraryAction extends Action {

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private ServiceRegister serviceRegister;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	private String definitionType;

	/**
	 * Inits the bean.
	 */
	@PostConstruct
	public void initBean() {
		definitionType = findDefinitionType();
	}

	/**
	 * Finds out the object definition type using the semantic uri of the selected object from
	 * object libraries menu.
	 * 
	 * @return the library type
	 */
	private String findDefinitionType() {
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, String> params = fc.getExternalContext().getRequestParameterMap();

		String objectType = params.get(SearchQueryParameters.OBJECT_TYPE);
		if (StringUtils.isNullOrEmpty(objectType)) {
			return null;
		}

		String fullUri = namespaceRegistryService.buildFullUri(objectType);
		if (StringUtils.isNullOrEmpty(fullUri)) {
			return null;
		}

		DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(fullUri);
		if (typeDefinition == null) {
			return null;
		}

		Class<?> javaClass = typeDefinition.getJavaClass();
		InstanceService<Instance, DefinitionModel> instanceService = serviceRegister
				.getInstanceService(javaClass);
		if (instanceService == null) {
			return null;
		}

		Class<DefinitionModel> instanceDefinitionClass = instanceService
				.getInstanceDefinitionClass();
		List<DefinitionModel> allDefinitions = dictionaryService
				.getAllDefinitions(instanceDefinitionClass);
		for (DefinitionModel definitionModel : allDefinitions) {
			Node child = definitionModel.getChild("rdf:type");
			if (child instanceof PropertyDefinition) {
				String defaultValue = ((PropertyDefinition) child).getDefaultValue();
				if (EqualsHelper.nullSafeEquals(fullUri, defaultValue)) {
					return definitionModel.getIdentifier();
				}
			}
		}

		return null;
	}

	/**
	 * Getter method for definitionType.
	 * 
	 * @return the definitionType
	 */
	public String getDefinitionType() {
		return definitionType;
	}

	/**
	 * Setter method for definitionType.
	 * 
	 * @param definitionType
	 *            the definitionType to set
	 */
	public void setDefinitionType(String definitionType) {
		this.definitionType = definitionType;
	}

}
