package jexten.plugin.tree;

import jexten.plugin.Plugin;

public interface ModuleLayerVisitor {

	void enterLayer(ModuleLayer layer, Plugin plugin, int depth);

	void exitLayer(ModuleLayer layer, Plugin plugin, int depth);

	void visitModule(ModuleLayer layer, Plugin plugin, int depth, Module module);


}
