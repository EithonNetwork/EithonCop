package net.eithon.plugin.cop.profanity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.db.BlacklistRow;
import net.eithon.plugin.cop.db.BlacklistTable;
import net.eithon.plugin.cop.db.SimilarTable;
import net.eithon.plugin.cop.db.WhitelistTable;

class Profanity {
	static final int PROFANITY_LEVEL_NONE = 0;
	static final int PROFANITY_LEVEL_LITERAL = 1;
	static final int PROFANITY_LEVEL_COMPOSED = 2;
	static final int PROFANITY_LEVEL_SIMILAR = 3;
	static final int PROFANITY_LEVEL_MAX = 3;
	static Metaphone3 metaphone3;

	private static Comparator<Profanity> profanityComparator;
	private static BlacklistTable blacklistTable;
	private static WhitelistTable whitelistTable;
	private static SimilarTable similarTable;

	private String _primaryEncoded;
	private String _secondaryEncoded;
	private BlacklistRow blacklistRow;

	static void initialize(Database database) throws FatalException {
		blacklistTable = new BlacklistTable(database);
		whitelistTable = new WhitelistTable(database);
		similarTable = new SimilarTable(database);
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

	public static Profanity getFromRecord(BlacklistRow dbBlacklist) {
		Profanity profanity = new Profanity(dbBlacklist.word, dbBlacklist.is_literal);
		profanity.blacklistRow = dbBlacklist;
		return profanity;
	}
	
	public static Profanity create(String word, boolean isLiteral) throws FatalException, TryAgainException {
		Profanity profanity = new Profanity(word, isLiteral);
		profanity.blacklistRow = blacklistTable.create(profanity.getWord(), profanity.getIsLiteral());
		return profanity;
	}

	private boolean getIsLiteral() { return this.blacklistRow.is_literal;}

	Profanity(String word, boolean isLiteral) {
		this.blacklistRow = new BlacklistRow();
		this.blacklistRow.word = normalize(word);
		this.blacklistRow.is_literal = isLiteral;
		prepare();
	}

	Profanity() {
	}

	public static String normalize(String word) { return Leet.decode(word.toLowerCase()); }

	private void prepare() {
		synchronized (metaphone3) {
			metaphone3.SetWord(this.getWord());
			metaphone3.Encode();
			this._primaryEncoded = metaphone3.GetMetaph();
			String secondaryEncoded = metaphone3.GetAlternateMetaph();
			if (secondaryEncoded.length() > 0) this._secondaryEncoded = secondaryEncoded;
		}
	}

	public String getWord() { return this.blacklistRow.word; }
	String getPrimary() {return this._primaryEncoded; }
	String getSecondary() { return this._secondaryEncoded; }
	boolean hasSecondary() { return this._secondaryEncoded != null; }
	boolean isLiteral() { return this.blacklistRow.is_literal; }
	boolean isSameWord(String word) {return this.getWord().equalsIgnoreCase(normalize(word)); }
	int getProfanityLevel(String word) { 
		if (isSameWord(word)) return PROFANITY_LEVEL_LITERAL;
		return PROFANITY_LEVEL_SIMILAR;
	}
	public long getDbId() { return (this.blacklistRow == null) ? 0 : this.blacklistRow.getId(); }

	static List<Profanity> sortByWord(Collection<Profanity> collection) {
		ArrayList<Profanity> array = new ArrayList<Profanity>(collection);
		array.sort(profanityComparator);
		return array;
	}

	@Override
	public String toString() {
		String result = String.format("%s (%s)", getWord(), this.getIsLiteral() ? "literal" : "not literal");
		if (hasSecondary()) result += String.format(" [%s, %s]", getPrimary(), getSecondary());
		else result += String.format(" [%s]", getPrimary());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Profanity)) return false;
		Profanity that = (Profanity) o;
		return (this.getWord().equalsIgnoreCase(that.getWord()));
	}

	public void deleteFromDb() throws FatalException, TryAgainException {
		if (this.blacklistRow == null) return;

		similarTable.deleteByBlacklistId(this.blacklistRow.getId());
		whitelistTable.deleteByBlacklistId(this.blacklistRow.getId());
		blacklistTable.delete(this.getDbId());
		this.blacklistRow = null;
	}

	public void setIsLiteral(boolean isLiteral) { this.blacklistRow.is_literal = isLiteral; }

	public void save() throws FatalException, TryAgainException { blacklistTable.update(this.blacklistRow); }
}
