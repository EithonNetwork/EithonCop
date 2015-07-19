package net.eithon.plugin.cop.logic;

import java.util.HashMap;

class Blacklist {
	private HashMap<String, ForbiddenWord> _hashMap;
	private static Metaphone3 metaphone3;

	static {
		metaphone3 = new Metaphone3();
		metaphone3.SetEncodeVowels(true);
		metaphone3.SetEncodeExact(true);
	}

	public Blacklist()
	{
		this._hashMap = new HashMap<String, ForbiddenWord>();
		ForbiddenWord word = new ForbiddenWord("hell");
		word.addSynonym("Tulsa");
		word.addSynonym("a warm place");
		add(word);
		word = new ForbiddenWord("shit");
		word.addSynonym("poop1");
		word.addSynonym("poop2");
		word.addSynonym("poop3");
		word.addSynonym("poop4");
		add(word);
		word = new ForbiddenWord("dildo");
		word.addSynonym("banana1");
		word.addSynonym("banana2");
		word.addSynonym("banana3");
		word.addSynonym("banana4");
		word.addSynonym("banana5");
		add(word);
		word = new ForbiddenWord("lars");
		add(word);
	}

	private void add(ForbiddenWord word) {
		this._hashMap.put(word.getPrimary(), word);
		if (word.hasSecondary()) this._hashMap.put(word.getSecondary(), word);
	}

	public boolean isBlacklisted(String word) { return getForbiddenWord(word) != null; }

	public String replaceIfBlacklisted(String word) {
		ForbiddenWord forbiddenWord = getForbiddenWord(word);
		if (forbiddenWord == null) return null;
		return forbiddenWord.getSynonym();
	}

	private ForbiddenWord getForbiddenWord(String word) {
		synchronized (metaphone3) {
			metaphone3.SetWord(word.toLowerCase());
			metaphone3.Encode();
			String encoding = metaphone3.GetMetaph();
			ForbiddenWord forbiddenWord = this._hashMap.get(encoding);
			if (forbiddenWord != null) return forbiddenWord;
			encoding = metaphone3.GetAlternateMetaph();
			if (encoding.length() == 0) return null;
			return this._hashMap.get(encoding);
		}
	}
}
