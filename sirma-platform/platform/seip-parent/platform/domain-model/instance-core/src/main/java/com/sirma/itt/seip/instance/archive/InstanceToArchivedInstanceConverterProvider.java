package com.sirma.itt.seip.instance.archive;

import static com.sirma.itt.seip.instance.version.VersionProperties.VERSION_CREATED_ON;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.mapping.ObjectMapper;

/**
 * Provides conversion from {@link ArchivedEntity} to {@link ArchivedInstance}.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class InstanceToArchivedInstanceConverterProvider implements TypeConverterProvider {

	@Inject
	private ObjectMapper dozerMapper;

	@Inject
	private TransactionIdHolder transactionIdHolder;

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(Instance.class, ArchivedInstance.class, instanceToArchivedInstance());
	}

	private Converter<Instance, ArchivedInstance> instanceToArchivedInstance() {
		return source -> {
			ArchivedInstance archivedInstance = dozerMapper.map(source, ArchivedInstance.class);
			archivedInstance.setId(InstanceVersionService.buildVersionId(source));
			archivedInstance.setTransactionId((String) transactionIdHolder.getTransactionId());
			// it is confusing, but it should not be null no matter, if we create version or delete instance
			archivedInstance.setDeletedOn(new Date());
			archivedInstance.setCreatedOn((Date) source.remove(VERSION_CREATED_ON));
			return archivedInstance;
		};
	}

}
