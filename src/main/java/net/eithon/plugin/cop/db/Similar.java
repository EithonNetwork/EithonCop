package net.eithon.plugin.cop.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.DbTable;
import net.eithon.library.mysql.IDbRecord;

public class Similar extends DbRecord<Similar> implements IDbRecord<Similar> {
	private String word;
	private long blacklistId;
	private boolean isVerified;
	public static Similar create(Database database, String word, long blacklistId, boolean isVerified) {
		Similar similar = getByWord(database, word);
		if (similar == null) {
			similar = new Similar(database, word, blacklistId, isVerified);
			similar.dbCreate();
		}
		return similar;
	}

	public static Similar getByWord(Database database, String word) {
		return getByWhere(database, "word=", word);
	}

	private Similar(Database database, String word, long blacklistId, boolean isVerified) {
		this(database);
		this.word = word;
		this.blacklistId = blacklistId;
		this.isVerified = isVerified;
	}

	private Similar(Database database) {
		super(new DbTable(database, "similar_to_blacklisted"));
	}

	protected Similar(DbTable table, long id) {
		super(table, id);
	}

	protected Similar(DbTable dbTable) {
		super(dbTable);
	}

	public String getWord() { return this.word; }
	public long getBlacklistId() { return this.blacklistId; }
	public boolean getIsVerified() { return this.isVerified; }

	@Override
	public String toString() {
		String result = String.format("%s (%s)", this.word, this.isVerified ? "verified" : "not verified");
		return result;
	}

	public void update(boolean isVerified) {
		this.isVerified = isVerified;
		dbUpdate();
	}

	private static Similar getByWhere(Database database, Object... whereParts) {
		Similar similar = new Similar(database);
		return similar.getByWhere(whereParts);
	}

	@Override
	public Similar fromDb(ResultSet resultSet) throws SQLException {
		this.word = resultSet.getString("word");
		this.blacklistId = resultSet.getLong("blacklist_id");
		this.isVerified = resultSet.getBoolean("is_verified");
		return this;
	}

	@Override
	public HashMap<String, Object> getColumnValues() {
		HashMap<String, Object> columnValues = new HashMap<String, Object>();
		columnValues.put("word", this.word);
		columnValues.put("blacklist_id", new Long(this.blacklistId));
		columnValues.put("is_verified", new Boolean(this.isVerified));
		return columnValues;
	}

	@Override
	public Similar factory(DbTable table, long id) {
		return new Similar(table, id);
	}
}
