// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.maven;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;
import jexten.plugin.*;
import maven.fetcher.*;
import org.slf4j.Logger;



public class MavenArtifactResolver implements ArtifactResolver {


    public static MavenArtifactResolverBuilder builder() {
        return new MavenArtifactResolverBuilder();
    }


    private final MavenFetcher mavenFetcher;
    private final Logger logger;

    public MavenArtifactResolver(
        Path localRepositoryPath,
        Map<String,URI> remoteRepositories,
        Properties configuration,
        Logger logger
    ) {
        this.mavenFetcher = new MavenFetcher()
            .localRepositoryPath(localRepositoryPath)
            .config(configuration)
            .logger(logger);
        this.logger = logger;
        remoteRepositories.forEach((id,uri)-> mavenFetcher.addRemoteRepository(id,uri.toString()));
    }


    private Stream<ArtifactResolver.ArtifactPath> resolveCoordinates(List<String> coordinates) {

        var request = new MavenFetchRequest(coordinates).scopes("compile","provided");

        var result = mavenFetcher.fetchArtifacts(request);

        if (result.hasErrors()) {
            result.errors().forEach(error -> logger.error(error.getMessage()));
            throw new PluginException("One or more dependencies were not resolved");
        }

        if (logger.isDebugEnabled()) {
            var artifacts = result.allArtifacts()
                .map(it -> "  - "+it.coordinates())
                .collect(Collectors.joining("\n"));
            logger.debug("Artifacts fetched:\n{}", artifacts);
        }
        return result.allArtifacts()
            .map(it -> new ArtifactPath(ArtifactID.of(it.coordinates()), it.path()));
    }


    @Override
    public Stream<ArtifactResolver.ArtifactPath> resolveArtifacts(Collection<ArtifactID> artifactIDs) {

        List<String> coordinates = artifactIDs.stream()
            .map(ArtifactID::toString)
            .toList();

        return resolveCoordinates(coordinates);
    }


    @Override
    public Stream<ArtifactPath> resolvePlugins(Collection<PluginID> plugins) throws PluginException {
        List<String> coordinates = plugins.stream()
            .map(PluginID::toString)
            .toList();
        return resolveCoordinates(coordinates);
    }




}
