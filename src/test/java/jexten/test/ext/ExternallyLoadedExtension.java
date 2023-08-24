// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.test.ext;

import jexten.Extension;
import jexten.test.TestingExtensionLoader;

@Extension(loadedWith = TestingExtensionLoader.class)
public class ExternallyLoadedExtension implements SimpleExtensionPoint {

	@Override
	public String provideStuff() {
		return "Stuff from ExternallyLoadedExtension";
	}

}
