// Copyright  (c) 2022 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.test;

import java.util.Map;

import jexten.plugin.tree.*;
import org.junit.jupiter.api.Test;

public class TestModuleLayerTree {


	@Test
	void displayTree() {
		var tree = new ModuleLayerTree(Map.of());
		var visitor = new AnsiModuleTreeDescriptionVisitor();
		System.out.println(visitor.toString(tree));
	}

}
