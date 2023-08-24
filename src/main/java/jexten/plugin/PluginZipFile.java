// Copyright  (c) 2021 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;


public class PluginZipFile {


	public static final String PLUGIN_PROPERTIES_FILE = "plugin.properties";


	public static PluginZipFile read(Path path) {
		if (!path.toString().endsWith(".zip")) {
			throw new PluginException(
				"Invalid format of plugin file {} , zip expected",
				path.getFileName()
			);
		}
		return new PluginZipFile(path).read();
	}


	public static PluginZipFile create(Path path, Path mainArtifact, List<Path> dependencies) {
		return new PluginZipFile(path).create(mainArtifact,dependencies);
	}

	private final Path path;
	private PluginManifest pluginManifest;


	private PluginZipFile(Path path) {
		this.path = path;
	}


	private PluginZipFile read() {
		this.pluginManifest = locatePluginManifest(path);
		return this;
	}


	private PluginZipFile create(Path mainArtifact, List<Path> dependencies) {
		this.pluginManifest = readPluginManifestFromJar(mainArtifact);
		try (var output = new ZipOutputStream(new FileOutputStream(path.toFile()))) {
			output.putNextEntry(new ZipEntry(PLUGIN_PROPERTIES_FILE));
			for (var e : pluginManifest.asMap().entrySet()) {
				writeLine(output, e.getKey()+": "+e.getValue());
			}
			output.putNextEntry(new ZipEntry(mainArtifact.toFile().getName()));
			Files.copy(mainArtifact, output);
			for (Path dependency : dependencies) {
				output.putNextEntry(new ZipEntry(dependency.toFile().getName()));
				Files.copy(dependency, output);
			}
			return this;
		} catch (IOException | RuntimeException e) {
			throw PluginException.wrapper(e);
	    }
	}



	public static PluginManifest readPluginManifestFromJar(Path jar) {
		try (var jarFile = new JarFile(jar.toFile())) {
			var jarManifest = jarFile.getManifest();
			if (jarManifest == null) {
				throw new PluginException("Jar Manifest is not present in file {}",jar);
			}
			return PluginManifest.of(jarManifestAsProperties(jarManifest));
		} catch (IOException | RuntimeException e) {
			throw new PluginException(
				"Cannot load distributed plugin jar file {}: {}",
				jar.getFileName(),
				e.getMessage()
			);
		}
	}



	private static Properties jarManifestAsProperties(Manifest jarManifest) {
		Properties properties = new Properties();
		jarManifest.getMainAttributes().forEach(
			(key,value)-> properties.setProperty(key.toString(),value.toString())
		);
		return properties;
	}



	public void unzip(Path targetFolder) throws IOException {
		try (var zipFile = new ZipFile(path.toFile())) {
			zipFile.stream().forEach(zipEntry -> unzip(zipFile, zipEntry, targetFolder));
		}
	}


	public PluginZipFile copyTo(Path targetFolder) throws IOException {
		Path newPath = targetFolder.resolve(path.getFileName());
		Files.copy(path,newPath);
		return PluginZipFile.read(newPath);
	}


	public PluginID pluginID() {
		return pluginManifest.id();
	}


	public PluginManifest pluginManifest() {
		return Objects.requireNonNull(pluginManifest);
	}


	public Path path() {
		return path;
	}


	private static PluginManifest locatePluginManifest(Path path) {
		try (ZipFile file = new ZipFile(path.toFile())) {

			var pluginManifestZipEntry = file
				.stream()
				.filter(zipEntry -> !zipEntry.isDirectory())
				.filter(PluginZipFile::isPluginManifestEntry)
				.findFirst()
				.orElseThrow(() -> new PluginException("Plugin manifest not present"));

			try (var entryInputStream = file.getInputStream(pluginManifestZipEntry)) {
				var properties = new Properties();
				properties.load(entryInputStream);
				return PluginManifest.of(properties);
			}

		} catch (IOException | RuntimeException e) {
			throw PluginException.wrapper(e);
		}
	}


	private static boolean isPluginManifestEntry(ZipEntry zipEntry) {
		return new File(zipEntry.getName()).getName().equals(PLUGIN_PROPERTIES_FILE);
	}


	private static void writeLine(ZipOutputStream output, String string) throws IOException {
		byte[] data = (string+"\n").getBytes(StandardCharsets.UTF_8);
		output.write(data);
	}


	private void unzip(ZipFile zipFile, ZipEntry zipEntry, Path targetFolder) {
		try {
			boolean isDirectory = zipEntry.getName().endsWith(File.separator);
			Path newPath = zipSlipProtect(zipEntry, targetFolder);
			if (isDirectory) {
				Files.createDirectories(newPath);
			} else {
				unzipEntry(zipFile, zipEntry, newPath);
			}
		} catch (IOException e) {
			throw PluginException.wrapper(e);
		}
	}


	private void unzipEntry(ZipFile zipFile, ZipEntry zipEntry, Path newPath) throws IOException {
		if (newPath.getParent() != null && Files.notExists(newPath.getParent())) {
			Files.createDirectories(newPath.getParent());
		}
		Files.copy(
			zipFile.getInputStream(zipEntry),
			newPath,
			StandardCopyOption.REPLACE_EXISTING
		);
	}


	private Path zipSlipProtect(ZipEntry zipEntry, Path targetFolder) throws IOException {
		Path targetDirResolved = targetFolder.resolve(zipEntry.getName());
		Path normalizePath = targetDirResolved.normalize();
		if (!normalizePath.startsWith(targetFolder)) {
			throw new IOException("Bad zip entry: " + zipEntry.getName());
		}
		return normalizePath;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PluginZipFile that = (PluginZipFile) o;
		return path.equals(that.path) && pluginManifest.equals(that.pluginManifest);
	}


	@Override
	public int hashCode() {
		return Objects.hash(path, pluginManifest);
	}

}
