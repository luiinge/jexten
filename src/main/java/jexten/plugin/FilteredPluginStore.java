// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.plugin;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jexten.ModuleLayerProvider;

/**
 * This class acts as a decorator for an existing plugin warehouse,
 * filtering the plugins that would be used to provider the module layers
 */
public class FilteredPluginStore implements ModuleLayerProvider {

	private final PluginStore pluginStore;
	private final Predicate<Plugin> filter;


	public FilteredPluginStore(PluginStore pluginStore, Predicate<Plugin> filter) {
		this.pluginStore = pluginStore;
		this.filter = filter;
	}


	@Override
	public Stream<ModuleLayer> moduleLayers() {
		return pluginStore.moduleLayersByPlugin()
			.entrySet().stream()
			.filter(entry -> filter.test(entry.getKey()))
			.map(Map.Entry::getValue);
	}


}
