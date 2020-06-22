package com.sirma.sep.model.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;

public class DomainInstanceServiceStub {

	private final DomainInstanceService domainInstanceService;
	private final Map<String, Instance> instanceMap;

	DomainInstanceServiceStub(DomainInstanceService domainInstanceService) {
		this.domainInstanceService = domainInstanceService;
		instanceMap = new LinkedHashMap<>();
		when(domainInstanceService.save(any())).then(invocation -> {
			InstanceSaveContext saveContext = invocation.getArgumentAt(0, InstanceSaveContext.class);
			Instance instance = saveContext.getInstance();
			instanceMap.put(instance.getId().toString(), instance);
			return instance;
		});
	}

	public void withInstance(Instance instance) {
		String identifier = instance.getId().toString();
		instanceMap.put(identifier, instance);
		when(domainInstanceService.loadInstance(identifier)).thenAnswer(invocation -> copy(instance));
	}

	private static Instance copy(Instance instance) {
		Instance copy = new EmfInstance(instance.getId());
		instance.getProperties().forEach((key, value) -> {
			if(value instanceof Copyable) {
				copy.getOrCreateProperties().put(key, (Serializable) ((Copyable) value).createCopy());
			} else {
				copy.getOrCreateProperties().put(key, value);
			}
		});
		return copy;
	}

	public Instance getInstance(String identifier) {
		return instanceMap.get(identifier);
	}

	public static class Builder<B extends Builder> {

		protected final Instance instance;

		Builder(String identifier) {
			instance = new EmfInstance(identifier);
		}

		B withProperty(IRI key, Serializable value) {
			instance.add(key.toString(), value);
			return (B) this;
		}

		B withProperty(String key, Serializable value) {
			instance.add(key, value);
			return (B) this;
		}

		B withLanguageProperty(IRI key, String label, String language) {
			Serializable languageProperty = instance.getOrCreateProperties().computeIfAbsent(key.toString(), k -> new MultiLanguageValue());
			((MultiLanguageValue) languageProperty).addValue(language, label);
			return (B) this;
		}

		B withLanguageProperty(String key, String label, String language) {
			Serializable languageProperty = instance.getOrCreateProperties().computeIfAbsent(key, k -> new MultiLanguageValue());
			((MultiLanguageValue) languageProperty).addValue(language, label);
			return (B) this;
		}
	}

	public static class InstanceBuilder extends Builder {
		InstanceBuilder(String identifier, DomainInstanceServiceStub domainInstanceServiceStub) {
			super(identifier);
			domainInstanceServiceStub.withInstance(instance);
		}
	}

	public static class InstanceVerifier extends Builder<InstanceVerifier> {
		private final DomainInstanceServiceStub domainInstanceServiceStub;

		InstanceVerifier(String identifier, DomainInstanceServiceStub domainInstanceServiceStub) {
			super(identifier);
			this.domainInstanceServiceStub = domainInstanceServiceStub;
		}

		public void validateState() {
			Instance expected = instance;
			Instance actual = domainInstanceServiceStub.getInstance(expected.getId().toString());
			assertNotNull(actual);

			// Allows for actual instance to have more properties
			expected.getProperties().keySet().forEach(key -> assertEquals(expected.get(key), actual.get(key)));
		}
	}
}
