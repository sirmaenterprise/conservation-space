package com.sirma.itt.seip.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;

import com.sirma.itt.seip.GenericProxy;

/**
 * Read only class for describing data access in different formats to remote systems.
 *
 * @author borislav banchev
 * @author BBonev
 */
public interface FileDescriptor extends Serializable {

	/**
	 * The id if the file.
	 *
	 * @return the id
	 */
	String getId();

	/**
	 * Gets the container id.
	 *
	 * @return the container id
	 */
	String getContainerId();

	/**
	 * Provides read access to the file identified by the current descriptor.
	 *
	 * @return the stream with the content
	 */
	InputStream getInputStream();

	/**
	 * Provides the original name of the file.
	 *
	 * @return the name of the file
	 */
	default String getFileName() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Length of the content represented by the current file descriptor. It unknown then the method may return -1.
	 *
	 * @return the length of the content provided by the input stream
	 */
	default long length() {
		return -1;
	}

	/**
	 * Loads the content provided by the {@link #getInputStream()} as UTF-8 string. The method closes the stream
	 * automatically.
	 * <p>
	 * The method is equivalent to: <code>asString(StandardCharsets.UTF_8);</code>
	 *
	 * @return the loaded string or <code>null</code> if the source was <code>null</code>.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	default String asString() throws IOException {
		return asString(StandardCharsets.UTF_8);
	}

	/**
	 * Loads the content provided by the {@link #getInputStream()} converted to text using the provided character
	 * encoding. If no encoding is provided then UTF-8 will be used. The method closes the stream automatically.
	 * <p>
	 * The method is equivalent to:
	 *
	 * <pre>
	 * <code> try (InputStream stream = getInputStream()) {
	 *    if (stream == null) {
	 *       return null;
	 *    }
	 *    return org.apache.commons.io.IOUtils.toString(stream, StandardCharsets.UTF_8);
	 * }</code>
	 * </pre>
	 *
	 * @param charset
	 *            the char set to use for conversion of the input stream bytes to characters. If null is passed then
	 *            UTF-8 will be used
	 * @return the loaded string or <code>null</code> if the source was <code>null</code>.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	default String asString(Charset charset) throws IOException {
		InputStream inputStream = getInputStream();
		if (inputStream == null) {
			return null;
		}
		try (InputStream stream = inputStream) {
			return IOUtils.toString(stream, charset == null ? StandardCharsets.UTF_8 : charset);
		}
	}

	/**
	 * Writes the content provided by the {@link #getInputStream()} to the given output. The source stream is closed at
	 * the end of the call.<br>
	 * <b> Note that the output is not closed by calling of this method.</b>
	 * <p>
	 * The method is equivalent to:
	 *
	 * <pre>
	 * <code> Objects.requireNonNull(outputStream, "Cannot write to null OutputStream");
	 * try (InputStream stream = getInputStream()) {
	 *    if (stream == null) {
	 *       return -1L;
	 *    }
	 *    return org.apache.commons.io.IOUtils.copyLarge(stream, outputStream);
	 * }</code>
	 * </pre>
	 *
	 * @param outputStream
	 *            the output stream to write the source to
	 * @return the number of bytes written to the output or -1 if the source was <code>null</code>.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	default long writeTo(OutputStream outputStream) throws IOException {
		Objects.requireNonNull(outputStream, "Cannot write to null OutputStream");
		InputStream inputStream = getInputStream();
		if (inputStream == null) {
			return -1L;
		}
		try (InputStream stream = inputStream) {
			return IOUtils.copyLarge(stream, outputStream);
		}
	}

	/**
	 * Writes the content provided by the {@link #getInputStream()} to the given file. The source stream is closed at
	 * the end of the call.<br>
	 * <p>
	 * The method is equivalent to:
	 *
	 * <pre>
	 * <code> Objects.requireNonNull(file, "Cannot write to null file");
	 * try (OutputStream output = new FileOutputStream(file)) {
	 *    return writeTo(output);
	 * }</code>
	 * </pre>
	 *
	 * @param file
	 *            the output file to write the source to
	 * @return the number of bytes written to the output or -1 if the source was <code>null</code>.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	default long writeTo(File file) throws IOException {
		Objects.requireNonNull(file, "Cannot write to null file");
		try (OutputStream output = new FileOutputStream(file)) {
			return writeTo(output);
		}
	}

	/**
	 * Can be used to clear descriptor resources. After calling this method all other methods are not guaranteed to work
	 * properly.
	 */
	void close();

	/**
	 * Creates base unnamed file descriptor that will report the {@link InputStream} provided by the method
	 * {@link #getInputStream()}.
	 *
	 * @param dataSupplier
	 *            the data supplier that will be used for stream producing. Required
	 * @param length
	 *            the content length of the provided stream
	 * @return the file descriptor
	 */
	static FileDescriptor create(Supplier<InputStream> dataSupplier, long length) {
		return new LazyDescriptor(null, dataSupplier, length);
	}

