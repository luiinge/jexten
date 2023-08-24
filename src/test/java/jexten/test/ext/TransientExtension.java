// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.test.ext;

import jexten.*;

@Extension(scope = Scope.TRANSIENT)
public class TransientExtension implements SimpleExtensionPoint {

	@Override
	public String provideStuff() {
		return "Stuff from TransientExtension";
	}

}
