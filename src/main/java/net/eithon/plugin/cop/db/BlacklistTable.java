package net.eithon.plugin.cop.db;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;

public class BlacklistTable extends DbTable<BlacklistRow> {

	public BlacklistTable(final Database database) throws FatalException {
		super(BlacklistRow.class, database);
	}
	
	public BlacklistRow createOrUpdate(String word, boolean isLiteral) throws FatalException, TryAgainException {
		BlacklistRow row = getByWord(word);
		if (row != null) return row;
		return create(word, isLiteral);
	}
	
	public BlacklistRow create(String word, boolean isLiteral) throws FatalException, TryAgainException {
		BlacklistRow row = new BlacklistRow();
		row.word = word;
		row.is_literal = isLiteral;
		long id = this.jDapper.createOne(row);
		return get(id);
	}

	public BlacklistRow getByWord(final String word) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("word=?", word);
	}
}
