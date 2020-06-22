package com.sirma.itt.seip.testutil.mocks;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Mock object for local cached context
 *
 * @author bbanchev
 */
public class InstanceContextServiceMock implements InstanceContextService {

	private Map<Serializable, InstanceReference> localCache = new HashMap<>();

	@Override
	public Optional<InstanceReference> getContext(Serializable instance) {
		return Optional.ofNullable(localCache.get(extractId(instance)));
	}

	@Override
	public Optional<InstanceReference> getRootContext(Serializable instance) {
		Optional<InstanceReference> root = getContext(instance);
		while (root.isPresent()) {
			Optional<InstanceReference> contextInternal = getContext(root.get());
			if (!contextInternal.isPresent()) {// last valid state reached
				return root;
			}
			root = contextInternal;
		}
		return Optional.empty();
	}

	private void validateCycleDependency(String instance, String newContextId) {
		boolean cycleDetected = false;
		if (newContextId == null) {
			return;
		}

		String context = newContextId;
		while (context != null) {
			if (EqualsHelper.nullSafeEquals(context, instance)) {
				cycleDetected = true;
				break;
			}
			context = extractId(getContext(context).orElse(null));
		}

		if (cycleDetected) {
			throw new EmfRuntimeException("Detected cycle dependency during context assigment on: " + instance);
		}
	}

	Instance getAsInstance(Serializable rawValue) {
		if (rawValue == null) {
			return null;
		}

		if (rawValue instanceof String) {
			return InstanceReferenceMock.createGeneric((String) rawValue).toInstance();
		}

		if (rawValue instanceof Instance) {
			return (Instance) rawValue;
		} else if (rawValue instanceof InstanceReference) {
			return ((InstanceReference) rawValue).toInstance();
		}

		return null;
	}

	private static void setContextInternal(Map<Serializable, InstanceReference> cache, Serializable instance,
			Serializable context, boolean overwrite) {
		if (instance == null) {
			return;
		}
		String key = extractId(instance);
		if (cache.containsKey(key)) {
			InstanceReference existingValue = cache.get(key);
			if (existingValue != null && !EqualsHelper.nullSafeEquals(existingValue.getId(), extractId(context))
					&& !overwrite) {
				throw new EmfRuntimeException("Context already initialized for " + key + " with different value: "
						+ existingValue + ", now trying to set: " + context);
			}
		}
		cache.put(key, getReferenceFromKey(context));
		if (instance instanceof Instance) {
			updateDomainModel((Instance) instance, extractId(cache.get(key)));
		} else if (instance instanceof InstanceReference) {
			updateDomainModel(((InstanceReference) instance).toInstance(), extractId(cache.get(key)));
		}
	}

	private static InstanceReference getReferenceFromKey(Serializable context) {
		if (context instanceof InstanceReference) {
			return (InstanceReference) context;
		} else if (context instanceof Instance) {
			return InstanceReferenceMock.createGeneric((Instance) context);
		} else if (context instanceof String) {
			return InstanceReferenceMock.createGeneric((String) context);
		} else if (context == null) {
			return null;
		}

		throw new UnsupportedOperationException(
				"Tests supports only String, InstanceReference, Instance context values!");

	}

	private static void updateDomainModel(Instance instance, String contextId) {
		if (instance == null) {
			return;
		}

		if (contextId != null) {
			// set also null values to clear context
			instance.add("hasParent", contextId);
			instance.add(InstanceContextService.HAS_PARENT, contextId);
			instance.add(InstanceContextService.PART_OF_URI, contextId);
		} else {
			instance.remove("hasParent");
			instance.remove(InstanceContextService.HAS_PARENT);
			instance.remove(InstanceContextService.PART_OF_URI);
		}
	}

	private static String extractId(Serializable value) {
		if (value == null) {
			return null;
		}

		String id = null;
		if (value instanceof String) {
			id = (String) value;
		} else if (value instanceof InstanceReference) {
			id = ((InstanceReference) value).getId();
		} else if (value instanceof Instance) {
			// chain call since id might be IRI, Uri
			id = extractId(((Instance) value).getId());
		} else if (value instanceof Collection && ((Collection<?>) value).size() == 1) {
			return extractId(((Collection<Serializable>) value).iterator().next());
		} else {
			id = TypeConverterUtil.getConverter().stringValue(value);
		}

		if (id == null) {
			throw new EmfRuntimeException("Unsupported (not persisted) context provided - " + value);
		}

		return id;
	}

	@Override
	public void bindContext(Instance instance, Serializable context) {
		validateCycleDependency(extractId(instance), extractId(context));
		setContextInternal(localCache, instance, context, true);
	}

	@Override
	public List<InstanceReference> getFullPath(Serializable instance) {
		InstanceReference source = getReferenceFromKey(instance);
		LinkedList<InstanceReference> fullContext = (LinkedList<InstanceReference>) getContextPath(source);
		if (source != null) {
			fullContext.addLast(source);
		}
		return fullContext;
	}

	@Override
	public boolean isContextChanged(Instance instance) {
		Serializable newContextId = instance.get(InstanceContextService.HAS_PARENT);
		String oldContextId = getContext(instance.getId()).map(InstanceReference::getId).orElse(null);
		return oldContextId == null ? newContextId != null : !oldContextId.equals(newContextId);
	}
}
