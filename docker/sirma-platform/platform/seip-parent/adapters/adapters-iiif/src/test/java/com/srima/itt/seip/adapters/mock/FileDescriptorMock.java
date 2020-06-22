package com.srima.itt.seip.adapters.mock;

import java.io.InputStream;

import com.sirma.itt.seip.io.FileDescriptor;

/**
 * File Descriptor mock which provides input stream to a particular source file.
 *
 * @author Nikolay Ch
 */
public class FileDescriptorMock implements FileDescriptor {

	private static final long serialVersionUID = 1L;
	private InputStream inStream;

	public void setInputStream(InputStream stream) {
		inStream = stream;
	}
	@Override
	public String getId() {
		return "mockftpserver.png";
	}

	@Override
	public String getContainerId() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return inStream;
	}

	@Override
	public void close() {
		// nothing to do
	}

}
