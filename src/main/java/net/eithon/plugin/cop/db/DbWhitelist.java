package net.eithon.plugin.cop.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.IDbRecord;

public class DbWhitelist extends DbRecord<DbWhitelist> implements IDbRecord<DbWhitelist> {
	private String word;
	private long blacklistId;
	
	public static DbWhitelist create(Database database, String word, long blacklistId) {
		DbWhitelist whitelist = getByWord(database, word);
		if (whitelist == null) {
			whitelist = new DbWhitelist(database, word, blacklistId);
			whitelist.dbCreate();
		}
		return whitelist;
	}

	public static DbWhitelist getByWord(Database database, String word) {
		return getByWhere(database, "word=?", word);
	}

	public static List<DbWhitelist> findAll(Database database) {
		return findByWhere(database, "1=1");
	}

	public static void deleteByWord(Database database, String word) {
		DbWhitelist whitelist = getByWord(database, word);
		whitelist.delete();
	}

	public static void deleteByBlacklistId(Database database, long blacklistId) {
		DbWhitelist whitelist = new DbWhitelist(database);
		whitelist.deleteByWhere("blacklist_id=?", blacklistId);
	}
	
	public static List<DbWhitelist> findByBlacklistId(Database database, long blacklistId) {
		return findByWhere(database, "blacklist_id=?", blacklistId);
	}


	private DbWhitelist(Database database, String word, long blacklistId) {
		this(database);
		this.word = word;
		this.blacklistId = blacklistId;
	}

	private DbWhitelist(Database database) {
		this(database, -1);
	}

	protected DbWhitelist(Database database, long id) {
		super(database, "whitelist", id);
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

	private static DbWhitelist getByWhere(Database database, String format, Object... arguments) {
		DbWhitelist whitelist = new DbWhitelist(database);
		return whitelist.getByWhere(format, arguments);
	}

	private static List<DbWhitelist> findByWhere(Database database, String format, Object... arguments) {
		DbWhitelist whitelist = new DbWhitelist(database);
		return whitelist.findByWhere(format, arguments);
	}

	@Override
	public DbWhitelist fromDb(ResultSet resultSet) throws SQLException {
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
	public DbWhitelist factory(Database database, long id) {
		return new DbWhitelist(database, id);
	}
}