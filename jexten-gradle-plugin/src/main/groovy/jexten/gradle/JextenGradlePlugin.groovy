package jexten.gradle

import jexten.Version
import jexten.plugin.ArtifactID
import jexten.plugin.PluginID
import jexten.plugin.PluginManifest
import jexten.plugin.PluginZipFile
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.jvm.tasks.Jar

import java.util.stream.Collectors

class JextenGradlePlugin implements Plugin<Project> {


    static final String CONFIGURATION_JEXTEN_APPLICATION = 'jextenApplication'
    static final String CONFIGURATION_JEXTEN_PARENT = 'jextenParent'



    @Override
    void apply(Project project) {

        def properties = project.extensions.create('jextenPlugin', JextenPluginExtension)
        properties.description.convention(project.description)


        project.plugins.apply 'java-library'

        def jextenApplication = project.configurations.create(CONFIGURATION_JEXTEN_APPLICATION)
        project.configurations.findByName('api').extendsFrom(jextenApplication)

        def jextenParent = project.configurations.create(CONFIGURATION_JEXTEN_PARENT)
        project.configurations.findByName('api').extendsFrom(jextenParent)


        project.task('prepareManifest') {
            group = 'build'
            doFirst {
                def helper = new ProjectHelper(project)
                project.tasks.withType(Jar) {
                    def jarFile = archiveFile.get().asFile.name
                    def pluginManifest = pluginManifest(helper, properties, jarFile).asMap()
                    manifest {
                       attributes(pluginManifest)
                    }
                }
            }
        }

        project.tasks.getByName('jar').dependsOn('prepareManifest')

        project.task('collectPluginDependencies') {
            group = 'build'
            dependsOn 'jar'
            doLast {
                def helper = new ProjectHelper(project)
                def target = project.layout.buildDirectory.dir('pluginLib')
                def files = helper.filteredDependencyFiles()
                Jar jar = project.tasks.findByName('jar') as Jar
                files.add jar.outputs.files.singleFile
                project.copy {
                    from files
                    into target
                }
                def propertiesFile = new File(target.get().asFile, PluginZipFile.PLUGIN_PROPERTIES_FILE)
                jar.manifest.writeTo propertiesFile
            }
        }

        project.task('assemblePlugin', type: Zip) {
            group = 'build'
            dependsOn 'collectPluginDependencies'
            destinationDirectory = project.layout.buildDirectory
            from project.layout.buildDirectory.dir('pluginLib')
            doFirst {
                archiveFileName = "${project.name}-${project.version}.zip"
            }
        }


    }


    private static PluginManifest pluginManifest(ProjectHelper helper, JextenPluginExtension properties, String jarFile) {

        def applicationArtifact = helper.applicationArtifact
        if (applicationArtifact == null) {
            throw new GradleException("There is no dependency of type 'jextenPluginApplication'")
        }
        def parentArtifact = helper.parentArtifact ?: applicationArtifact


        return new PluginManifest(
            PluginID.of("$helper.project.group:$helper.project.name"),
            Version.of("$helper.project.version"),
            helper.projectModule,
            jarFile,
            ArtifactID.of(applicationArtifact),
            helper.moduleName(applicationArtifact),
            ArtifactID.of(parentArtifact),
            helper.moduleName(parentArtifact),
            properties.name.getOrNull(),
            properties.URL.getOrNull(),
            properties.description.getOrNull(),
            properties.license.getOrNull(),
            properties.vendorName.getOrNull(),
            properties.vendorURL.getOrNull(),
            helper.filteredDependencies().collect { ArtifactID.of(it) },
            helper.metaInfExtensionFileContent()
                .lines()
                .map(line -> line.split("="))
                .collect(Collectors.toMap(it -> it[0], it -> List.of(it[1].split(","))))
        )

    }




}
