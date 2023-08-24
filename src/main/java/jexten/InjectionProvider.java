// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten;

import java.util.stream.Stream;

public interface InjectionProvider {

    static InjectionProvider empty () {
        return (type,name) -> Stream.empty();
    }

    Stream<Object> provideInstancesFor(Class<?> requestedType, String name);

}
