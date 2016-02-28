package net.eithon.plugin.cop.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.DbTable;
import net.eithon.library.mysql.IDbRecord;

public class DbSimilar extends DbRecord<DbSimilar> implements IDbRecord<DbSimilar> {
	private String word;
	private long blacklistId;
	private boolean isVerified;
	public static DbSimilar create(Database database, String word, long blacklistId, boolean isVerified) {
		DbSimilar similar = getByWord(database, word);
		if (similar == null) {
			similar = new DbSimilar(database, word, blacklistId, isVerified);
			similar.dbCreate();
		}
		return similar;
	}

	public static DbSimilar getByWord(Database database, String word) {
		return getByWhere(database, "word=", word);
	}

	public static List<DbSimilar> findAll(Database database) {
		return findByWhere(database, "1=", 1);
	}

	public static void deleteByBlacklistId(Database database, long blacklistId) {
		DbSimilar similar = new DbSimilar(database);
		similar.deleteByWhere("blacklist_id=", blacklistId);
	}

	private DbSimilar(Database database, String word, long blacklistId, boolean isVerified) {
		this(database);
		this.word = word;
		this.blacklistId = blacklistId;
		this.isVerified = isVerified;
	}

	private DbSimilar(Database database) {
		super(new DbTable(database, "similar_to_blacklisted"));
	}

	protected DbSimilar(DbTable table, long id) {
		super(table, id);
	}

	protected DbSimilar(DbTable dbTable) {
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

	private static DbSimilar getByWhere(Database database, Object... whereParts) {
		DbSimilar similar = new DbSimilar(database);
		return similar.getByWhere(whereParts);
	}

	private static List<DbSimilar> findByWhere(Database database, Object... whereParts) {
		DbSimilar similar = new DbSimilar(database);
		return similar.findByWhere(whereParts);
	}

	@Override
	public DbSimilar fromDb(ResultSet resultSet) throws SQLException {
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
	public DbSimilar factory(DbTable table, long id) {
		return new DbSimilar(table, id);
	}
}
