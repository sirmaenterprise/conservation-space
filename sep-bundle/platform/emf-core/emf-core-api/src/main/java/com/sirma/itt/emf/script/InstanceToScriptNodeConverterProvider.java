package com.sirma.itt.emf.script;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Base converter provider for handling the common case for converting any {@link Instance} to
 * {@link ScriptNode}. If nothing else is specified the produces {@link ScriptNode} instance will
 * have the default implementation. Any additional implementation could be provided by adding
 * concrete converter of the specific instance to {@link ScriptNode} and using the provided
 * {@link InstanceToScriptNodeConverter} or any custom converter. <br>
 * <b>IMPLEMENTATION NOTICE:</b> Each produced instance from the converter should be new object
 * instance for every different instance that is converted. Or in other words each call of the
 * convert method should provide unique instance for each unique instance passed.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class InstanceToScriptNodeConverterProvider implements TypeConverterProvider {

	/** The nodes. */
	@Inject
	private javax.enterprise.inject.Instance<ScriptNode> nodes;

	/**
	 * Universal converter from any instance to {@link ScriptNode} implementation using the provided
	 * {@link javax.enterprise.inject.Instance} with valid producer. The provided instance will be
	 * used to fetch an instance set the converted instance as target and return the node.
	 * 
	 * @author BBonev
	 * @param <I>
	 *            the generic type
	 */
	public class InstanceToScriptNodeConverter<I extends Instance> implements
			Converter<I, ScriptNode> {

		/** The script nodes. */
		private javax.enterprise.inject.Instance<? extends ScriptNode> scriptNodes;

		/**
		 * Instantiates a new instance to script node converter.
		 * 
		 * @param nodes
		 *            the nodes provider
		 */
		public InstanceToScriptNodeConverter(
				javax.enterprise.inject.Instance<? extends ScriptNode> nodes) {
			scriptNodes = nodes;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ScriptNode convert(I source) {
			ScriptNode node = scriptNodes.get();
			node.setTarget(source);
			return node;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(Instance.class, ScriptNode.class,
				new InstanceToScriptNodeConverter<Instance>(nodes));
	}

}
