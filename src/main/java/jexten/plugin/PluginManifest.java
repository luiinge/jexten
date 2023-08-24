// Copyright  (c) 2021 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.plugin;


import java.util.*;
import java.util.stream.*;
import jexten.Version;


public record PluginManifest(
    PluginID id,
    Version version,
    String module,
    String pluginJarFile,
    ArtifactID applicationArtifact,
    String applicationModule,
    ArtifactID parentArtifact,
    String parentModule,
    String name,
    String url,
    String description,
    String license,
    String vendorName,
    String vendorURL,
    List<ArtifactID> dependencies,
    Map<String,List<String>> extensions
) {


    public static final String PLUGIN_ID = "Plugin-ID";
    public static final String PLUGIN_VERSION = "Plugin-Version";
    public static final String PLUGIN_MODULE = "Plugin-Module";
    public static final String PLUGIN_JAR_FILE = "Plugin-Jar-File";
    public static final String PLUGIN_APPLICATION_ARTIFACT = "Plugin-Application-Artifact";
    public static final String PLUGIN_APPLICATION_MODULE = "Plugin-Application-Module";
    public static final String PLUGIN_PARENT_ARTIFACT = "Plugin-Parent-Artifact";
    public static final String PLUGIN_PARENT_MODULE = "Plugin-Parent-Module";
    public static final String PLUGIN_NAME = "Plugin-Name";
    public static final String PLUGIN_URL = "Plugin-URL";
    public static final String PLUGIN_DESCRIPTION = "Plugin-Description";
    public static final String PLUGIN_LICENSE = "Plugin-License";
    public static final String PLUGIN_VENDOR_NAME = "Plugin-Vendor-Name";
    public static final String PLUGIN_VENDOR_URL = "Plugin-Vendor-URL";
    public static final String PLUGIN_DEPENDENCIES = "Plugin-Dependencies";
    public static final String PLUGIN_EXTENSIONS = "Plugin-Extensions";
    public static final String DELIMITER = ";";



    /**
     * Create a new plugin descriptor from the given properties
     * @throws PluginException when any required property is missing
     */
    public static PluginManifest of(Properties properties) {

        List<ArtifactID> manifestDependencies;
        if (properties.getProperty(PLUGIN_DEPENDENCIES) == null) {
            manifestDependencies = List.of();
        } else {
            manifestDependencies = Stream.of(
                properties.getProperty(PLUGIN_DEPENDENCIES).split(DELIMITER)
            ).map(ArtifactID::of).toList();
        }

        Map<String,List<String>> manifestExtensions;
        if (properties.getProperty(PLUGIN_EXTENSIONS) == null) {
            manifestExtensions = Map.of();
        } else {
            manifestExtensions = Stream.of(properties.getProperty(PLUGIN_EXTENSIONS)
                .split(DELIMITER))
                .collect(Collectors.toMap(
                    line -> line.split("=")[0],
                    line -> List.of(line.split("=")[1].split(","))
                ));
        }

        return new PluginManifest(
            PluginID.of(properties.getProperty(PLUGIN_ID)),
            Version.of(properties.getProperty(PLUGIN_VERSION)),
            properties.getProperty(PLUGIN_MODULE),
            properties.getProperty(PLUGIN_JAR_FILE),
            ArtifactID.of(properties.getProperty(PLUGIN_APPLICATION_ARTIFACT)),
            properties.getProperty(PLUGIN_APPLICATION_MODULE),
            ArtifactID.of(properties.getProperty(PLUGIN_PARENT_ARTIFACT)),
            properties.getProperty(PLUGIN_PARENT_MODULE),
            properties.getProperty(PLUGIN_NAME),
            properties.getProperty(PLUGIN_URL),
            properties.getProperty(PLUGIN_DESCRIPTION),
            properties.getProperty(PLUGIN_LICENSE),
            properties.getProperty(PLUGIN_VENDOR_NAME),
            properties.getProperty(PLUGIN_VENDOR_URL),
            manifestDependencies,
            manifestExtensions
        );
    }




    public Map<String,String> asMap() {
        Map<String,String> map = new LinkedHashMap<>();
        put(map, PLUGIN_ID, id);
        put(map, PLUGIN_VERSION, version);
        put(map, PLUGIN_MODULE, module);
        put(map, PLUGIN_JAR_FILE, pluginJarFile);
        put(map, PLUGIN_APPLICATION_ARTIFACT, applicationArtifact);
        put(map, PLUGIN_APPLICATION_MODULE, applicationModule);
        put(map, PLUGIN_PARENT_ARTIFACT, applicationArtifact);
        put(map, PLUGIN_PARENT_MODULE, applicationModule);
        put(map, PLUGIN_NAME, name);
        put(map, PLUGIN_URL, url);
        put(map, PLUGIN_DESCRIPTION, description);
        put(map, PLUGIN_LICENSE, license);
        put(map, PLUGIN_VENDOR_NAME, vendorName);
        put(map, PLUGIN_VENDOR_URL, vendorURL);
        if (!dependencies.isEmpty()) {
            map.put(
                PLUGIN_DEPENDENCIES,
                dependencies.stream().map(ArtifactID::toString).collect(Collectors.joining(DELIMITER))
            );
        }
        if (!extensions.isEmpty()) {
            map.put(
                PLUGIN_EXTENSIONS,
                extensions.entrySet().stream()
                    .map(e -> e.getKey()+"="+String.join(",",e.getValue()))
                    .collect(Collectors.joining(DELIMITER))
            );
        }
        map.values().removeAll(Collections.singleton(null));
        return map;
    }


    private void put(Map<String,String> map, String key, Object value) {
        if (value == null || value.toString().isBlank()) {
            return;
        }
        map.put(key,value.toString());
    }


    public PluginManifest {
        requireNonNull(id, "id");
        requireNonNull(version, "version");
        requireNonNull(module, "module");
        requireNonNull(applicationModule, "applicationModule");
        requireNonNull(parentModule, "parentModule");
     }


     public ArtifactID artifactID() {
        return new ArtifactID(id.group(), id.name(), version);
     }


    private static <T> void requireNonNull(T value, String name) {
        if (value == null) {
            throw new PluginException(
                "Parameter {} is missing in {}",
                name,
                PluginManifest.class.getSimpleName()
            );
        }
    }




}
