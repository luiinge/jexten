package jexten.maven;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class MavenArtifactResolverTest {

	private static final Path LOCAL_MAVEN =
		Path.of(System.getProperty("user.home")).resolve(".m2/repository");


	@Test
	void test() {
		MavenArtifactResolver.builder()
			// we set the local maven repo as remote as well, in order to get
			// the example artifacts previously installed:
			.addRemoteRepository("local",LOCAL_MAVEN.toUri())
			.build();
	}

}
