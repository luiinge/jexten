// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.plugin;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import jexten.*;
import jexten.internal.Lazy;
import jexten.plugin.tree.ModuleLayerTree;
import org.slf4j.Logger;


public class PluginStore implements ModuleLayerProvider {



	/**
	 * @return A new builder for {@link PluginStore} instances
	 */
	public static PluginStoreBuilder builder() {
		return new PluginStoreBuilder();
	}


	private record PluginModuleLayer(Plugin plugin, ModuleLayer moduleLayer) { }


	private record PluginMap (
		Map<PluginID, Plugin> pluginsByID,
		Map<Plugin,ModuleLayer> layersByPlugin,
		ModuleLayerTree layerTree
	) {
		public Optional<Plugin> get(PluginID id) {
			return Optional.ofNullable(pluginsByID.get(id));
		}
		public Optional<Version> getVersion(PluginID id) {
			return Optional.ofNullable(pluginsByID.get(id)).map(Plugin::version);
		}
		public Iterable<PluginID> ids() {
			return pluginsByID.keySet();
		}
	}


	private static final ModuleLayer bootLayer = ModuleLayer.boot();

	private final ClassLoader parentClassLoader;
	private final String applicationModule;
	private final Path storePath;
	private final Logger logger;
	private final PluginReader pluginReader;
	private final ArtifactResolver artifactResolver;

	private final Lazy<PluginMap,PluginException> pluginMap = Lazy.of(this::buildPluginMap);


	/*
	  This constructor is meant to be used only by PluginStoreBuilder
	*/
	PluginStore(
		ClassLoader parentClassLoader,
		String application,
		Path storePath,
		Logger logger,
		ArtifactResolver artifactResolver
	) {
		this.parentClassLoader = parentClassLoader;
		this.applicationModule = application;
		this.storePath = createStoreFolder(logger, storePath);
		this.logger = logger;
		this.artifactResolver = artifactResolver;
		this.pluginReader = new PluginReader(logger);
	}


	private static Path createStoreFolder(Logger logger, Path storePath) {
		try {
			if (Files.notExists(storePath)) {
				logger.info("Creating plugin store folder {} ...", storePath);
				Files.createDirectories(storePath);
			}
			return storePath;
		} catch (IOException e) {
			throw new PluginException("Cannot create plugin store folder: {}",e.getMessage(),e);
		}
	}


	@Override
	public Stream<ModuleLayer> moduleLayers() {
		return pluginMap.get().layersByPlugin.values().stream();
	}


	public Stream<Plugin> plugins() {
		return pluginMap.get().pluginsByID.values().stream();
	}


	/**
	 * @return a module layer tree that describes the layer composition
	 * regarding the different plugins allocated
	 */
	public ModuleLayerTree moduleLayerTree() {
		return this.pluginMap.get().layerTree;
	}


	public Map<Plugin,ModuleLayer> moduleLayersByPlugin() {
		return this.pluginMap.get().layersByPlugin;
	}



	public void removePlugin(PluginID pluginID) {
		var plugin = pluginMap.get().get(pluginID).orElseThrow(
			()->new PluginException("Plugin {} is not present", pluginID)
		);
		try {
			Files.delete(plugin.zipFile().path());
			logger.info("Removed plugin {}", pluginID);
			invalidateCachedPlugins();
		} catch (IOException e) {
			throw new PluginException(e,"Cannot remove plugin {}",pluginID);
		}

	}


