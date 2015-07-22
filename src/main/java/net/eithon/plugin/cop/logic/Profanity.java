package net.eithon.plugin.cop.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import net.eithon.library.json.IJson;
import net.eithon.plugin.cop.Config;

import org.json.simple.JSONObject;

class Profanity implements IJson<Profanity> {
	static final int PROFANITY_LEVEL_NONE = 0;
	static final int PROFANITY_LEVEL_LITERAL = 1;
	static final int PROFANITY_LEVEL_SIMILAR = 2;
	static final int PROFANITY_LEVEL_MAX = 2;
	static Metaphone3 metaphone3;
	
	private static EnumMap<ProfanityType, Integer> profanityTypeToInteger = new EnumMap<Profanity.ProfanityType, Integer>(ProfanityType.class);
	private static HashMap<Integer, ProfanityType> integerToProfanityType = new HashMap<Integer, Profanity.ProfanityType>();
	private static EnumMap<ProfanityType, String[]> synonyms = new EnumMap<Profanity.ProfanityType, String[]>(ProfanityType.class);

	private String _word;
	private String _primaryEncoded;
	private String _secondaryEncoded;
	private ProfanityType _type;
	private boolean _isLiteral;
	
	static {
		metaphone3 = new Metaphone3();
		metaphone3.SetEncodeVowels(true);
		metaphone3.SetEncodeExact(true);
		addProfanityType(ProfanityType.UNKNOWN, 0);
		addProfanityType(ProfanityType.BODY_CONTENT, 1);
		addProfanityType(ProfanityType.BODY_PART, 2);
		addProfanityType(ProfanityType.LOCATION, 3);
		addProfanityType(ProfanityType.OFFENSIVE, 4);
		addProfanityType(ProfanityType.PROFESSION, 5);
		addProfanityType(ProfanityType.RACIST, 6);
		addProfanityType(ProfanityType.SEXUAL_NOUN, 7);
		addProfanityType(ProfanityType.SEXUAL_VERB, 8);
		addProfanityType(ProfanityType.DEROGATIVE, 9);
		synonyms.put(ProfanityType.UNKNOWN, Config.V.categoryUnknown);
		synonyms.put(ProfanityType.BODY_CONTENT, Config.V.categoryBodyContent);
		synonyms.put(ProfanityType.BODY_PART, Config.V.categoryBodyPart);
		synonyms.put(ProfanityType.LOCATION, Config.V.categoryLocation);
		synonyms.put(ProfanityType.OFFENSIVE, Config.V.categoryOffensive);
		synonyms.put(ProfanityType.PROFESSION, Config.V.categoryProfession);
		synonyms.put(ProfanityType.RACIST, Config.V.categoryRacist);
		synonyms.put(ProfanityType.SEXUAL_NOUN, Config.V.categorySexualNoun);
		synonyms.put(ProfanityType.SEXUAL_VERB, Config.V.categorySexualVerb);
		synonyms.put(ProfanityType.DEROGATIVE, Config.V.categoryDerogative);
	}

	public Profanity(String word) {
		this._word = normalize(word);
		this._type = ProfanityType.UNKNOWN;
		this._isLiteral = true;
		prepare();
	}

	Profanity() {
	}

	public enum ProfanityType {
	    UNKNOWN, BODY_CONTENT, BODY_PART, LOCATION, OFFENSIVE, PROFESSION, RACIST, SEXUAL_NOUN, SEXUAL_VERB, DEROGATIVE
	}
	
	public static String normalize(String word) { return Leet.decode(word.toLowerCase()); }

	private static void addProfanityType(ProfanityType type, Integer i) {
		profanityTypeToInteger.put(type, i);
		integerToProfanityType.put(i, type);
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
	public boolean isLiteral() { return this._isLiteral; }
	public ProfanityType getProfanityType() { return this._type; }
	public void setProfanityType(ProfanityType type) { this._type = type; }
	public boolean isSameWord(String word) {return this._word.equalsIgnoreCase(normalize(word)); }
	public int getProfanityLevel(String word) { 
		if (isSameWord(word)) return PROFANITY_LEVEL_LITERAL;
		return PROFANITY_LEVEL_SIMILAR;
	}
	
	public String getSynonym() {
		String[] array = getSynonyms();
		int index = (int) (Math.random()*array.length);
		return array[index];
	}
	
	public String[] getSynonyms() {
		String[] array = synonyms.get(this._type);
		if ((array == null) || (array.length == 0)) return new String[] {"****"};
		return array;
	}

	@Override
	public Profanity factory() {
		return new Profanity();
	}

	static List<Profanity> sortByWord(Collection<Profanity> collection) {
		return sortByWord(collection, true);
	}

	static List<Profanity> sortByWord(Collection<Profanity> collection, boolean ascending) {
		int factor = ascending ? 1 : -1;
		ArrayList<Profanity> array = new ArrayList<Profanity>(collection);
		array.sort(
				new Comparator<Profanity>(){
					public int compare(Profanity f1, Profanity f2)
					{
						return factor*f1.getWord().compareTo(f2.getWord());
					} });	
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("word", this._word);
		json.put("type", profanityTypeToInteger.get(this._type));
		json.put("isLiteral", this._isLiteral ? 1 : 0);
		return json;
	}

	@Override
	public Profanity fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._word = (String) jsonObject.get("word");
		Long typeAsInteger = (Long) jsonObject.get("type");
		Long isLiteralAsLong = (Long) jsonObject.get("isLiteral");
		this._isLiteral = ((isLiteralAsLong == null) || (isLiteralAsLong.longValue() == 0)) ? false : true;
		if (typeAsInteger == null) this._type = ProfanityType.UNKNOWN;
		else this._type = integerToProfanityType.get(typeAsInteger.intValue());
		this.prepare();
		return this;
	}

	public static Profanity getFromJson(Object json) {
		return new Profanity().fromJson(json);
	}
	
	@Override 
	public String toString() {
		String result = String.format("%s (%s, %d)", getWord(), this._isLiteral ? "literal" : "not literal", profanityTypeToInteger.get(this._type));
		if (hasSecondary()) result += String.format(" [%s, %s]", getPrimary(), getSecondary());
		else result += String.format(" [%s]", getPrimary());
		return result;
	}
}
