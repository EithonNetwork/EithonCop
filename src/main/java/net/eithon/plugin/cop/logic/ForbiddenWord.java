package net.eithon.plugin.cop.logic;

import java.util.ArrayList;

class ForbiddenWord {
	private String _word;
	private String _primaryEncoded;
	private String _secondaryEncoded;
	private ArrayList<String> _synonyms;
	static Metaphone3 metaphone3;
	
	static {
		metaphone3 = new Metaphone3();
		metaphone3.SetEncodeVowels(true);
		metaphone3.SetEncodeExact(true);
	}
	
	public ForbiddenWord(String word) {
		synchronized (metaphone3) {
		this._word = word.toLowerCase();
		metaphone3.SetWord(this._word);
		metaphone3.Encode();
		this._primaryEncoded = metaphone3.GetMetaph();
		String secondaryEncoded = metaphone3.GetAlternateMetaph();
		if (secondaryEncoded.length() > 0) this._secondaryEncoded = secondaryEncoded;
		this._synonyms = new ArrayList<String>();
		}
	}
	
	public String getWord() { return this._word; }
	public String getPrimary() {return this._primaryEncoded; }
	public String getSecondary() { return this._secondaryEncoded; }
	public boolean hasSecondary() { return this._secondaryEncoded != null; }

	public String getSynonym() {
		if (this._synonyms.size() == 0) return "****";
		int index = (int) (Math.random()*this._synonyms.size());
		return this._synonyms.get(index);
	}

	public void addSynonym(String synonym) {
		this._synonyms.add(synonym);
	}
}