	/**
	 * Install a new plugin from the given identifier.
	 * If the plugin is already present in the warehouse but a specific version is requested,
	 * it shall replace the previous version.
	 * @param pluginID The identifier of the plugin to install.
	 *                 Can o cannot include a specific version.
	 * @throws PluginException when the plugin could not be installed
	 */
	public void installPluginFromID(PluginID pluginID, Version requested, boolean force) throws PluginException {

		if (artifactResolver == null) {
			throw new PluginException("There is no artifact resolver configured");
		}
		Version current = pluginMap.get().getVersion(pluginID).orElse(null);
		Version latest = artifactResolver.latestVersion(pluginID)
			.orElseThrow(()->new PluginException("Cannot found latest version of {}", pluginID));

		if (current != null) {
			logger.info("Current version of plugin {} is {}", pluginID, current);
		}

		if (requested == null) {
			logger.info("Considering latest version {}", latest);
			requested = latest;
		}

		if (requested.equals(current)) {
			if (force) {
				logger.warn("Plugin {} is already version {}, reinstalling...", pluginID, requested);
				removePlugin(pluginID);
			} else {
				logger.warn("Plugin {} is already version {}, nothing to do.", pluginID ,requested);
				return;
			}
		}

		if (requested.compareTo(current) < 0) {
			if (force) {
				logger.warn("Plugin {} current version {} is newer than {}, downgrading...", pluginID, current, requested);
				removePlugin(pluginID);
			} else {
				logger.warn("Plugin {} current version {} is newer than {}, nothing to do.", pluginID, current, requested);
				return;
			}
		}

		var requestedID = pluginID.version(requested);
		resolveArtifact(requestedID);

	}




	/**
	 * Update all the plugins existing in the warehouse
	 */
	public void updatePlugins() {
		for (var pluginID : pluginMap.get().ids()) {
			try {
				updatePlugin(pluginID);
			} catch (RuntimeException e) {
				logger.error("Problem updating plugin {}: {}", pluginID, e.getMessage());
				logger.debug("",e);
			}
		}
	}



	/**
	 * Update a plugin from the given identifier.
	 *
	 * @param pluginID The identifier of the plugin to install.
	 *                 Cannot include a specific version.
	 * @throws PluginException when the plugin could not be installed
	 */
	public void updatePlugin(PluginID pluginID) throws PluginException {
		if (artifactResolver == null) {
			throw new PluginException("There is no artifact resolver configured");
		}
		Version current = pluginMap.get().getVersion(pluginID).orElseThrow(
			()->new PluginException("Plugin {} is not present", pluginID)
		);
		Version latest = artifactResolver.latestVersion(pluginID)
			.orElseThrow(()->new PluginException("Cannot found latest version of {}", pluginID));

		if (latest.compareTo(current) < 0) {
			return;
		}

		var requestedID = pluginID.version(latest);
		resolveArtifact(requestedID);
	}


	private void resolveArtifact(ArtifactID requestedID) {
		var artifacts = artifactResolver.resolveArtifact(requestedID).toList();
		var mainArtifact = artifacts.stream()
			.filter(it -> it.artifactID().equals(requestedID))
			.findAny()
			.map(ArtifactResolver.ArtifactPath::path)
			.orElseThrow(
				()->new PluginException("Artifact {} was not resolved", requestedID)
			);
		var dependencies = artifacts.stream()
			.filter(it -> !it.artifactID().equals(requestedID))
			.map(ArtifactResolver.ArtifactPath::path)
			.toList();

		Path zipFilePath = storePath.resolve(requestedID.name()+".zip");
		var pluginZipFile = PluginZipFile.create(zipFilePath,mainArtifact,dependencies);
		installPluginFromZipFile(pluginZipFile.path());
	}



	/**
	 * Install a new plugin from an existing file path
	 * If the plugin is already present in the warehouse,
	 * it shall replace the previous version.
	 * @param path The path of an existing plugin.
	 * @throws PluginException when the plugin could not be installed
	 */
	public void installPluginFromZipFile(Path path) throws PluginException {
		copyPluginZipFileToStore(PluginZipFile.read(path));
	}


	public void installPluginFromJar(Path path) throws PluginException {
		var pluginManifest = PluginZipFile.readPluginManifestFromJar(path);
		var dependencies = artifactResolver.resolveArtifacts(pluginManifest.dependencies())
			.map(ArtifactResolver.ArtifactPath::path)
			.toList();
		Path zipFilePath = storePath.resolve(pluginManifest.id().name()+"-"+pluginManifest.version()+".zip");
		var pluginZipFile = PluginZipFile.create(zipFilePath,path,dependencies);
		installPluginFromZipFile(pluginZipFile.path());
	}



