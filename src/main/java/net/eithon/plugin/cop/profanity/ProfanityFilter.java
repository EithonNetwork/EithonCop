package net.eithon.plugin.cop.profanity;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.eithon.plugin.cop.Config;

import org.bukkit.entity.Player;

class ProfanityFilter {
	private String _inMessage;
	private String _transformedInMessage;
	private StringTokenizer _tokenizer;
	private int _position = 0;
	private ArrayList<Token> _queue;
	private Player _player;
	private StringBuilder _outMessage;
	private ProfanityFilterController _controller;

	ProfanityFilter(ProfanityFilterController controller, Player player, String message) {
		this._controller = controller;
		this._player = player;
		this._inMessage = message;
		this._transformedInMessage = Profanity
				.normalize(this._inMessage)
				.replaceAll("[^a-z]", " ");
		this._tokenizer = new StringTokenizer(this._transformedInMessage, " ", true);
		this._queue = new ArrayList<Token>();
		this._outMessage = new StringBuilder();
	}

	boolean hasMoreTokens() { return this._tokenizer.hasMoreTokens(); }

	String getFilteredMessage() {
		while (hasMoreTokens()) {
			Token token = getNextToken();
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
		StringBuilder tail = new StringBuilder("");
		StringBuilder front = new StringBuilder("");
		String out = null;
		for (int i = 0; i < length; i++) {
			int beginning = i;
			int end = length-i-1;
			Token tokensWithTail = this._queue.get(end);
			Token tokensWithFront = this._queue.get(beginning);
			if (!tokensWithTail.getTransformed().equalsIgnoreCase(" ")) {
				out = replaceProfanityInSubpart(0, end);
				if (out != null) {
					this._outMessage.append(out);
					this._outMessage.append(tail);
					verbose("ProfanityFilter.handleQueue", "out=\"%s\", tail=\"%s\", outmessage=\"%s\"",
							out, tail, this._outMessage);
					break;
				}
			}
			tail.insert(0, tokensWithTail.getIn());
			front.append(tokensWithFront.getIn());
			if (!tokensWithFront.getTransformed().equalsIgnoreCase(" ")) {
				out = replaceProfanityInSubpart(beginning+1, length-1);
				if (out != null) {
					this._outMessage.append(front);
					this._outMessage.append(out);
					verbose("ProfanityFilter.handleQueue", "front=\"%s\", out=\"%s\", outmessage=\"%s\"",
							front, out, this._outMessage);
					break;
				}
			}
		}
		if (out == null) {
			this._outMessage.append(tail);
			verbose("ProfanityFilter.handleQueue", "front=\"%s\", tail=\"%s\", outmessage=\"%s\"",
					front, tail, this._outMessage);
		}
		this._queue = new ArrayList<Token>();
	}

	private String replaceProfanityInSubpart(int start, int end) {
		if (end-start+1 < Config.V.profanityWordMinimumLength) return null;
		String in = "";
		String transformed = "";
		Token token;
		for (int j = start; j <= end; j++) {
			token = this._queue.get(j);
			in += token.getIn();
			String t = token.getTransformed();
			if (!t.equalsIgnoreCase(" ")) transformed += t;
		}
		token = new Token(in, transformed);
		return replaceProfanity(token);
	}

	private void handleToken(Token token) {
		String out = replaceProfanity(token);
		if (out == null) out = token.getIn();
		this._outMessage.append(out);
		verbose("ProfanityFilter.handleToken", "out = \"%s\", outmessage=\"%s\"", out, this._outMessage);
	}

	private String replaceProfanity(Token token) {
		String outWord = replaceWithSynonym(token.getTransformed(), token.getIn(), true);
		if (outWord == null) return null;
		String result = casifyAsReferenceWord(outWord, token.getIn());
		if (Leet.isLeet(token.getIn())) return Leet.encode(result);
		return result;
	}

	private String replaceWithSynonym(String transformedWord, String originalWord, boolean checkPlural) {
		String outWord = replaceWithSynonym(transformedWord, originalWord);
		if ((outWord != null) || !checkPlural) return outWord;
		String withoutPlural = withoutPlural(transformedWord);
		if (transformedWord.equalsIgnoreCase(withoutPlural)) return null;
		return replaceWithSynonym(withoutPlural, originalWord);
	}

	private String replaceWithSynonym(String transformedWord, String originalWord) {
		if (transformedWord.length() < Config.V.profanityWordMinimumLength) {
			return null;
		}
		if (this._controller.isWhitelisted(transformedWord)) {
			return null;
		}
		String result = this._controller.replaceIfBlacklisted(this._player, transformedWord, originalWord);
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
		this._controller.verboseLog("ProfanityFilter", method, format, args);
	}
}
