package jexten.plugin.tree;


import jexten.plugin.Plugin;
import org.fusesource.jansi.*;

public class AnsiModuleTreeDescriptionVisitor implements ModuleLayerVisitor {

	private final StringBuilder description = new StringBuilder();


	public String toString(ModuleLayerTree tree) {
		tree.forEach(this);
		return description.toString();
	}


	protected void append(String text, Object...args) {
		description.append(AnsiRenderer.render(text.formatted(args))).append("\n");
	}


	@Override
	public void enterLayer(ModuleLayer layer, Plugin plugin, int depth) {
		if (depth == 0) {
			append("@|bold [ ApplicationLayer ]|@");
		} else {
			String margin = "  "+"    ".repeat(depth-1);
			append(
				"%s@|bold %s (|@@|green %s|@ version @|yellow %s|@@|bold )|@",
				margin,
				plugin.manifest().name(),
				plugin.id(),
				plugin.version()
			);
			append("%s  Extensions:", margin);
			plugin.manifest().extensions().forEach((extensionPoint,extensions)->{
				append("%s  - %s :", margin,extensionPoint);
				extensions.forEach(it -> append("%s     %s",margin,it));
			});
		}
	}


	@Override
	public void exitLayer(ModuleLayer layer, Plugin plugin, int depth) {

	}


	@Override
	public void visitModule(ModuleLayer layer, Plugin plugin, int depth, Module module) {

	}

}
