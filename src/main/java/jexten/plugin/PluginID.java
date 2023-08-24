// Copyright  (c) 2021 -  Luis Iñesta Gelabert  <luiinge@gmail.com>

package jexten.plugin;

import java.util.StringTokenizer;
import jexten.Version;


public record PluginID(String group, String name) {

	public static PluginID of(String coordinates) {
		if (coordinates == null || coordinates.isBlank()) {
			return null;
		}
		StringTokenizer tokenizer = new StringTokenizer(coordinates,":");
		return new PluginID(tokenizer.nextToken(),tokenizer.nextToken());
	}


	@Override
	public String toString() {
		return group+":"+name;
	}

	public ArtifactID version(Version version) {
		return new ArtifactID(group,name,version);
	}

}
