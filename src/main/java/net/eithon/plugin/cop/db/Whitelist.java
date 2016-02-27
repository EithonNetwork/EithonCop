package net.eithon.plugin.cop.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.DbTable;
import net.eithon.library.mysql.IDbRecord;

public class Whitelist extends DbRecord<Whitelist> implements IDbRecord<Whitelist> {
	private String word;
	private long blacklistId;
	public static Whitelist create(Database database, String word, long blacklistId) {
		Whitelist whitelist = getByWord(database, word);
		if (whitelist == null) {
			whitelist = new Whitelist(database, word, blacklistId);
			whitelist.dbCreate();
		}
		return whitelist;
	}

	public static Whitelist getByWord(Database database, String word) {
		return getByWhere(database, String.format("word='%s'", word));
	}

	private Whitelist(Database database, String word, long blacklistId) {
		this(database);
		this.word = word;
		this.blacklistId = blacklistId;
	}

	private Whitelist(Database database) {
		super(new DbTable(database, "whitelist"));
	}

	private Whitelist(DbTable table, long id) {
		super(table, id);
	}

	private Whitelist() {
		super();
	}

	public String getWord() { return this.word; }
	public long getBlacklistId() { return this.blacklistId; }

	@Override
	public String toString() {
		return getWord();
	}

	public void update() {
		dbUpdate();
	}

	private static Whitelist getByWhere(Database database, String where) {
		Whitelist whitelist = new Whitelist(database);
		return whitelist.getByWhere(where);
	}

	@Override
	public Whitelist fromDb(ResultSet resultSet) throws SQLException {
		this.word = resultSet.getString("word");
		this.blacklistId = resultSet.getLong("blacklist_id");
		return this;
	}

	@Override
	public HashMap<String, Object> getColumnValues() {
		HashMap<String, Object> columnValues = new HashMap<String, Object>();
		columnValues.put("word", this.word);
		columnValues.put("blacklist_id", new Long(this.blacklistId));
		return columnValues;
	}

	@Override
	public Whitelist factory(DbTable table, long id) {
		return new Whitelist(table, id);
	}
}
