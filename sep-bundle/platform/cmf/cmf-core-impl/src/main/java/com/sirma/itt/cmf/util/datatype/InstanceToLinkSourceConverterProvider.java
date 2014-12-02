package com.sirma.itt.cmf.util.datatype;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.converters.AbstractInstanceToInstanceReferenceConverterProvider;

/**
 * Provider class that registers the converters for the default implementations of.
 * {@link com.sirma.itt.emf.instance.model.Instance} interface to {@link LinkSourceId}.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class InstanceToLinkSourceConverterProvider extends
		AbstractInstanceToInstanceReferenceConverterProvider {

	/** The entity classes. */
	private static List<Class<? extends Instance>> entityClasses = new java.util.LinkedList<Class<? extends Instance>>();
	/** The task classes. */
	private static List<Class<? extends AbstractTaskInstance>> taskClasses = new java.util.LinkedList<Class<? extends AbstractTaskInstance>>();

	static {
		entityClasses.add(CaseInstance.class);
		entityClasses.add(DocumentInstance.class);
		entityClasses.add(SectionInstance.class);
		entityClasses.add(WorkflowInstanceContext.class);
		entityClasses.add(CommonInstance.class);
		entityClasses.add(LinkInstance.class);

		entityClasses.add(TaskInstance.class);
		entityClasses.add(StandaloneTaskInstance.class);
	}

	/**
	 * Converter class for {@link TaskInstance} to {@link LinkSourceId}.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param <I>
	 *            the generic type
	 * @author BBonev
	 */
	public class TaskInstanceToLinkSourceConverter<T extends AbstractTaskInstance, I extends InstanceReference>
			implements Converter<T, I> {

		/** The target class. */
		private Class<? extends AbstractTaskInstance> targetClass;

		/**
		 * Instantiates a new task instance to link source converter.
		 * 
		 * @param targetClass
		 *            the target class
		 */
		public TaskInstanceToLinkSourceConverter(Class<? extends AbstractTaskInstance> targetClass) {
			this.targetClass = targetClass;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public I convert(AbstractTaskInstance source) {
			return (I) new LinkSourceId(source.getTaskInstanceId(), getType(targetClass), source);
		}
	}

	/**
	 * Gets the data type based on the given class.
	 * 
	 * @param clazz
	 *            the source class
	 * @return the data type
	 */
	@Override
	protected DataTypeDefinition getType(Class<?> clazz) {
		DataTypeDefinition typeDefinition = dictionaryService
				.getDataTypeDefinition(clazz.getName());
		if (typeDefinition == null) {
			throw new TypeConversionException("The given source class " + clazz.getName()
					+ " is not supported!");
		}
		return typeDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		for (Class<? extends Instance> c : entityClasses) {
			addEntityConverter(converter, c, LinkSourceId.class);
			addEntityConverter(converter, c, InstanceReference.class);
		}

		// for tasks we use custom converter
		// NOTE: this is not used for now - probably should not be used at all
		for (Class<? extends AbstractTaskInstance> c : taskClasses) {
			addTaskConverter(converter, c, LinkSourceId.class);
			addTaskConverter(converter, c, InstanceReference.class);
		}
	}

	/**
	 * Adds the task converter.
	 * 
	 * @param <F>
	 *            the generic type
	 * @param <T>
	 *            the generic type
	 * @param converter
	 *            the converter
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 */
	protected <F extends AbstractTaskInstance, T extends InstanceReference> void addTaskConverter(
			TypeConverter converter, Class<F> from, Class<T> to) {
		converter.addConverter(from, to, new TaskInstanceToLinkSourceConverter<F, T>(from));
	}

}
