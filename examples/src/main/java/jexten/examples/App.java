package jexten.examples;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;
import jexten.ExtensionManager;
import jexten.maven.MavenArtifactResolver;
import jexten.plugin.*;
import jexten.plugin.tree.AnsiModuleTreeDescriptionVisitor;

public class App {


	private static final Path LOCAL_MAVEN =
		Path.of(System.getProperty("user.home")).resolve(".m2/repository");

	private static final Path PLUGINS_PATH = Path.of("build/plugins").toAbsolutePath();


	public static void main(String[] args) throws IOException {
		if (!App.class.getModule().isNamed()) {
			throw new PluginException("Application module is unnamed!");
		}
		deleteFolder(PLUGINS_PATH);
		var pluginStore = PluginStore.builder()
			.module(App.class.getModule())
			.warehousePath(PLUGINS_PATH)
			.artifactResolver(
				MavenArtifactResolver.builder()
					// we set the local maven repo as remote as well, in order to get
					// the example artifacts previously installed:
					.addRemoteRepository("local",LOCAL_MAVEN.toUri())
					.build()
			)
			.build();


		System.out.println(pluginStore.moduleLayerTree().description());

		// install plugin from zip file
		pluginStore.installPluginFromZipFile(Path.of("plugin-a/build/plugin-a-1.0.0.zip"));
		System.out.println(pluginStore.moduleLayerTree().description());

		// retrieve the extension
		var extensionManager = ExtensionManager.create(pluginStore);
		extensionManager.getExtensions(Greeting.class)
			.forEach(it -> System.out.println(it.greet()));

		// uninstall plugin
		pluginStore.removePlugin(PluginID.of("jexten.example:plugin-a"));
		System.out.println(pluginStore.moduleLayerTree().description());

		// retrieve the extension
		extensionManager.getExtensions(Greeting.class)
			.forEach(it -> System.out.println(it.greet()));

		// install plugin from jar
		pluginStore.installPluginFromJar(Path.of("plugin-a/build/libs/plugin-a-1.0.0.jar"));
		System.out.println(pluginStore.moduleLayerTree().description());

		System.out.println(new AnsiModuleTreeDescriptionVisitor().toString(pluginStore.moduleLayerTree()));
	}



	private static void deleteFolder(Path folder) throws IOException {
		if (Files.notExists(folder)) {
			return;
		}
		try (Stream<Path> pathStream = Files.walk(folder)) {
			pathStream.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
		}
	}

}
