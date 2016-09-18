package net.eithon.plugin.cop.db;

import java.util.List;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;

public class SimilarTable extends DbTable<SimilarRow> {

	public SimilarTable(final Database database) throws FatalException {
		super(SimilarRow.class, database);
	}
	
	public SimilarRow createOrUpdate(String word, long blacklistId, boolean isVerified) throws FatalException, TryAgainException {
		SimilarRow row = getByWord(word);
		if (row != null) return row;
		return create(word, blacklistId, isVerified);
	}
	
	public SimilarRow create(String word, long blacklistId, boolean isVerified) throws FatalException, TryAgainException {
		SimilarRow row = new SimilarRow();
		row.word = word;
		row.blacklist_id = blacklistId;
		long id = this.jDapper.createOne(row);
		return get(id);
	}

	public SimilarRow getByWord(final String word) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("word=?", word);
	}

	public void deleteByWord(String word) throws FatalException, TryAgainException {
		this.jDapper.deleteWhere("word=?", word);
	}

	public void deleteByBlacklistId(long blacklistId) throws FatalException, TryAgainException {
		this.jDapper.deleteWhere("blacklist_id=?", blacklistId);
	}
	
	public List<SimilarRow> findByBlacklistId(long blacklistId) throws FatalException, TryAgainException {
		return this.jDapper.readSomeWhere("blacklist_id=?", blacklistId);
	}
}
