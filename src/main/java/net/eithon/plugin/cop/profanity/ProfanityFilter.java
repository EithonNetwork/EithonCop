package net.eithon.plugin.cop.profanity;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.cop.Config;

import org.bukkit.command.CommandSender;

class ProfanityFilter {
	private String _inMessage;
	private String _transformedInMessage;
	private StringTokenizer _tokenizer;
	private int _position = 0;
	private ArrayList<Token> _queue;
	private CommandSender _sender;
	private StringBuilder _outMessage;
	private ProfanityFilterController _controller;

	ProfanityFilter(ProfanityFilterController controller, CommandSender sender, String message) {
		this._controller = controller;
		this._sender = sender;
		this._inMessage = message;
		this._transformedInMessage = Profanity
				.normalize(this._inMessage)
				.replaceAll("\\W", " ");
		this._tokenizer = new StringTokenizer(this._transformedInMessage, " ", true);
		this._queue = new ArrayList<Token>();
		this._outMessage = new StringBuilder();
	}

	boolean hasMoreTokens() { return this._tokenizer.hasMoreTokens(); }

	String getFilteredMessage() {
		while (hasMoreTokens()) {
			Token token = getNextToken();
			verbose("ProfanityFilter.getFilteredMessage", "token = \"%s\"", token);
			if (token.equalsIgnoreCase(" ")) {
				if (queueIsEmpty()) handleToken(token);
				else addToQueue(token);
			} else if (token.length() < Config.V.profanityWordMinimumLength) {
				addToQueue(token);
			} else {
				handleQueue();
				handleToken(token);
			}
		}
		handleQueue();
		return this._outMessage.toString();
	}

	private boolean queueIsEmpty() {
		return this._queue.size() == 0;
	}

	private Token getNextToken() {
		String transformed = this._tokenizer.nextToken();
		int tokenLength = transformed.length();
		int beginIndex = this._position;
		this._position += tokenLength;
		int endIndex = this._position;
		String in = this._inMessage.substring(beginIndex, endIndex);
		return new Token(in, transformed);
	}

	private void addToQueue(Token token) {
		this._queue.add(token);
	}

	private void handleQueue() {
		int length = this._queue.size();
		String tail = "";
		for (int i = length; i > 0; i--) {
			String in = "";
			String transformed = "";
			Token token = this._queue.get(i-1);
			if (token.getTransformed().equalsIgnoreCase(" ")) {
				tail = token.getIn() + tail;
				continue;
			}
			boolean success = getTokenFromBeginning(tail, i, in, transformed);
			if (success) break;
			tail = this._queue.get(i-1).getIn() + tail;
		}
		this._queue = new ArrayList<Token>();
		this._outMessage.append(tail);
		verbose("ProfanityFilter.handleQueue", "tail=\"%s\", outmessage=\"%s\"", tail, this._outMessage);
	}

	private boolean getTokenFromBeginning(String tail, int i, String in,
			String transformed) {
		Token token;
		for (int j = 0; j < i; j++) {
			token = this._queue.get(j);
			in += token.getIn();
			String t = token.getTransformed();
			if (!t.equalsIgnoreCase(" ")) transformed += t;
		}
		token = new Token(in, transformed);
		String result = replaceProfanity(token);
		if (result != null) {
			this._outMessage.append(result);
			verbose("ProfanityFilter.handleQueue", "token = \"%s\", result=\"%s\", tail=\"%s\", outmessage=\"%s\"",
					token, result, tail, this._outMessage);
			return true;
		}
		return false;
	}

	private void handleToken(Token token) {
		String out = replaceProfanity(token);
		if (out == null) out = token.getIn();
		this._outMessage.append(out);
		verbose("ProfanityFilter.handleToken", "out = \"%s\", outmessage=\"%s\"", out, this._outMessage);
	}

	private String replaceProfanity(Token token) {
		String outWord = replaceWithSynonym(token.getTransformed(), true);
		if (outWord == null) return null;
		String result = casifyAsReferenceWord(outWord, token.getIn());
		if (Leet.isLeet(token.getIn())) return Leet.encode(result);
		return result;
	}

	private String replaceWithSynonym(String transformedWord, boolean checkPlural) {
		String outWord = replaceWithSynonym(transformedWord);
		if ((outWord != null) || !checkPlural) return outWord;
		String withoutPlural = withoutPlural(transformedWord);
		if (transformedWord.equalsIgnoreCase(withoutPlural)) return null;
		return replaceWithSynonym(withoutPlural);
	}

	private String replaceWithSynonym(String transformedWord) {
		if (transformedWord.length() < Config.V.profanityWordMinimumLength) {
			return null;
		}
		if (this._controller.isWhitelisted(transformedWord)) {
			return null;
		}
		String result = this._controller.replaceIfBlacklisted(this._sender, transformedWord);
		verbose("Controller.replaceWithSynonym", "transformedWord=\"%s\", Leave = \"%s\"", transformedWord, result);
		return result;
	}

	private String withoutPlural(String transformedWord) {
		if (transformedWord.endsWith("es")) return transformedWord.substring(0, transformedWord.length()-2);
		if (transformedWord.endsWith("s")) return transformedWord.substring(0, transformedWord.length()-1);
		return transformedWord;
	}

	private String casifyAsReferenceWord(String outWord, String referenceWord) {
		char[] charArray = referenceWord.toCharArray();
		char c = charArray[0];
		boolean firstCharacterIsUpperCase = Character.isAlphabetic(c) && Character.isUpperCase(c);
		if (!firstCharacterIsUpperCase) return outWord.toLowerCase();
		for (int i = 1; i < charArray.length; i++) {
			c = charArray[i];
			if (Character.isAlphabetic(c)) {
				if (Character.isUpperCase(c)) return outWord.toUpperCase();
				else {
					StringBuilder result = new StringBuilder();
					result.append(outWord.substring(0, 1).toUpperCase());
					result.append(outWord.substring(1).toLowerCase());
					return result.toString();
				}
			}
		}
		return outWord.toLowerCase();
	}

	private void verbose(String method, String format, Object... args) {
		this._controller.verbose(method, format, args);
	}
}
