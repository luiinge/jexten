// Copyright  (c) 2021 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jexten.internal.DefaultExtensionManager;


public interface ExtensionManager {


    static ExtensionManager create(ModuleLayerProvider layerProvider) {
        return new DefaultExtensionManager(layerProvider);
    }


    static ExtensionManager create() {
        return new DefaultExtensionManager(ModuleLayerProvider.boot());
    }

    ExtensionManager withInjectionProvider(InjectionProvider injectionProvider);

    <T> Optional<T> getExtension(Class<T> extensionPoint);

    <T> Optional<T> getExtension(Class<T> extensionPoint, Predicate<Class<?>> filter);

    <T> Optional<T> getExtensionByName(Class<T> extensionPoint, String name);

    <T> Optional<T> getExtensionByName(Class<T> extensionPoint, Predicate<String> name);

    <T> Stream<T> getExtensions(Class<T> extensionPoint);

    <T> Stream<T> getExtensions(Class<T> extensionPoint, Predicate<Class<?>> filter);

    <T> Stream<T> getExtensionsByName(Class<T> extensionPoint, Predicate<String> filter);

    void clear();

}
