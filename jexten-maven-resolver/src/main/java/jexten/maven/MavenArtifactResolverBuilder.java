// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.maven;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.slf4j.*;

public class MavenArtifactResolverBuilder {

	private Path localRepositoryPath;
	private final Map<String, URI> remoteRepositories = new HashMap<>();
	private Properties configuration;
	private Logger logger;


	public MavenArtifactResolverBuilder localRepositoryPath(Path localRepositoryPath) {
		this.localRepositoryPath = localRepositoryPath;
		return this;
	}


	public MavenArtifactResolverBuilder fetcherConfiguration(Properties configuration) {
		this.configuration = configuration;
		return this;
	}


	public MavenArtifactResolverBuilder fetcherConfiguration(Map<String,String> configuration) {
		this.configuration = new Properties();
		this.configuration.putAll(configuration);
		return this;
	}


	public MavenArtifactResolverBuilder logger(Logger logger) {
		this.logger = logger;
		return this;
	}


	public MavenArtifactResolverBuilder addRemoteRepository(String name, URI uri) {
		this.remoteRepositories.put(name,uri);
		return this;
	}


	public MavenArtifactResolver build() {
		return new MavenArtifactResolver(
			Objects.requireNonNullElse(
				localRepositoryPath,
				Path.of(System.getProperty("user.home")).resolve(".m2/repository")
			),
			remoteRepositories,
			Objects.requireNonNullElseGet(configuration,Properties::new),
			Objects.requireNonNullElse(logger, LoggerFactory.getLogger(MavenArtifactResolver.class))
		);
	}

}
