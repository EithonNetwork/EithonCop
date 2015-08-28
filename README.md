# EithonCop

A Cop plugin for Minecraft.

## Release history

### 1.1.3 (2015-08-28)

* BUG: Could not remove blacklisted words.

### 1.1.2 (2015-08-24)

* BUG: Could not give a multi word explanation to the tempmute command.

### 1.1.1 (2015-08-23)

* BUG: A new blacklisted words that was similar to existing word could not be added.

### 1.1 (2015-08-19)

* CHANGE: Now ignores the word "I" when considering upper and lower case words.
* CHANGE: Now has configurable documentation for the unmute command.
* CHANGE: Now accepts new blacklisted words that are similar to existing words.
* BUG: Could get problem when muting players because of a formatting error.
* MISC: Added debug printouts to find out how the event sequence for chat events looks like

### 1.0.2 (2015-08-16)

* CHANGE: Now has configurable documentation for the tempmute command.

### 1.0.1 (2015-08-16)

* BUG: Censor method could not handle null messages.

### 1.0 (2015-08-11)

* NEW: No censoring of private (one-to-one) chats.
* NEW: Added an API for other plugins to use copbot.

### 0.12 (2015-08-10)

* CHANGE: All time span configuration values are now in the general TimeSpan format instead of hard coded to seconds or minutes or hours.
* BUG: The blacklist command without synonyms resulted in a null pointer exception.

### 0.11 (2015-08-06)

* NEW: unmute command
* CHANGE: tempmute and unmute commands are now issued from the sender, not from console.

### 0.10 (2015-08-05)

* NEW: tempmute command. Added configurations DefaultTempMuteInSeconds, DefaultTempMuteReason and MutedCommands.
* CHANGE: Added configuration MaxNumberOfRepeatedLines for spam.

### 0.9 (2015-08-03)

* NEW: Added configuration MaxNumberOfUpperCaseLettersInLine to limit the allowed number of upper case letters when chatting
* NEW: Added configuration MaxNumberOfUpperCaseWordsInLine to limit the allowed number of upper case words when chatting
* NEW: Checks for duplicate chat messages (configuration LineIsProbablyDuplicate and SecondsToRememberLines).

### 0.8 (2015-08-03)

* NEW: Added "add" and "remove" for the commands "blacklist" and "whitelist"

### 0.7 (2015-07-24)

* NEW: ProfanityBuildingBlocks is a list of words that, if part of a word, the word is considered blacklisted.

### 0.6.1 (2015-07-24)

* BUG: Ignored the level. Similar was always replaced.

### 0.6 (2015-07-24)

* NEW: Logs offender messages in offender.log

### 0.5 (2015-07-23)

* NEW: Completely rewritten profanity finder.
* NEW: Can detect the profanity in "i f u c k" and "f u c k u"

### 0.4 (2015-07-23)

* CHANGE: Inform players with a certain permission when a player has used a blacklisted word.
* CHANGE: Inform players with a certain permission when a player has used a similar word.

### 0.3 (2015-07-22)

* CHANGE: Added individual synonyms for profanities.
* CHANGE: Whitelist and similar are now listed alphabetically.

### 0.2 (2015-07-22)

* NEW: Now recognizes plural s (both "dicks" and "bitches" are recognized as dick and bitch respectively).
* NEW: Now has an isLiteral property for profanities.
* BUG: Save blacklist duplicated values.

### 0.1 (2015-07-21)

* NEW: First Eithon release
