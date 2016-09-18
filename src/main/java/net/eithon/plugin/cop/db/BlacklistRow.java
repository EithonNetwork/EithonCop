package net.eithon.plugin.cop.db;

import net.eithon.library.mysql.Row;

public class BlacklistRow extends Row {
	public BlacklistRow() {
		super("blacklist");
	}
	
	public String word;
	public boolean is_literal;
}
