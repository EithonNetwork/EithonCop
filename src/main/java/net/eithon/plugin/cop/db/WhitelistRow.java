package net.eithon.plugin.cop.db;

import net.eithon.library.mysql.Row;

public class WhitelistRow extends Row {
	public WhitelistRow() {
		super("whitelist");
	}
	
	public String word;
	public long blacklist_id;
}
