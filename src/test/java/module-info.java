import jexten.test.ext.*;
module jexten.test {

    requires jexten;
    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.engine;
    requires org.assertj.core;
    requires org.slf4j;
    requires org.slf4j.simple;

    exports jexten.test to jexten, org.junit.platform.commons;
    exports jexten.test.ext to jexten;
    opens jexten.test.ext to jexten;
    opens jexten.test to org.junit.platform.commons;

    provides SimpleExtensionPoint with
        SimpleExtension,
        AnotherSimpleExtension,
        SingletonExtension,
        ExternallyLoadedExtension,
        InjectedFieldExtension,
        TransientExtension;

    provides VersionedExtensionPoint with
        VersionedExtension_1,
        VersionedExtension_2_1,
        VersionedExtension_3;


    provides PriorityExtensionPoint with
        LowerPriorityExtension,
        LowestPriorityExtension,
        NormalPriorityExtension,
        HighestPriorityExtension,
        HigherPriorityExtension;

    provides InjectableExtensionPoint with InjectedExtension;

    provides LoopedExtensionPoint with InjectedLoopExtension;



}