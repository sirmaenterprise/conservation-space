package com.sirma.itt.emf.semantic.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;

/**
 * Simple reader of the semantic debug dumped files
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/03/2019
 */
public class SemanticDebugDataExtractor {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.err.println("Specify path to the log file to read as argument");
			System.exit(-1);
		}

		BiConsumer<File, Consumer<Map<String, Map<String, List<String[]>>>>> handler = SemanticDebugDataExtractor::createQuery;
		Function<Map<String, Map<String, List<String[]>>>, String> statementParser = SemanticDebugDataExtractor::generateRDF;
		BiFunction<AtomicInteger, File, File> fileFactory = createFileFactory(".trig");

		for (String arg : args) {
			if (arg == null || arg.trim().isEmpty()) {
				continue;
			}
			File file = new File(arg.trim());
			if (file.isDirectory()) {
				File[] filesList = file.listFiles();
				if (filesList != null) {
					AtomicInteger idx = new AtomicInteger();
					for (File child : filesList) {
						handler.accept(child, createFileWriter(idx, child, statementParser, fileFactory));
					}
				}
			} else {
				handler.accept(file, createFileWriter(new AtomicInteger(), file, statementParser, fileFactory));
			}
		}
	}

	private static BiFunction<AtomicInteger, File, File> createFileFactory(String fileExtension) {
		return (idx, template) -> {
			File parentFile = template.getParentFile();
			String name = FilenameUtils.getName(template.getName());
			return new File(parentFile, name + "-" + idx.incrementAndGet() + fileExtension);
		};
	}

	private static Consumer<Map<String, Map<String, List<String[]>>>> createFileWriter(AtomicInteger idx, File child,
			Function<Map<String, Map<String, List<String[]>>>, String> statementParser,
			BiFunction<AtomicInteger, File, File> fileFactory) {
		return data -> {
			String rdf = statementParser.apply(data);
			File file = fileFactory.apply(idx, child);
			writeToFile(file, rdf);
		};
	}

	private static void writeToFile(File file, String rdf) {
		if (rdf.isEmpty()) {
			return;
		}
		try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			IOUtils.write(rdf, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createQuery(File file, Consumer<Map<String, Map<String, List<String[]>>>> dataConsumer) {
		System.out.println("Reading: " + file.getAbsolutePath());
		try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
			List<Object[]> list = (List<Object[]>) inputStream.readObject();
			for (Object[] row : list) {
				String op = row[0].toString();
				if (!SemanticOperationLogger.COMMIT_OPERATION.equals(op)) {
					continue;
				}
				if (!(row[1] instanceof List)) {
					continue;
				}
				Map<String, Map<String, List<String[]>>> statements = readCommitData(row[1]);
				if (!statements.isEmpty()) {
					dataConsumer.accept(statements);
				}
			}

		} catch (Exception e) {
			System.err.println("Invalid file: " + file.getAbsolutePath() + " : " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static Map<String, Map<String, List<String[]>>> readCommitData(Object o) {
		Map<String, Map<String, List<String[]>>> statements = new HashMap<>();
		List dataList = (List) o;
		for (Object rawData : dataList) {
			Map<String, Object> data = (Map<String, Object>) rawData;
			String type = data.get("type").toString();
			String subject = Objects.toString(data.get("subject"));
			String predicate = Objects.toString(data.get("predicate"));
			String object = Objects.toString(data.get("object"));
			statements.computeIfAbsent(type, t -> new HashMap<>())
					.computeIfAbsent(subject, s -> new LinkedList<>())
					.add(new String[] { subject, predicate, object });
		}
		return statements;
	}

	private static Predicate<Object[]> filterByTimestamp(String time) {
		return row -> {
			Object timestamp = null;
			if (row.length >= 4) {
				timestamp = row[3];
			}
			timestamp = readTimestamp(timestamp);
			return time.equals(timestamp);
		};
	}

	private static String generateSparql(Map<String, Map<String, List<String[]>>> statements) {
		Map<String, List<String[]>> remove = statements.getOrDefault("REMOVE", Collections.emptyMap());
		Map<String, List<String[]>> add = statements.getOrDefault("ADD", Collections.emptyMap());
		if (remove.isEmpty() && add.isEmpty()) {
			return "";
		}
		String removeStatements = remove.values()
				.stream()
				.flatMap(List::stream)
				.map(SemanticDebugDataExtractor::writeTripple)
				.collect(Collectors.joining(". \n"));

		String addStatements = add.values()
				.stream()
				.flatMap(List::stream)
				.map(SemanticDebugDataExtractor::writeTripple)
				.collect(Collectors.joining(". \n"));

		return String.format("DELETE {\n %s \n} INSERT {\n %s \n} WHERE {}", addStatements, removeStatements);
	}

	private static String generatePermissionSql(Map<String, Map<String, List<String[]>>> statements) {
		Map<String, List<String[]>> remove = statements.getOrDefault("REMOVE", Collections.emptyMap());
		Map<String, List<String[]>> add = statements.getOrDefault("ADD", Collections.emptyMap());
		if (remove.isEmpty() && add.isEmpty()) {
			return "";
		}
		String removeStatements = remove.values()
				.stream()
				.flatMap(List::stream)
				.filter(isMemberChanged())
				.map(SemanticDebugDataExtractor::writeTripple)
				.collect(Collectors.joining(". \n"));

		String addStatements = add.values()
				.stream()
				.flatMap(List::stream)
				.map(SemanticDebugDataExtractor::writeTripple)
				.collect(Collectors.joining(". \n"));

		return String.format("DELETE {\n %s \n} INSERT {\n %s \n} WHERE {}", addStatements, removeStatements);
	}

	private static Predicate<? super String[]> isMemberChanged() {
		return triple -> {
			String predicate = triple[1];
			return  predicate.equals(Proton.HAS_MEMBER.toString());
		};
	}

	private static String generateRDF(Map<String, Map<String, List<String[]>>> statements) {
		Map<String, List<String[]>> remove = statements.getOrDefault("REMOVE", Collections.emptyMap());
		String removeStatements = remove.values()
				.stream()
				.flatMap(List::stream)
				.map(SemanticDebugDataExtractor::writeTripple)
				.collect(Collectors.joining(". \n", "", ". "));

		return String.format("<%s> {\n%s\n}", EMF.DATA_CONTEXT, removeStatements);
	}

	@SuppressWarnings("unchecked")
	private static void dumpFile(File file, Consumer<Map<String, Map<String, List<String[]>>>> dataConsumer) {
		System.out.println("Reading: " + file.getAbsolutePath());
		try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
			List<Object[]> list = (List<Object[]>) inputStream.readObject();
			int rowNumber = 0;
			Map<String, AtomicInteger> stats = new HashMap<>();
			for (Object[] row : list) {
				String op = row[0].toString();
				stats.computeIfAbsent(op, o -> new AtomicInteger(0)).incrementAndGet();
				if (!SemanticOperationLogger.UPDATE_QUERY_OPERATION.equals(op)) {
					continue;
				}

				Object timestamp = null;
				if (row.length >= 4) {
					timestamp = row[3];
				}
				System.out.println(
						rowNumber++ + ": " + op + " : " + readTimestamp(timestamp) + " : " + row[2] + " : " + readData(
								row[1]));
			}
			System.out.println(stats);
		} catch (Exception e) {
			System.err.println("Invalid file: " + file.getAbsolutePath() + " : " + e.getMessage());
		}
	}

	private static String writeTripple(String[] strings) {
		String object = strings[2];
		String prefix = "";
		if ("null".equals(object)) {
			prefix = "# ";
		} else if (object.startsWith("http")) {
			object = "<" + object + ">";
		}
		return prefix + " <" + strings[0] + "> <" + strings[1] + "> " + object;
	}

	private static String readTimestamp(Object timestamp) {
		if (timestamp == null) {
			return "(no timestamp)";
		}
		return ISO8601DateFormat.format(new Date((Long) timestamp));
	}

	@SuppressWarnings("unchecked")
	private static Object readData(Object cell) {
		if (cell instanceof Map) {
			return Stream.of("type", "subject", "predicate", "object", "contexts")
					.map(((Map<String, Object>) cell)::get)
					.map(SemanticDebugDataExtractor::readData)
					.map(Object::toString)
					.collect(Collectors.joining(" "));
		} else if (cell instanceof Collection) {
			return ((Collection) cell).stream()
					.map(SemanticDebugDataExtractor::readData)
					.collect(Collectors.joining("," + System.lineSeparator() + "\t", "[", "]"));
		} else if (cell == null) {
			return "null";
		} else if (cell.getClass().isArray()) {
			return Arrays.toString((Object[]) cell);
		}
		return cell.toString();
	}
}
