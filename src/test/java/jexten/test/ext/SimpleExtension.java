// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.test.ext;

import jexten.Extension;

@Extension
public class SimpleExtension implements SimpleExtensionPoint {

	@Override
	public String provideStuff() {
		return "Stuff from SimpleExtension";
	}

}
