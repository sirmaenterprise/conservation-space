package com.sirma.itt.seip.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.BooleanUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.DateUtil;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Support for generic conversion between types. Additional conversions may be added. Basic inter-operability supported.
 * Direct conversion and two stage conversions via Number are supported.
 *
 * @author andyh
 * @author BBonev
 */
@ApplicationScoped
public class DefaultTypeConverter implements TypeConverterProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String UTF_8 = "UTF-8";
	private static final Long LONG_FALSE = Long.valueOf(0L);
	private static final Long LONG_TRUE = Long.valueOf(1L);

	/**
	 * The Class ListToStringConverter.
	 *
	 * @param <L>
	 *            the list type type
	 */
	private static final class CollectionToStringConverter<L extends Collection> implements Converter<L, String> {

		/** The converter. */
		private TypeConverter converter;

		/**
		 * Instantiates a new list to string converter.
		 *
		 * @param converter
		 *            the converter
		 */
		public CollectionToStringConverter(TypeConverter converter) {
			this.converter = converter;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String convert(L source) {
			if (source != null) {
				StringBuilder result = new StringBuilder();
				int i = source.size();
				for (Object element : source) {
					result.append(converter.convert(String.class, element));
					i--;
					if (i > 0) {
						result.append(",");
					}
				}
				return result.toString();
			}
			return "";
		}
	}

	@Override
	public void register(TypeConverter converter) {
		//
		// From string
		//
		converter.addConverter(String.class, Class.class, source -> {
			try {
				return Class.forName(source);
			} catch (ClassNotFoundException e) {
				throw new TypeConversionException("Failed to convert string to class: " + source, e);
			}
		});

		converter.addConverter(String.class, Boolean.class, source -> {
			Boolean booleanValue = BooleanUtils.toBooleanObject(source);
			if(booleanValue == null) {
				throw new TypeConversionException("Failed to convert string to boolean:  " + source);
			}
			return booleanValue;
		});

		converter.addConverter(String.class, Character.class, source -> {
			if (source == null || source.length() == 0) {
				return null;
			}
			return Character.valueOf(source.charAt(0));
		});

		converter.addConverter(String.class, Number.class, source -> {
			try {
				return NumberFormat.getNumberInstance().parse(source);
			} catch (ParseException e) {
				throw new TypeConversionException("Failed to parse number " + source, e);
			}
		});

		converter.addConverter(String.class, Byte.class, source -> Byte.valueOf(source));

		converter.addConverter(String.class, Short.class, source -> Short.valueOf(source));

		converter.addConverter(String.class, Integer.class, source -> Integer.valueOf(source));

		converter.addConverter(String.class, Long.class, source -> Long.valueOf(source));

		converter.addConverter(String.class, Float.class, source -> Float.valueOf(source));

		converter.addConverter(String.class, Double.class, source -> Double.valueOf(source));

		converter.addConverter(String.class, BigInteger.class, source -> new BigInteger(source));

		converter.addConverter(String.class, BigDecimal.class, source -> new BigDecimal(source));

		converter.addConverter(String.class, Date.class, source -> {
			try {
				// Converts timestamp value passed as string to date
				if (source.matches("\\d*")) {
					Timestamp time = new Timestamp(Long.parseLong(source));
					Date date = new Date(time.getTime());
					return date;
				}
				Date date = ISO8601DateFormat.parse(source);
				return date;
			} catch (RuntimeException e) {
				throw new TypeConversionException("Failed to convert string " + source + " to date", e);
			}
		});

		converter.addConverter(String.class, InputStream.class, source -> {
			try {
				return new ByteArrayInputStream(source.getBytes(UTF_8));
			} catch (UnsupportedEncodingException e) {
				throw new TypeConversionException("Encoding not supported", e);
			}
		});

		//
		// From Locale
		//

		converter.addConverter(Locale.class, String.class, source -> {
			String localeStr = source.toString();
			if (localeStr.length() < 6) {
				localeStr += "_";
			}
			return localeStr;
		});

		//
		// From enum
		//

		converter.addConverter(Enum.class, String.class, source -> source.toString());

		// From Class

		converter.addConverter(Class.class, String.class, source -> source.getName());

		//
		// Number to Subtypes and Date
		//

		converter.addConverter(Number.class, Boolean.class, source -> Boolean.valueOf(source.longValue() > 0));

		converter.addConverter(Number.class, Byte.class, source -> Byte.valueOf(source.byteValue()));

		converter.addConverter(Number.class, Short.class, source -> Short.valueOf(source.shortValue()));

		converter.addConverter(Number.class, Integer.class, source -> Integer.valueOf(source.intValue()));

		converter.addConverter(Number.class, Long.class, source -> Long.valueOf(source.longValue()));

		converter.addConverter(Number.class, Float.class, source -> Float.valueOf(source.floatValue()));

		converter.addConverter(Number.class, Double.class, source -> Double.valueOf(source.doubleValue()));

		converter.addConverter(Number.class, Date.class, source -> new Date(source.longValue()));

		converter.addConverter(Number.class, String.class, source -> source.toString());

		converter.addConverter(Number.class, BigInteger.class, source -> {
			if (source instanceof BigDecimal) {
				return ((BigDecimal) source).toBigInteger();
			}
			return BigInteger.valueOf(source.longValue());
		});

		converter.addConverter(Number.class, BigDecimal.class, source -> {
			if (source instanceof BigInteger) {
				return new BigDecimal((BigInteger) source);
			}
			return BigDecimal.valueOf(source.longValue());
		});

		converter.addDynamicTwoStageConverter(Number.class, String.class, InputStream.class);

		//
		// Date, Timestamp ->
		//

		converter.addConverter(Timestamp.class, Date.class, source -> new Date(source.getTime()));

		converter.addConverter(Date.class, Number.class, source -> Long.valueOf(source.getTime()));

		converter.addConverter(Date.class, String.class, source -> {
			try {
				return ISO8601DateFormat.format(source);
			} catch (RuntimeException e) {
				throw new TypeConversionException("Failed to convert date " + source + " to string", e);
			}
		});

		converter.addConverter(Date.class, Calendar.class, source -> {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(source);
			return calendar;
		});

		converter.addDynamicTwoStageConverter(Date.class, String.class, InputStream.class);

		//
		// java.time.OffsetDateTime
		//

		converter.addConverter(OffsetDateTime.class, Date.class, DateUtil::toDate);

		converter.addConverter(OffsetDateTime.class, String.class, source -> source.toZonedDateTime().toString());

		converter.addConverter(ZonedDateTime.class, Date.class, DateUtil::toDate);

		converter.addConverter(ZonedDateTime.class, String.class, source -> source.toString());

		//
		// Boolean ->
		//

		converter.addConverter(Boolean.class, Long.class, source -> source.booleanValue() ? LONG_TRUE : LONG_FALSE);

		converter.addConverter(Boolean.class, String.class, source -> source.toString());

		converter.addDynamicTwoStageConverter(Boolean.class, String.class, InputStream.class);

		//
		// Character ->
		//

		converter.addConverter(Character.class, String.class, source -> source.toString());

		converter.addDynamicTwoStageConverter(Character.class, String.class, InputStream.class);

		//
		// Byte
		//

		converter.addConverter(Byte.class, String.class, source -> source.toString());

		converter.addDynamicTwoStageConverter(Byte.class, String.class, InputStream.class);

		//
		// Short
		//

		converter.addConverter(Short.class, String.class, source -> source.toString());

		converter.addDynamicTwoStageConverter(Short.class, String.class, InputStream.class);

		//
		// Integer
		//

		converter.addConverter(Integer.class, String.class, source -> source.toString());

		converter.addDynamicTwoStageConverter(Integer.class, String.class, InputStream.class);

		//
		// Long
		//

		converter.addConverter(Long.class, String.class, source -> source.toString());

		converter.addDynamicTwoStageConverter(Long.class, String.class, InputStream.class);

		//
		// Float
		//

		converter.addConverter(Float.class, String.class, source -> source.toString());

		converter.addDynamicTwoStageConverter(Float.class, String.class, InputStream.class);

		//
		// Double
		//

		converter.addConverter(Double.class, String.class, source -> source.toString());

		converter.addDynamicTwoStageConverter(Double.class, String.class, InputStream.class);

		//
		// BigInteger
		//

		converter.addConverter(BigInteger.class, String.class, source -> source.toString());

		converter.addDynamicTwoStageConverter(BigInteger.class, String.class, InputStream.class);

		//
		// Calendar
		//

		converter.addConverter(Calendar.class, Date.class, source -> source.getTime());

		converter.addConverter(Calendar.class, String.class, source -> {
			try {
				return ISO8601DateFormat.format(source);
			} catch (RuntimeException e) {
				throw new TypeConversionException("Failed to convert date " + source + " to string", e);
			}
		});

		//
		// BigDecimal
		//

		converter.addConverter(BigDecimal.class, String.class, BigDecimal::toString);
		converter.addDynamicTwoStageConverter(BigDecimal.class, String.class, InputStream.class);

		//
		// List converters
		//

		// list to to string conversions
		// we cannot register just to List interface but to concrete implementations and we should
		// register at least the 2 most used implementations
		converter.addConverter(ArrayList.class, String.class, new CollectionToStringConverter<>(converter));
		converter.addConverter(LinkedList.class, String.class, new CollectionToStringConverter<>(converter));
		converter.addConverter(AbstractList.class, String.class, new CollectionToStringConverter<>(converter));
		converter.addConverter(HashSet.class, String.class, new CollectionToStringConverter<>(converter));
		converter.addConverter(LinkedHashSet.class, String.class, new CollectionToStringConverter<>(converter));
		converter.addConverter(String.class, List.class, source -> {
			String[] split = source.split(",");
			return new ArrayList<>(Arrays.asList(split));
		});
		converter.addConverter(JSONArray.class, List.class, source -> {
			if (source == null) {
				return null;
			}
			try {
				List<Object> result = new ArrayList<>(source.length());
				for (int i = 0; i < source.length(); i++) {
					result.add(source.get(i));
				}
				return result;
			} catch (JSONException e) {
				LOGGER.warn("Failed to get the json array value due to", e);
			}
			return new ArrayList<>(1);
		});

		//
		// Input Stream
		//

		converter.addConverter(InputStream.class, String.class, source -> {
			try (InputStream input = source) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buffer = new byte[8192];
				int read;
				while ((read = input.read(buffer)) > 0) {
					out.write(buffer, 0, read);
				}
				byte[] data = out.toByteArray();
				return new String(data, UTF_8);
			} catch (UnsupportedEncodingException e1) {
				throw new TypeConversionException("Cannot convert input stream to String.", e1);
			} catch (IOException e2) {
				throw new TypeConversionException("Conversion from stream to string failed", e2);
			}
		});

		converter.addDynamicTwoStageConverter(InputStream.class, String.class, Date.class);

		converter.addDynamicTwoStageConverter(InputStream.class, String.class, Double.class);

		converter.addDynamicTwoStageConverter(InputStream.class, String.class, Long.class);

		converter.addDynamicTwoStageConverter(InputStream.class, String.class, Boolean.class);

		converter.addConverter(DateRange.class, String.class, new Converter<DateRange, String>() {
			private static final String DATE_FROM_SUFFIX = "T00:00:00";
			/** The Constant DATE_SUFFIX. */
			private static final String DATE_TO_SUFFIX = "T23:59:59";
			/** example 2012-09-26T00:00:00. */
			public final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

			@Override
			public String convert(DateRange value) {
				return createDateRange(value.getFirst(), value.getSecond());
			}

			/**
			 * Creates the range.
			 *
			 * @param from
			 *            the start date
			 * @param to
			 *            the end date
			 * @return the string
			 */
			private String createDateRange(Serializable from, Serializable to) {
				String result = null;
				if (from != null && !from.toString().isEmpty()) {
					String fromData = DATE_FORMAT.format(from);
					if (to != null && !to.toString().isEmpty()) {
						result = "[\"" + fromData + DATE_FROM_SUFFIX + "\" TO \"" + DATE_FORMAT.format(to)
								+ DATE_TO_SUFFIX + "\"]";
					} else {
						result = "[\"" + fromData + DATE_FROM_SUFFIX + "\" TO MAX]";
					}
				} else if (to != null && !to.toString().isEmpty()) {
					String format = DATE_FORMAT.format(to) + DATE_TO_SUFFIX;
					result = "[MIN TO \"" + format + "\"]";
				}
				return result;

			}
		});

		converter.addConverter(JSONObject.class, String.class, (json) -> json.toString());
	}

}
