package net.eithon.plugin.cop.logic;

import java.util.ArrayList;

import net.eithon.library.json.IJson;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Profanity implements IJson<Profanity> {
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

	public Profanity(String profanity) {
		this._word = profanity.toLowerCase();
		this._synonyms = new ArrayList<String>();
		prepare();
	}

	private Profanity() {
	}

	private void prepare() {
		synchronized (metaphone3) {
			metaphone3.SetWord(this._word);
			metaphone3.Encode();
			this._primaryEncoded = metaphone3.GetMetaph();
			String secondaryEncoded = metaphone3.GetAlternateMetaph();
			if (secondaryEncoded.length() > 0) this._secondaryEncoded = secondaryEncoded;
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

	@Override
	public Profanity factory() {
		return new Profanity();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("word", this._word);
		JSONArray jsonArray = new JSONArray();
		for (String synonym: this._synonyms) {
			jsonArray.add(synonym);
		}
		json.put("synonyms", jsonArray);
		return json;
	}

	@Override
	public Profanity fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._word = (String) jsonObject.get("word");
		JSONArray jsonArray = (JSONArray) jsonObject.get("synonyms");
		this._synonyms = new ArrayList<String>();
		if ((jsonArray != null) && (jsonArray.size() > 0)) {
			for (Object synonym : jsonArray) {
				this._synonyms.add((String)synonym);
			}
		}
		this.prepare();
		return this;
	}

	public static Profanity getFromJson(Object json) {
		return new Profanity().fromJson(json);
	}
}
