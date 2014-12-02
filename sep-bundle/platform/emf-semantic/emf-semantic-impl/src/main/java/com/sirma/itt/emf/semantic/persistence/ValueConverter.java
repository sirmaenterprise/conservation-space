package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Utility class for converting RDF {@link Value} objects to their {@link Serializable}
 * representation and vice versa.
 *
 * @author Valeri Tishev
 */
@ApplicationScoped
public class ValueConverter implements TypeConverterProvider {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(ValueConverter.class);

	/** The value factory. */
	private static ValueFactory valueFactory = ValueFactoryImpl.getInstance();

	/** The Constant primitiveClasses. */
	private static final List<Pair<Class<?>, URI>> primitiveClasses = new ArrayList<Pair<Class<?>, URI>>(
			Arrays.asList(new Pair<Class<?>, URI>(String.class, null),
					new Pair<Class<?>, URI>(Integer.class, XMLSchema.INTEGER),
					new Pair<Class<?>, URI>(Long.class, XMLSchema.LONG), new Pair<Class<?>, URI>(
							Float.class, XMLSchema.FLOAT), new Pair<Class<?>, URI>(Double.class,
							XMLSchema.DOUBLE), new Pair<Class<?>, URI>(Boolean.class,
							XMLSchema.BOOLEAN)));

	/** The namespace registry service. */
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/**
	 * Creates literal {@link Value} by given {@link Serializable} value.
	 *
	 * @param value
	 *            the {@link Serializable} value
	 * @return the value or <code>null</code> if the value type is not recognized
	 */
	public static Value createLiteral(Serializable value) {
		if (value == null) {
			return null;
		}
		return TypeConverterUtil.getConverter().convert(Value.class, value);
	}

	/**
	 * Converts {@link Value} to Serializable object.
	 *
	 * @param value
	 *            Value from the semantics
	 * @return Serializable object or 'null' if the value is 'null'
	 */
	public static Serializable convertValue(Value value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Literal) {
			Literal literalValue = (Literal) value;
			URI datatype = literalValue.getDatatype();

			if (datatype == null) {
				return literalValue.stringValue();
			}

			if (datatype.equals(XMLSchema.BOOLEAN)) {
				return literalValue.booleanValue();
			} else if (datatype.equals(XMLSchema.DATE)) {
				return literalValue.calendarValue().toGregorianCalendar().getTime();
			} else if (datatype.equals(XMLSchema.DATETIME)) {
				return literalValue.calendarValue().toGregorianCalendar().getTime();
			} else if (datatype.equals(XMLSchema.LONG)) {
				return literalValue.longValue();
			} else if (datatype.equals(XMLSchema.INT)) {
				return literalValue.intValue();
			} else if (datatype.equals(XMLSchema.INTEGER)) {
				return literalValue.integerValue().intValue();
			} else if (datatype.equals(XMLSchema.DOUBLE)) {
				return literalValue.doubleValue();
			} else if (datatype.equals(XMLSchema.FLOAT)) {
				return literalValue.floatValue();
			} else if (datatype.equals(XMLSchema.BYTE)) {
				return literalValue.byteValue();
			} else if (datatype.equals(XMLSchema.TIME)) {
				return literalValue.calendarValue().toGregorianCalendar().getTime();
			} else {
				return literalValue.stringValue();
			}
		} else if (value instanceof URI) {
			URI uri = (URI) value;
			return uri.stringValue();
		} else if (value instanceof BNode) {
			// skip blank nodes
			return null;
		} else {
			LOGGER.warn("Unknown data type definition [" + value.getClass() + "]");
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void register(TypeConverter converter) {
		for (Pair<Class<?>, URI> pair : primitiveClasses) {
			converter.addConverter(pair.getFirst(), Value.class,
					new PrimitiveToValueConverter(pair.getSecond()));
		}

		converter.addConverter(EmfUser.class, Value.class, new ResourceConverter<EmfUser>());
		converter.addConverter(EmfGroup.class, Value.class, new ResourceConverter<EmfGroup>());
		converter.addConverter(Date.class, Value.class, new DateToValueConverter());
	}

	/**
	 * Resource converter to Value implementation. The current implementation convert the given
	 * resource to URIs.
	 *
	 * @param <E>
	 *            the resource type
	 * @author BBonev
	 */
	public class ResourceConverter<E extends Resource> implements Converter<E, Value> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Value convert(E source) {
			if (source.getId() == null) {
				return null;
			}
			return valueFactory.createURI(namespaceRegistryService.buildFullUri(source.getId()
					.toString()));
		}
	}

	/**
	 * Primitive to concrete value converter. The converter creates new value implementation with
	 * the given type
	 *
	 * @param <E>
	 *            the element type
	 * @author BBonev
	 */
	public class PrimitiveToValueConverter<E> implements Converter<E, Value> {

		/** The type. */
		private final URI type;

		/**
		 * Instantiates a new primitive to value converter.
		 *
		 * @param type
		 *            the type
		 */
		public PrimitiveToValueConverter(URI type) {
			this.type = type;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Value convert(E source) {
			if (type == null) {
				return valueFactory.createLiteral(source.toString());
			}
			return valueFactory.createLiteral(source.toString(), type);
		}

	}

	/**
	 * Value converter for {@link Date} to {@link Value}.
	 *
	 * @author BBonev
	 */
	public class DateToValueConverter implements Converter<Date, Value> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Value convert(Date source) {
			return valueFactory.createLiteral(source);
		}

	}

}
