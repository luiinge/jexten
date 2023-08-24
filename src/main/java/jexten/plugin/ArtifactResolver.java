// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.plugin;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import jexten.Version;

/**
 * A plugin resolver is responsible for resolving the required files for a plugin and its
 * dependencies.
 */
public interface ArtifactResolver {

    record ArtifactPath (ArtifactID artifactID, Path path) { }


    /**
     * Resolve a plugin artifact in order to get the file paths of the
     * plugin and its dependencies within the local file system.
     * <p>
     * If the required artifacts are not present, the repository would
     * try to fetch them from online sources.
     * @param artifacts The identifier of the artifacts to resolve
     * @throws PluginException if one or more artifacts could not be resolved
     */
    Stream<ArtifactPath> resolveArtifacts(Collection<ArtifactID> artifacts) throws PluginException;

    Stream<ArtifactPath> resolvePlugins(Collection<PluginID> artifacts) throws PluginException;

    default Stream<ArtifactPath> resolvePlugin(PluginID pluginID) {
        return resolvePlugins(List.of(pluginID));
    }

    default Optional<Version> latestVersion(PluginID pluginID) {
        return resolvePlugin(pluginID)
            .map(ArtifactPath::artifactID)
            .filter(id -> id.pluginID().equals(pluginID))
            .map(ArtifactID::version)
            .findAny();
    }


    default Stream<ArtifactPath> resolveArtifact(ArtifactID artifactID) {
        return resolveArtifacts(List.of(artifactID));
    }

}
