// Copyright  (c) 2021 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.plugin;

import java.util.*;
import jexten.Version;

/**
 * This class represents the identifier or an artifact (either a plugin or a plugin dependency).
 * <p>
 * It is formed by a group, a name, and optionally a version.
 */
public record ArtifactID (String group, String name, Version version) {

	public static ArtifactID of (String coordinates) {
		if (coordinates == null || coordinates.isBlank()) {
			return null;
		}
		StringTokenizer tokenizer = new StringTokenizer(coordinates,":");
		return new ArtifactID(
			tokenizer.nextToken(),
			tokenizer.nextToken(),
			Version.of(tokenizer.nextToken())
		);
	}

	@Override
	public String toString() {
		return group+":"+name+":"+version;
	}

	public PluginID pluginID() {
		return new PluginID(group,name);
	}

}
