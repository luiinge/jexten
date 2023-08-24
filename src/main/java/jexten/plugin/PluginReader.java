// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.plugin;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.slf4j.*;

/**
 * This class creates standalone plugins from a specified path.
 * <p>
 * Standalone plugins are distributed as single zip files with every
 * dependency included within. They are suitable for offline installations.
 */
public record PluginReader(Logger logger)  {


	private static final Map<PluginZipFile,Plugin> cache = new HashMap<>();



	public Optional<Plugin> readPlugin(PluginZipFile zipFile) {

		try {

			if (cache.containsKey(zipFile)) {
				return Optional.of(cache.get(zipFile));
			}

			Path temporaryPath = Files.createTempDirectory("jexten-plugin");
			zipFile.unzip(temporaryPath);
			logger.debug("plugin {} unzipped in {}", zipFile.pluginID(), temporaryPath);

			try (var files = Files.list(temporaryPath)) {
				Plugin plugin = new Plugin(
					zipFile.pluginManifest(),
					zipFile,
					files.filter(file -> file.toString().endsWith(".jar")).toList(),
					List.of(),
					logger
				);
				cache.put(zipFile, plugin);
				return Optional.of(plugin);
			}

		} catch (IOException | RuntimeException e) {
			logger.error(
				"Invalid plugin file {}: {}",
				zipFile.path().getFileName(),
				e.getMessage()
			);
			logger.debug("",e);
			return Optional.empty();
		}
	}

}
