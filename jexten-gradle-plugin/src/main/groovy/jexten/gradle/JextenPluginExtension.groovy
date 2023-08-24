package jexten.gradle


import org.gradle.api.provider.Property

interface JextenPluginExtension {
    Property<String> getName()
    Property<String> getDescription()
    Property<String> getURL()
    Property<String> getVendorName()
    Property<String> getVendorURL()
    Property<String> getLicense()


}
