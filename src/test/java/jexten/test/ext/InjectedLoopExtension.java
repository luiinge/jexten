// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.test.ext;

import jexten.*;

@Extension(scope = Scope.GLOBAL)
public class InjectedLoopExtension implements LoopedExtensionPoint {

	@Inject
	public LoopedExtensionPoint loop;

}
