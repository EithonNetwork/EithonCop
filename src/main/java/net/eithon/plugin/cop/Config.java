package net.eithon.plugin.cop;

import java.util.List;

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
		public static String[] categoryUnknown;
		public static String[] categoryBodyContent;
		public static String[] categoryBodyPart;
		public static String[] categoryLocation;
		public static String[] categoryOffensive;
		public static String[] categoryProfession;
		public static String[] categoryRacist;
		public static String[] categorySexualNoun;
		public static String[] categorySexualVerb;
		public static String[] categoryDerogative;
		static void load(Configuration config) {
			String[] sample = {""};
			categoryUnknown = config.getStringList("CategoryUnknown").toArray(sample);
			categoryBodyContent = config.getStringList("CategoryBodyContent").toArray(sample);
			categoryBodyPart = config.getStringList("CategoryBodyPart").toArray(sample);
			categoryLocation = config.getStringList("CategoryLocation").toArray(sample);
			categoryOffensive = config.getStringList("CategoryOffensive").toArray(sample);
			categoryProfession = config.getStringList("CategoryProfession").toArray(sample);
			categoryRacist = config.getStringList("CategoryRacist").toArray(sample);
			categorySexualNoun = config.getStringList("CategorySexualNoun").toArray(sample);
			categorySexualVerb = config.getStringList("CategorySexualVerb").toArray(sample);
			categoryDerogative = config.getStringList("CategoryDerogative").toArray(sample);
		}

	}
	public static class C {
		static void load(Configuration config) {
		}

	}
	public static class M {
		public static ConfigurableMessage probablyDuplicateProfanity;
		public static ConfigurableMessage duplicateProfanity;
		public static ConfigurableMessage profanityAdded;

		static void load(Configuration config) {
			duplicateProfanity = config.getConfigurableMessage("DuplicateProfanity", 1,
					"You specified the word \"%s\", but that word has already been blacklisted.");
			probablyDuplicateProfanity = config.getConfigurableMessage("ProbablyDuplicateProfanity", 2,
					"You specified the word \"%s\", but that word collides with existing profanity \"%s\".");
			profanityAdded = config.getConfigurableMessage("ProfanityAdded", 1,
					"Profanity \"%s\" has been added to the blacklist.");
		}		
	}

}
