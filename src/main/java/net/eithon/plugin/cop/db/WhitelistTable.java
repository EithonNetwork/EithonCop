package net.eithon.plugin.cop.db;

import java.util.List;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;

public class WhitelistTable extends DbTable<WhitelistRow> {

	public WhitelistTable(final Database database) throws FatalException {
		super(WhitelistRow.class, database);
	}
	
	public WhitelistRow createOrUpdate(String word, long blacklistId) throws FatalException, TryAgainException {
		WhitelistRow row = getByWord(word);
		if (row != null) return row;
		return create(word, blacklistId);
	}
	
	public WhitelistRow create(String word, long blacklistId) throws FatalException, TryAgainException {
		WhitelistRow row = new WhitelistRow();
		row.word = word;
		row.blacklist_id = blacklistId;
		long id = this.jDapper.createOne(row);
		return get(id);
	}

	public WhitelistRow getByWord(final String word) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("word=?", word);
	}

	public void deleteByWord(String word) throws FatalException, TryAgainException {
		this.jDapper.deleteWhere("word=?", word);
	}

	public void deleteByBlacklistId(long blacklistId) throws FatalException, TryAgainException {
		this.jDapper.deleteWhere("blacklist_id=?", blacklistId);
	}
	
	public List<WhitelistRow> findByBlacklistId(long blacklistId) throws FatalException, TryAgainException {
		return this.jDapper.readSomeWhere("blacklist_id=?", blacklistId);
	}
}
