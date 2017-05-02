package com.sirma.itt.seip.eai.cms.service.communication.response;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.cms.configuration.CMSIntegrationConfigurationProvider;
import com.sirma.itt.seip.eai.cs.model.CSItemRecord;
import com.sirma.itt.seip.eai.cs.model.CSItemRelations;
import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.cs.model.internal.CSResolvableInstance;
import com.sirma.itt.seip.eai.cs.service.communication.response.CSResponseReaderAdapter;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.mapping.EntityRelation;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReaderAdapter;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.plugin.Extension;

/**
 * CMS response adapter that handles specific requirements for CMS integrations
 * 
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = EAIResponseReaderAdapter.PLUGIN_ID, order = 10)
public class CMSResponseReaderAdapter extends CSResponseReaderAdapter {

	@Override
	protected Object loadRelationAsInformation(CSItemRecord sourceRecord, CSItemRelations nextRelation,
			Map<CSExternalInstanceId, Instance> existing, ErrorBuilderProvider errorBuilder) throws EAIException {
		CSItemRecord target = nextRelation.getRecord();
		CSExternalInstanceId targetExternalId = extractExternalId(target, null);
		boolean primaryImageRelation = isPrimaryImageRelation(sourceRecord, target, nextRelation);
		if (!primaryImageRelation) {
			return existing.get(targetExternalId);
		}
		// check primary image as ESUC03-S07
		if (existing.get(targetExternalId) != null) {
			Instance targetInstance = existing.get(targetExternalId);
			CSExternalInstanceId sourceExternalId = extractExternalId(sourceRecord, null);
			Instance sourceInstance = existing.get(sourceExternalId);
			if (sourceInstance == null) {
				List<Instance> executeSearchByRecordInfo = executeSearchByRecordInfo(
						Collections.singletonMap(sourceExternalId, sourceRecord), errorBuilder);
				if (!executeSearchByRecordInfo.isEmpty()) {
					sourceInstance = executeSearchByRecordInfo.get(0);
				}
			}
			if (sourceInstance == null || targetInstance == null) {
				return null;
			}
			return targetInstance;
		}
		return new CSResolvableInstance(getNamespace(target), targetExternalId);
	}

	protected boolean isPrimaryImageRelation(CSItemRecord sourceRecord, CSItemRecord target,
			CSItemRelations nextRelation) throws EAIReportableException {

		ModelConfiguration modelConfigurationByNamespace = modelService
				.getModelConfigurationByNamespace(getNamespace(sourceRecord));
		EntityType typeByExternalName = modelConfigurationByNamespace
				.getTypeByExternalName(sourceRecord.getClassification());
		if (typeByExternalName == null) {
			return false;
		}
		if (!"image".equalsIgnoreCase(getNamespace(target))) {
			return false;
		}
		Optional<EntityRelation> matchedRelation = typeByExternalName.getRelations()
				.stream()
				.filter(r -> r.getMappings().contains(nextRelation.getType().toLowerCase()))
				.findFirst();
		if (matchedRelation.isPresent()
				&& matchedRelation.get().getUri().equalsIgnoreCase(LinkConstants.HAS_PRIMARY_IMAGE)) {
			return true;
		}
		return false;
	}

	@Override
	protected String getDefaultNamespace() {
		return CMSIntegrationConfigurationProvider.NAMESPACE;
	}

	@Override
	public String getName() {
		return CMSIntegrationConfigurationProvider.SYSTEM_ID;
	}

}
