package com.sirma.itt.seip.instance.script;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.script.ScriptInstance;

/**
 * Base converter provider for handling the common case for converting any {@link Instance} to {@link ScriptNode}. If
 * nothing else is specified the produces {@link ScriptNode} instance will have the default implementation. Any
 * additional implementation could be provided by adding concrete converter of the specific instance to
 * {@link ScriptNode} and using the provided {@link InstanceToScriptNodeConverter} or any custom converter. <br>
 * <b>IMPLEMENTATION NOTICE:</b> Each produced instance from the converter should be new object instance for every
 * different instance that is converted. Or in other words each call of the convert method should provide unique
 * instance for each unique instance passed.
 *
 * @author BBonev
 */
@ApplicationScoped
public class InstanceToScriptNodeConverterProvider implements TypeConverterProvider {

	/** The generic nodes provider. */
	@Inject
	@InstanceType(type = ScriptInstance.SCRIPT_TYPE)
	private javax.enterprise.inject.Instance<ScriptNode> nodes;

	/**
	 * Universal converter from any instance to {@link ScriptNode} implementation using the provided
	 * {@link javax.enterprise.inject.Instance} with valid producer. The provided instance will be used to fetch an
	 * instance set the converted instance as target and return the node.
	 *
	 * @author BBonev
	 * @param <I>
	 *            the instance type
	 * @param <S>
	 *            the script instance type
	 */
	public static class InstanceToScriptNodeConverter<I extends Instance, S extends ScriptInstance>
			implements Converter<I, S> {

		/** The script nodes. */
		private javax.enterprise.inject.Instance<S> scriptNodes;

		/**
		 * Instantiates a new instance to script node converter.
		 *
		 * @param nodes
		 *            the nodes provider
		 */
		public InstanceToScriptNodeConverter(javax.enterprise.inject.Instance<S> nodes) {
			scriptNodes = nodes;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public S convert(I source) {
			// forces new node creation by calling get() method
			S node = scriptNodes.get();
			node.setTarget(source);
			return node;
		}
	}

	@Override
	public void register(TypeConverter converter) {
		addConverter(converter, Instance.class, nodes);
	}

	/**
	 * Adds a converter for the given source class. The instance will be created using the given node provider.
	 *
	 * @param <I>
	 *            the type of the source instance type
	 * @param <S>
	 *            the type of the script instance node
	 * @param converter
	 *            the converter instance to add to
	 * @param sourceType
	 *            is the source instance type
	 * @param nodesProvider
	 *            is the node provider to use when new instance is needed to be created.
	 */
	@SuppressWarnings("unchecked")
	public static <I extends Instance, S extends ScriptInstance> void addConverter(TypeConverter converter,
			Class<I> sourceType, javax.enterprise.inject.Instance<S> nodesProvider) {
		if (nodesProvider == null) {
			// nothing is done
			return;
		}
		converter.addConverter(sourceType, ScriptNode.class,
				new InstanceToScriptNodeConverter<>((javax.enterprise.inject.Instance<ScriptNode>) nodesProvider));
		converter.addConverter(sourceType, ScriptInstance.class,
				new InstanceToScriptNodeConverter<>((javax.enterprise.inject.Instance<ScriptInstance>) nodesProvider));
	}

}
