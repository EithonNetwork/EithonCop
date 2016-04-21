package net.eithon.plugin.cop.profanity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.eithon.plugin.cop.Config;
import net.eithon.plugin.cop.db.DbBlacklist;
import net.eithon.plugin.cop.db.DbSimilar;
import net.eithon.plugin.cop.db.DbWhitelist;

class Profanity {
	static final int PROFANITY_LEVEL_NONE = 0;
	static final int PROFANITY_LEVEL_LITERAL = 1;
	static final int PROFANITY_LEVEL_COMPOSED = 2;
	static final int PROFANITY_LEVEL_SIMILAR = 3;
	static final int PROFANITY_LEVEL_MAX = 3;
	static Metaphone3 metaphone3;

	private static Comparator<Profanity> profanityComparator;

	private String _word;
	private String _primaryEncoded;
	private String _secondaryEncoded;
	private boolean _isLiteral;
	private DbBlacklist _dbBlacklist;

	static void initialize() {
		metaphone3 = new Metaphone3();
		metaphone3.SetEncodeVowels(true);
		metaphone3.SetEncodeExact(true);
		profanityComparator = new Comparator<Profanity>(){
			public int compare(Profanity p1, Profanity p2)
			{
				return p1.getWord().compareTo(p2.getWord());
			} 
		};
	}

	public static Profanity getFromRecord(DbBlacklist dbBlacklist) {
		Profanity profanity = new Profanity(dbBlacklist.getWord(), dbBlacklist.getIsLiteral());
		profanity._dbBlacklist = dbBlacklist;
		return profanity;
	}
	
	public static Profanity create(String word, boolean isLiteral) {
		Profanity profanity = new Profanity(word, isLiteral);
		profanity._dbBlacklist = DbBlacklist.create(Config.V.database, profanity._word, profanity._isLiteral);
		return profanity;
	}

	Profanity(String word, boolean isLiteral) {
		this._word = normalize(word);
		this._isLiteral = isLiteral;
		prepare();
	}

	Profanity() {
	}

	public static String normalize(String word) { return Leet.decode(word.toLowerCase()); }

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
	String getPrimary() {return this._primaryEncoded; }
	String getSecondary() { return this._secondaryEncoded; }
	boolean hasSecondary() { return this._secondaryEncoded != null; }
	boolean isLiteral() { return this._isLiteral; }
	boolean isSameWord(String word) {return this._word.equalsIgnoreCase(normalize(word)); }
	int getProfanityLevel(String word) { 
		if (isSameWord(word)) return PROFANITY_LEVEL_LITERAL;
		return PROFANITY_LEVEL_SIMILAR;
	}
	public long getDbId() { return (this._dbBlacklist == null) ? 0 : this._dbBlacklist.getDbId(); }

	static List<Profanity> sortByWord(Collection<Profanity> collection) {
		ArrayList<Profanity> array = new ArrayList<Profanity>(collection);
		array.sort(profanityComparator);
		return array;
	}

	@Override
	public String toString() {
		String result = String.format("%s (%s)", getWord(), this._isLiteral ? "literal" : "not literal");
		if (hasSecondary()) result += String.format(" [%s, %s]", getPrimary(), getSecondary());
		else result += String.format(" [%s]", getPrimary());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Profanity)) return false;
		Profanity that = (Profanity) o;
		return (this._word.equalsIgnoreCase(that._word));
	}

	public void setIsLiteral(boolean isLiteral) { 
		this._isLiteral = isLiteral;
		this._dbBlacklist.update(this._isLiteral);
	}

	public void deleteFromDb() {
		if (this._dbBlacklist == null) return;

		DbSimilar.deleteByBlacklistId(Config.V.database, this._dbBlacklist.getDbId());
		DbWhitelist.deleteByBlacklistId(Config.V.database, this._dbBlacklist.getDbId());
		this._dbBlacklist.delete();
		this._dbBlacklist = null;
	}
}