	private void copyPluginZipFileToStore(PluginZipFile pluginZipFile) throws PluginException {
		try {
			pluginZipFile.copyTo(storePath);
			invalidateCachedPlugins();
		} catch (IOException e) {
		   throw new PluginException(
			   e,
			   "Cannot copy file {} to plugin store directory {}",
			   pluginZipFile.path(),
			   storePath
		   );
		}
	}





	private PluginMap buildPluginMap() {
		logger.debug("building plugin map...");
		try (var storeContents = Files.list(storePath)) {

			var pluginArtifacts = storeContents
				.map(this::readPluginZipFile)
				.flatMap(Optional::stream)
				.map(pluginReader::readPlugin)
				.flatMap(Optional::stream)
				.filter(this::validatePluginApplication)
				.collect(Collectors.toUnmodifiableMap(Plugin::artifactID, x->x));

			var layersByPlugin = computeModuleLayers(pluginArtifacts.values());
			var layerTree = new ModuleLayerTree(layersByPlugin);

			var pluginsByID = pluginArtifacts.values().stream()
				.collect(Collectors.toMap(Plugin::id,it->it));

			logger.debug("plugin map finished, {} plugins available", pluginArtifacts.size());
			return new PluginMap(pluginsByID, layersByPlugin, layerTree);

		} catch (IOException e) {
			throw PluginException.wrapper(e);
		}
	}



	private void invalidateCachedPlugins() {
		logger.debug("plugin cache invalidated due to plugins were added/removed");
		this.pluginMap.reset();
	}


	private Optional<PluginZipFile> readPluginZipFile(Path path) {
		try {
			return Optional.of(PluginZipFile.read(path));
		} catch (PluginException e) {
			logger.warn("File {} is not a valid plugin file",path);
			return Optional.empty();
		}
	}



	private boolean validatePluginApplication(Plugin plugin) {
		if (!plugin.manifest().applicationModule().equals(this.applicationModule)) {
			if (logger.isWarnEnabled()) {
				logger.warn(
					"Plugin {} not aimed to the application module '{}' but '{}'",
					plugin.id(),
					this.applicationModule,
					plugin.manifest().applicationModule()
				);
			}
			return false;
		}
		return true;
	}



	private Map<Plugin,ModuleLayer> computeModuleLayers(Collection<Plugin> plugins) {
		var layersByPlugin = new HashMap<Plugin,ModuleLayer>();
		Set<ModuleLayer> exploredLayers = new HashSet<>();
		ModuleLayer parentLayer = bootLayer;
		Map<Plugin,ModuleLayer> newLayers;
		do {
			newLayers = computeModuleLayers(parentLayer,plugins);
			exploredLayers.add(parentLayer);
			layersByPlugin.putAll(newLayers);
			parentLayer = layersByPlugin.values().stream()
				.filter(it -> !exploredLayers.contains(it))
				.findAny()
				.orElse(null);
		} while (parentLayer != null);
		return Map.copyOf(layersByPlugin);
	}



	private Map<Plugin,ModuleLayer> computeModuleLayers(
		ModuleLayer parentLayer,
		Collection<Plugin> plugins
	) {
		return plugins.stream()
		.filter(plugin -> plugin.isHostedBy(parentLayer))
		.map(plugin -> buildModuleLayer(plugin, parentLayer))
		.flatMap(Optional::stream)
		.collect(Collectors.toMap(PluginModuleLayer::plugin, PluginModuleLayer::moduleLayer));
	}


	private Optional<PluginModuleLayer> buildModuleLayer(Plugin plugin, ModuleLayer parentLayer) {
		return plugin
			.buildModuleLayer(parentLayer, parentClassLoader)
			.map(moduleLayer -> new PluginModuleLayer(plugin, moduleLayer));
	}







}
