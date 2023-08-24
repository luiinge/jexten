// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.plugin;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;

import org.slf4j.*;

public class PluginStoreBuilder {

    private ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
    private Predicate<ArtifactID> pluginFilter = it -> true;
    private String application;
    private Path warehousePath;
    private Logger logger;
    private ArtifactResolver artifactResolver;


    public PluginStoreBuilder module(Module module) {
        if (module == null) {
            throw new PluginException("Module cannot be null");
        }
        if (!module.isNamed()) {
            throw new PluginException("Unnamed module cannot be used as application module");
        }
        this.application = module.getName();
        this.parentClassLoader = module.getClassLoader();
        return this;
    }


    public PluginStoreBuilder application(String application) {
        this.application = application;
        return this;
    }


    public PluginStoreBuilder warehousePath(Path location) {
        this.warehousePath = location;
        return this;
    }


    public PluginStoreBuilder logger(Logger logger) {
        this.logger = logger;
        return this;
    }


    public PluginStoreBuilder parentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        return this;
    }


    public PluginStoreBuilder pluginFilter(Predicate<ArtifactID> pluginFilter) {
        this.pluginFilter = pluginFilter;
        return this;
    }


    public PluginStoreBuilder artifactResolver(ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
        return this;
    }


    public PluginStore build() {
        logger = Objects.requireNonNullElseGet(logger, ()->LoggerFactory.getLogger(application));
        return new PluginStore(
            Objects.requireNonNull(parentClassLoader,"parentClassLoader cannot be null"),
            Objects.requireNonNull(application, "application cannot be null"),
            Objects.requireNonNull(warehousePath, "location cannot be null"),
            logger,
            artifactResolver
        );
    }

}
