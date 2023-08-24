// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten;

import java.lang.annotation.*;

/**
 * Methods marked with this annotation will be executed
 * when an extension instance is created, after the
 * injected fields have been resolved.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {

}
