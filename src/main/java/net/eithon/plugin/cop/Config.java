package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config);
		C.load(config);
		M.load(config);

	}
	public static class V {
		static void load(Configuration config) {
		}

	}
	public static class C {
		static void load(Configuration config) {
		}

	}
	public static class M {
		public static ConfigurableMessage profanityAlreadySaved;
		public static ConfigurableMessage profanityAdded;

		static void load(Configuration config) {
			profanityAlreadySaved = config.getConfigurableMessage("ProfanityAlreadySaved", 2,
					"You specified the word \"%s\", but that seems to collide with existing profanity \"%s\".");
			profanityAdded = config.getConfigurableMessage("ProfanityAdded", 1,
					"Profanity \"%s\" has been added to the blacklist.");
		}		
	}

}
