package net.eithon.plugin.cop.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbRecord;
import net.eithon.library.mysql.DbTable;
import net.eithon.library.mysql.IDbRecord;

public class Blacklist extends DbRecord<Blacklist> implements IDbRecord<Blacklist> {
	private String word;
	private boolean isLiteral;

	public static Blacklist create(Database database, String word, boolean isLiteral) {
		Blacklist blacklist = getByWord(database, word);
		if (blacklist == null) {
			blacklist = new Blacklist(database, word, isLiteral);
			blacklist.dbCreate();
		} else {
			blacklist.update(isLiteral);
		}
		return blacklist;
	}

	public static Blacklist getByWord(Database database, String word) {
		return getByWhere(database, String.format("word='%s'", word));
	}

	private Blacklist(Database database, String word, boolean isLiteral) {
		this(database);
		this.word = word;
		this.isLiteral = isLiteral;
	}

	private Blacklist(Database database) {
		super(new DbTable(database, "blacklist"));
	}

	private Blacklist(DbTable table, long id) {
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

	private static Blacklist getByWhere(Database database, String where) {
		Blacklist blacklist = new Blacklist(database);
		return blacklist.getByWhere(where);
	}

	@Override
	public Blacklist fromDb(ResultSet resultSet) throws SQLException {
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
	public Blacklist factory(DbTable table, long id) {
		return new Blacklist(table, id);
	}
}
