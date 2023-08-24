package jexten.plugin;

import java.lang.module.*;
import java.nio.file.Path;
import java.util.*;

import java.util.stream.Stream;
import jexten.Version;
import org.slf4j.Logger;

/**
 * This class represents a valid plugin that has been catalogued by a {@link PluginStore}.
 * <p>
 * It provides methods to build a {@link ModuleLayer} suitable for loading the classes
 * contained within the plugin as well as its dependencies.
 */
public class Plugin {

	private final PluginManifest manifest;
	private final PluginZipFile zipFile;
	private final ModuleFinder moduleFinder;
	private final List<Path> primaryPaths;
	private final Set<ModuleReference> moduleReferences;
	private final Logger logger;



	public Plugin(
		PluginManifest manifest,
		PluginZipFile zipFile,
		List<Path> primaryPaths,
		List<Path> additionalPaths,
		Logger logger
	) {
		this.manifest = manifest;
		this.zipFile = zipFile;
		this.primaryPaths = primaryPaths;
		this.moduleFinder = moduleFinderOf(primaryPaths,additionalPaths);
		this.logger = logger;
		this.moduleReferences = moduleFinder.findAll();
		if (this.moduleReferences.isEmpty()) {
			throw new PluginException("Cannot find the module reference for plugin {}", id());
		}
	}

	public PluginID id() {
		return manifest.id();
	}

	public Version version() {
		return manifest.version();
	}

	public ArtifactID artifactID() {
		return manifest.artifactID();
	}

	public PluginManifest manifest() {
		return manifest;
	}

	public List<Path> primaryPaths() {
		return primaryPaths;
	}

	public PluginZipFile zipFile() {
		return zipFile;
	}


	/**
	 * Compute a list with every Java module used by the plugin, excluding the modules
	 * that are already used by the parent layer.
	 */
	public List<String> moduleNames(ModuleLayer parentLayer) {
		var parentModules = parentLayer.modules().stream().map(Module::getName).toList();
		return moduleReferences
			.stream()
			.map(ModuleReference::descriptor)
			.map(ModuleDescriptor::name)
			.filter(name -> !parentModules.contains(name))
			.distinct()
			.toList();
	}



	public boolean isHostedBy(ModuleLayer moduleLayer) {
		return moduleLayer.modules().stream()
			.map(Module::getName)
			.anyMatch(manifest.parentModule()::equals);
	}


	/**
	 * Build the Java {@link ModuleLayer} that will be used to load the classes of
	 * the plugin.
	 * @return Either the module layer or an empty optional if it could not be created for
	 *         any reason
	 */
	public Optional<ModuleLayer> buildModuleLayer(
		ModuleLayer parentLayer,
		ClassLoader parentClassLoader
	) {

		logger.debug("building module layer for {} with modules: {}",id(),moduleNames(parentLayer));

		try {
			var moduleLayer = Optional.of(parentLayer.defineModulesWithOneLoader(
				parentLayer.configuration().resolve(
					this.moduleFinder,
					ModuleFinder.of(),
					moduleNames(parentLayer)
				),
				parentClassLoader
			));
			logger.info("Plugin {} prepared.", id());
			return moduleLayer;

		} catch (RuntimeException e) {
			logger.error("Cannot build the module layer of plugin {} : {}", this, e.getMessage());
			logger.debug("", e);
			return Optional.empty();
		}
	}


	@Override
	public String toString() {
		return id().toString();
	}


	@Override
	public int hashCode() {
		return manifest.hashCode();
	}


	@Override
	public boolean equals(Object object) {
		return
			(object instanceof Plugin other) &&
			(other == this || Objects.equals(manifest, other.manifest));
	}


	private static ModuleFinder moduleFinderOf(List<Path> paths, List<Path> dependencyPaths) {
		return ModuleFinder.of(
			Stream.concat(paths.stream(),dependencyPaths.stream()).toArray(Path[]::new)
		);
	}




}