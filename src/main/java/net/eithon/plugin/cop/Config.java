package net.eithon.plugin.cop;

import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableCommand;
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
		public static String[] profanityBuildingBlocks;
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
		public static int profanityLevel;
		public static boolean saveSimilar;
		public static boolean markReplacement;
		public static String markReplacementPrefix;
		public static String markReplacementPostfix;
		public static boolean markSimilar;
		public static String markSimilarPrefix;
		public static String markSimilarPostfix;
		public static int profanityWordMinimumLength = 3;
		public static int profanityRecentOffenderCooldownInSeconds;
		public static int profanityOffenderCooldownInSeconds;
		public static boolean logOffenderMessages;
		public static int maxNumberOfUpperCaseLettersInLine;
		public static int maxNumberOfUpperCaseWordsInLine;
		public static double lineIsProbablyDuplicate;
		public static long secondsToRememberLines;
		public static long defaultTempMuteInSeconds;
		public static String defaultTempMuteReason;
		public static List<String> mutedCommands;
		
		static void load(Configuration config) {
			profanityBuildingBlocks = config.getStringList("ProfanityBuildingBlocks").toArray(new String[0]);
			categoryUnknown = config.getStringList("CategoryUnknown").toArray(new String[0]);
			categoryBodyContent = config.getStringList("CategoryBodyContent").toArray(new String[0]);
			categoryBodyPart = config.getStringList("CategoryBodyPart").toArray(new String[0]);
			categoryLocation = config.getStringList("CategoryLocation").toArray(new String[0]);
			categoryOffensive = config.getStringList("CategoryOffensive").toArray(new String[0]);
			categoryProfession = config.getStringList("CategoryProfession").toArray(new String[0]);
			categoryRacist = config.getStringList("CategoryRacist").toArray(new String[0]);
			categorySexualNoun = config.getStringList("CategorySexualNoun").toArray(new String[0]);
			categorySexualVerb = config.getStringList("CategorySexualVerb").toArray(new String[0]);
			categoryDerogative = config.getStringList("CategoryDerogative").toArray(new String[0]);
			profanityLevel = config.getInt("ProfanityLevel", 0);
			logOffenderMessages = config.getInt("LogOffenderMessages", 0) != 0;
			profanityRecentOffenderCooldownInSeconds = config.getInt("ProfanityOffenderCooldownInSeconds", 20);
			profanityOffenderCooldownInSeconds = config.getInt("ProfanityOffenderCooldownInSeconds", 3600);
			saveSimilar = config.getInt("SaveSimilar", 0) != 0;
			markReplacement = config.getInt("MarkReplacement", 0) != 0;
			markReplacementPrefix = config.getString("MarkReplacementPrefix", "'");
			markReplacementPostfix = config.getString("MarkReplacementPostfix", "'");
			markSimilar = config.getInt("MarkSimilar", 0) != 0;
			markSimilarPrefix = config.getString("MarkSimilarPrefix", "<");
			markSimilarPostfix = config.getString("MarkSimilarPostfix", ">");
			maxNumberOfUpperCaseLettersInLine = config.getInt("MaxNumberOfUpperCaseLettersInLine", 15);
			maxNumberOfUpperCaseWordsInLine = config.getInt("MaxNumberOfUpperCaseWordsInLine", 3);
			lineIsProbablyDuplicate = config.getDouble("LineIsProbablyDuplicate", 0.9);
			secondsToRememberLines = config.getInt("SecondsToRememberLines", 30);
			defaultTempMuteInSeconds = config.getInt("mute.DefaultTempMuteInSeconds", 10);
			defaultTempMuteReason = config.getString("mute.DefaultTempMuteReason", "Unspecified");
			mutedCommands = config.getStringList("mute.MutedCommands");
		}

	}
	public static class C {
		public static ConfigurableCommand tempMutePlayer;

		static void load(Configuration config) {
			tempMutePlayer = config.getConfigurableCommand("commands.mute.TempMutePlayer", 3, 
					"tempmute %s %ds %s");
		}

	}
	public static class M {
		public static ConfigurableMessage probablyDuplicateProfanity;
		public static ConfigurableMessage profanityNotFound;
		public static ConfigurableMessage profanityNotFoundButSimilarFound;
		public static ConfigurableMessage duplicateProfanity;
		public static ConfigurableMessage profanityAdded;
		public static ConfigurableMessage profanityRemoved;
		public static ConfigurableMessage acceptedWordWasNotBlacklisted;
		public static ConfigurableMessage acceptedWordAdded;
		public static ConfigurableMessage acceptedWordNotFound;
		public static ConfigurableMessage acceptedWordRemoved;
		public static ConfigurableMessage acceptedWordWasBlacklisted;
		public static ConfigurableMessage duplicateAcceptedWord;
		public static ConfigurableMessage blackListWordMinimalLength;
		public static ConfigurableMessage whitelistWordMinimalLength;
		public static ConfigurableMessage notifyAboutProfanity;
		public static ConfigurableMessage notifyAboutComposed;
		public static ConfigurableMessage notifyAboutSimilar;
		public static ConfigurableMessage tempMutedPlayer;

		static void load(Configuration config) {
			profanityNotFound = config.getConfigurableMessage("messages.ProfanityNotFound", 1,
					"The word \"%s\" was not blacklisted.");
			profanityNotFoundButSimilarFound = config.getConfigurableMessage("messages.ProfanityNotFoundButSimilarFound", 2,
					"The word \"%s\" was not blacklisted. Did you mean \"%s\"?");
			duplicateProfanity = config.getConfigurableMessage("messages.DuplicateProfanity", 1,
					"The word \"%s\" has already been blacklisted.");
			probablyDuplicateProfanity = config.getConfigurableMessage("messages.ProbablyDuplicateProfanity", 2,
					"You specified the word \"%s\", but that word collides with existing blacklisted word \"%s\".");
			profanityAdded = config.getConfigurableMessage("messages.ProfanityAdded", 1,
					"The word \"%s\" has been added to the blacklist.");
			profanityRemoved = config.getConfigurableMessage("messages.ProfanityRemoved", 1,
					"The word \"%s\" has been removed from the blacklist.");
			acceptedWordWasNotBlacklisted = config.getConfigurableMessage("messages.AcceptedWordWasNotBlacklisted", 1,
					"The word \"%s\" is not blacklisted, so it will not be added as whitelisted.");
			acceptedWordNotFound = config.getConfigurableMessage("messages.AcceptedWordNotFound", 1,
					"The word \"%s\" was not whitelisted.");
			acceptedWordAdded = config.getConfigurableMessage("messages.AcceptedWordAdded", 2,
					"The word \"%s\" is now whitelisted, to prevent it from being mixed up with the blacklisted word \"%s\".");
			acceptedWordRemoved = config.getConfigurableMessage("messages.AcceptedWordRemoved", 1,
					"The word \"%s\" is no longer whitelisted.");
			acceptedWordWasBlacklisted = config.getConfigurableMessage("messages.AcceptedWordWasBlacklisted", 1,
					"You can't whitelist \"%s\" because it is blacklisted with that spelling.");
			duplicateAcceptedWord = config.getConfigurableMessage("messages.DuplicateAcceptedWord", 1,
					"The word \"%s\" has already been whitelisted.");
			blackListWordMinimalLength = config.getConfigurableMessage("messages.BlacklistWordMinimalLength", 1,
					"A word that should be blacklisted must have at least %d characters.");
			whitelistWordMinimalLength = config.getConfigurableMessage("messages.WhitelistWordMinimalLength", 1,
					"A word that should be whitelisted must have at least %d characters.");
			notifyAboutProfanity = config.getConfigurableMessage("messages.NotifyAboutProfanity", 3,
					"Player %s used the word \"%s\" (%s) which is blacklisted.");
			notifyAboutComposed = config.getConfigurableMessage("messages.NotifyAboutComposed", 4,
					"Player %s used the word \"%s\" (%s), that contains the blacklisted building block \"%s\".");
			notifyAboutSimilar = config.getConfigurableMessage("messages.NotifyAboutSimilar", 4,
					"Player %s used the word \"%s\" (%s), that is similar to the blacklisted word \"%s\".");
			tempMutedPlayer = config.getConfigurableMessage("messages.mute.TempMutedPlayer", 3,
					"Player %s has been muted %s with reason \"%s\".");
		}		
	}

}
