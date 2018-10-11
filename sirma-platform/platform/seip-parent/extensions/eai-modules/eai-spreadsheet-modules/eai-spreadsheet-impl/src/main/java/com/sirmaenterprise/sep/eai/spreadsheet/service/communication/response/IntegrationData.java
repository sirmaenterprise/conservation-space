package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import java.util.Objects;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;

/**
 * The {@link IntegrationData} is wrapper for all entity data related to response processing of integration data
 * 
 * @author bbanchev
 */
class IntegrationData {
	private Instance integrated;
	private Instance context;
	private SpreadsheetEntry source;
	private EntityType type;
	private Operation operation;
	private ModelConfiguration modelConfiguration;
	private SpreadsheetEntryId id;

	/**
	 * Instantiates a new integration data.
	 *
	 * @param integrated
	 *            the integrated instance entity. Requires non null.
	 * @param context
	 *            the context for the instance
	 * @param source
	 *            the source data. Requires non null.
	 * @param id
	 *            the source id. Requires non null.
	 * @param operation
	 *            the operation - create or update
	 * @param modelConfiguration
	 *            the model configuration linked to this data. Requires non null.
	 */
	IntegrationData(Instance integrated, Instance context, SpreadsheetEntry source, SpreadsheetEntryId id,
			Operation operation, ModelConfiguration modelConfiguration) {
		Objects.requireNonNull(integrated, "Missing integrated instance value. Check client code!");
		Objects.requireNonNull(source, "Missing integrated source value. Check client code!");
		Objects.requireNonNull(id, "Missing integrated source id value. Check client code!");
		Objects.requireNonNull(modelConfiguration, "Missing integrated data model confiu value. Check client code!");
		this.integrated = integrated;
		this.context = context;
		this.source = source;
		this.id = id;
		this.operation = operation;
		this.modelConfiguration = modelConfiguration;
		this.type = modelConfiguration.getTypeByDefinitionId(integrated.getIdentifier());

	}

	/**
	 * Gets the integrated data instance in SEP
	 *
	 * @return the integrated instance
	 */
	Instance getIntegrated() {
		return integrated;
	}

	/**
	 * Gets the integrated data context in SEP
	 *
	 * @return the context. Might be null
	 */
	Instance getContext() {
		return context;
	}

	/**
	 * Gets the integration data source entry
	 *
	 * @return the source
	 */
	SpreadsheetEntry getSource() {
		return source;
	}

	/**
	 * Gets the cached type based on the integration source
	 *
	 * @return the type
	 */
	EntityType getType() {
		return type;
	}

	/**
	 * Gets the operation executed on the integrated instance - create or update or etc.
	 *
	 * @return the operation
	 */
	Operation getOperation() {
		return operation;
	}

	/**
	 * Gets the model configuration for integrated instance
	 *
	 * @return the modelConfiguration
	 */
	ModelConfiguration getModelConfiguration() {
		return modelConfiguration;
	}

	/**
	 * Gets the source id for entry
	 *
	 * @return the id
	 */
	SpreadsheetEntryId getId() {
		return id;
	}

}
