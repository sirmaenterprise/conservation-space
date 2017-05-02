package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.convert.TypeConverterUtil;

/**
 * Utility class for converting RDF {@link Value} objects to their {@link Serializable} representation and vice versa.
 *
 * @author Valeri Tishev
 */
@ApplicationScoped
@SuppressWarnings("boxing")
public class ValueConverter implements TypeConverterProvider {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ValueConverter.class);

	/** The value factory. */
	private static ValueFactory valueFactory = ValueFactoryImpl.getInstance();

	/** The Constant primitiveClasses. */
	private static final List<Pair<Class<?>, URI>> primitiveClasses = new ArrayList<>(Arrays.asList(
			new Pair<Class<?>, URI>(String.class, null), new Pair<Class<?>, URI>(Integer.class, XMLSchema.INTEGER),
			new Pair<Class<?>, URI>(Long.class, XMLSchema.LONG), new Pair<Class<?>, URI>(Float.class, XMLSchema.FLOAT),
			new Pair<Class<?>, URI>(Double.class, XMLSchema.DOUBLE),
			new Pair<Class<?>, URI>(Boolean.class, XMLSchema.BOOLEAN)));

	private static Map<URI, Function<Literal, Serializable>> valueTransformers = new HashMap<>(64);

	static {
		valueTransformers.put(XMLSchema.BOOLEAN, literal -> literal.booleanValue());
		valueTransformers.put(XMLSchema.DATE, literal -> literal.calendarValue().toGregorianCalendar().getTime());
		valueTransformers.put(XMLSchema.DATETIME, literal -> literal.calendarValue().toGregorianCalendar().getTime());
		valueTransformers.put(XMLSchema.LONG, literal -> literal.longValue());
		valueTransformers.put(XMLSchema.INT, literal -> literal.intValue());
		valueTransformers.put(XMLSchema.INTEGER, literal -> literal.integerValue().intValue());
		valueTransformers.put(XMLSchema.DOUBLE, literal -> literal.doubleValue());
		valueTransformers.put(XMLSchema.FLOAT, literal -> literal.floatValue());
		valueTransformers.put(XMLSchema.BYTE, literal -> literal.byteValue());
		valueTransformers.put(XMLSchema.TIME, literal -> literal.calendarValue().toGregorianCalendar().getTime());
		valueTransformers.put(XMLSchema.STRING, literal -> literal.stringValue());
		valueTransformers.put(null, literal -> literal.stringValue());
	}

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
		// skip blank nodes
		if (value == null || value instanceof BNode) {
			return null;
		}
		if (value instanceof URI) {
			return value.stringValue();
		}
		Serializable result = null;
		if (value instanceof Literal) {
			Literal literalValue = (Literal) value;
			Function<Literal, Serializable> trasformer = valueTransformers.get(literalValue.getDatatype());
			if (trasformer != null) {
				result = trasformer.apply(literalValue);
			} else {
				result = literalValue.stringValue();
			}
		} else {
			LOGGER.warn("Unknown data type definition [{}]", value.getClass());
		}
		return result;
	}

	@Override
	public void register(TypeConverter converter) {
		for (Pair<Class<?>, URI> pair : primitiveClasses) {
			converter.addConverter(pair.getFirst(), Value.class, source -> {
				URI type = pair.getSecond();
				if (type == null) {
					return valueFactory.createLiteral(source.toString());
				}
				return valueFactory.createLiteral(source.toString(), type);
			});
		}

		converter.addConverter(Date.class, Value.class, source -> valueFactory.createLiteral(source));
	}

}