	/**
	 * Creates a lazy initializing file descriptor that will report name and {@link InputStream} by the methods
	 * {@link #getId()} and {@link #getInputStream()}.<br>
	 * Note that the returned descriptor may not be fully serializable if the given suppliers are not serializable.
	 *
	 * @param name
	 *            the name supplier to be used for name resolution. If <code>null</code> then <code>null</code> will be
	 *            returned by the method {@link FileDescriptor#getId()}
	 * @param dataSupplier
	 *            the data supplier that will be used for stream producing. If <code>null</code> then <code>null</code>
	 *            will be returned by the method {@link FileDescriptor#getInputStream()}
	 * @param length
	 *            the content length of the provided stream
	 * @return the file descriptor
	 */
	static FileDescriptor create(Supplier<String> name, Supplier<InputStream> dataSupplier, long length) {
		return new LazyDescriptor(name, dataSupplier, length);
	}

	/**
	 * Creates a base file descriptor that will report the given name and {@link InputStream}s provided by the
	 * respective methods {@link #getId()} and {@link #getInputStream()}
	 *
	 * @param name
	 *            the name to be returned. Optional
	 * @param dataSupplier
	 *            the data supplier that will be used for stream producing. Required
	 * @param length
	 *            the content length of the provided stream
	 * @return the file descriptor
	 */
	static FileDescriptor create(String name, Supplier<InputStream> dataSupplier, long length) {
		return new LazyDescriptor(() -> name, dataSupplier, length);
	}

	/**
	 * Enable counting of the transferred data via this {@link FileDescriptor}. The returned instance allows for
	 * monitoring of the transferred bytes data via the {@link InputStream} returned by the method
	 * {@link #getInputStream()}. Note that the stream must be consumed for the correct count to be returned. If the
	 * stream is not consumed then the method {@link CountingFileDescriptor#getTransferredBytes()} will return
	 * {@code -1L}.
	 *
	 * @param descriptor
	 *            the descriptor to wrap
	 * @return the counting file descriptor or <code>null</code> if passed <code>null</code>. Note that if the passed
	 *         {@link FileDescriptor} is of instance {@link CountingFileDescriptor} then the same instance will be
	 *         returned
	 */
	static CountingFileDescriptor enableCounting(FileDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		if (descriptor instanceof CountingFileDescriptor) {
			return (CountingFileDescriptor) descriptor;
		}
		return new CountingFileDescriptor(descriptor);
	}

	/**
	 * Lazy initializing {@link FileDescriptor} that can return an id and {@link InputStream} provided by a
	 * {@link Supplier}s.
	 *
	 * @author BBbonev
	 */
	class LazyDescriptor implements FileDescriptor {
		private static final long serialVersionUID = 2108054711682525982L;
		private Supplier<InputStream> dataSupplier;
		private Supplier<String> nameSupplier;
		private long length;

		/**
		 * Instantiates a new lazy descriptor that will return <code>null</code> from all it's methods.
		 */
		public LazyDescriptor() {
			this(null, null, -1);
		}

		/**
		 * Instantiates a new stream descriptor.
		 *
		 * @param length
		 *            the length
		 * @param nameSupplier
		 *            the name supplier
		 * @param dataSupplier
		 *            the data supplier
		 */
		public LazyDescriptor(Supplier<String> nameSupplier, Supplier<InputStream> dataSupplier, long length) {
			this.nameSupplier = nameSupplier == null ? () -> null : nameSupplier;
			this.dataSupplier = dataSupplier == null ? () -> null : dataSupplier;
			this.length = length;
		}

		@Override
		public String getId() {
			return nameSupplier.get();
		}

		@Override
		public String getContainerId() {
			return null;
		}

		@Override
		public InputStream getInputStream() {
			return dataSupplier.get();
		}

		@Override
		public long length() {
			return length;
		}

		@Override
		public void close() {
			// nothing to do
		}
	}

	/**
	 * {@link CountingFileDescriptor} is a proxy instance of {@link FileDescriptor} that allows monitoring of the
	 * transferred bytes via the {@link InputStream} return by the method {@link #getInputStream()}
	 *
	 * @author BBonev
	 */
	class CountingFileDescriptor implements FileDescriptor, GenericProxy<FileDescriptor> {
		private static final long serialVersionUID = 621969672142399951L;

		private final FileDescriptor delegate;
		private CountingInputStream countingStream;

		/**
		 * Instantiates a new counting file descriptor.
		 *
		 * @param delegate
		 *            the delegate
		 */
		public CountingFileDescriptor(FileDescriptor delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getId() {
			return delegate.getId();
		}

		@Override
		public String getContainerId() {
			return delegate.getContainerId();
		}

		@Override
		public InputStream getInputStream() {
			countingStream = new CountingInputStream(delegate.getInputStream());
			return countingStream;
		}

		@Override
		public void close() {
			delegate.close();
		}

		@Override
		public long length() {
			return delegate.length();
		}

		/**
		 * Gets the transferred bytes via this descriptor. The method returns the number of transferred bytes via last
		 * returned {@link InputStream} from the method {@link #getInputStream()}. If the stream is not consumed then
		 * the method will return {@code -1L}
		 *
		 * @return the transferred bytes or {@code -1L} if nothing is transferred, yet.
		 */
		public long getTransferredBytes() {
			return countingStream != null ? countingStream.getCount() : -1L;
		}

		@Override
		public FileDescriptor getTarget() {
			return delegate;
		}

		@Override
		public void setTarget(FileDescriptor target) {
			// nothing to do
		}

		@Override
		public FileDescriptor cloneProxy() {
			return new CountingFileDescriptor(delegate);
		}

		@Override
		public FileDescriptor createCopy() {
			return new CountingFileDescriptor(delegate);
		}

	}
}
