package net.eithon.plugin.cop.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.DbTable;
import net.eithon.library.mysql.IDbRecord;

public class DbBlacklist extends DbRecord<DbBlacklist> implements IDbRecord<DbBlacklist> {
	private String word;
	private boolean isLiteral;

	public static DbBlacklist create(Database database, String word, boolean isLiteral) {
		DbBlacklist blacklist = getByWord(database, word);
		if (blacklist == null) {
			blacklist = new DbBlacklist(database, word, isLiteral);
			blacklist.dbCreate();
		} else {
			blacklist.update(isLiteral);
		}
		return blacklist;
	}

	public static DbBlacklist getByWord(Database database, String word) {
		return getByWhere(database, "word=", word);
	}

	public static List<DbBlacklist> findAll(Database database) {
		return findByWhere(database, "1=", 1);
	}

	private DbBlacklist(Database database, String word, boolean isLiteral) {
		this(database);
		this.word = word;
		this.isLiteral = isLiteral;
	}

	private DbBlacklist(Database database) {
		super(new DbTable(database, "blacklist"));
	}

	private DbBlacklist(DbTable table, long id) {
		super(table, id);
	}
	
	public String getWord() { return this.word; }
	public boolean getIsLiteral() { return this.isLiteral; }

	@Override
	public String toString() {
		String result = String.format("%s (%s)", this.word, this.isLiteral ? "literal" : "not literal");
		return result;
	}

	public void update(boolean isLiteral) {
		this.isLiteral = isLiteral;
		dbUpdate();
	}

	private static DbBlacklist getByWhere(Database database, Object... whereParts) {
		DbBlacklist blacklist = new DbBlacklist(database);
		return blacklist.getByWhere(whereParts);
	}

	private static List<DbBlacklist> findByWhere(Database database, Object... whereParts) {
		DbBlacklist blacklist = new DbBlacklist(database);
		return blacklist.findByWhere(whereParts);
	}

	@Override
	public DbBlacklist fromDb(ResultSet resultSet) throws SQLException {
		this.word = resultSet.getString("word");
		this.isLiteral = resultSet.getBoolean("is_literal");
		return this;
	}

	@Override
	public HashMap<String, Object> getColumnValues() {
		HashMap<String, Object> columnValues = new HashMap<String, Object>();
		columnValues.put("word", this.word);
		columnValues.put("is_literal", new Boolean(this.isLiteral));
		return columnValues;
	}

	@Override
	public DbBlacklist factory(DbTable table, long id) {
		return new DbBlacklist(table, id);
	}
}
