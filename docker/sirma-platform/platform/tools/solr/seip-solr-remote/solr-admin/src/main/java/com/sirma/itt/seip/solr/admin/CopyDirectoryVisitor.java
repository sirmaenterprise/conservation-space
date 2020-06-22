package com.sirma.itt.seip.solr.admin;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * The CopyDirectoryVisitor copy each file recursivelly from a starting
 * directory.
 */
public class CopyDirectoryVisitor extends SimpleFileVisitor<Path> {
	private Path fromPath;
	private Path toPath;
	private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;

	/**
	 * Instantiates a new copy dir visitor.
	 *
	 * @param fromPath
	 *            the from path
	 * @param toPath
	 *            the to path
	 */
	public CopyDirectoryVisitor(Path fromPath, Path toPath) {
		this.fromPath = fromPath;
		this.toPath = toPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Path targetPath = toPath.resolve(fromPath.relativize(dir));
		if (!Files.exists(targetPath)) {
			Files.createDirectory(targetPath);
		}
		return FileVisitResult.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
		return FileVisitResult.CONTINUE;
	}
}