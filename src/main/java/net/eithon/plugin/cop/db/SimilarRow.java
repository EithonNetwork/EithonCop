package net.eithon.plugin.cop.db;

import net.eithon.library.mysql.Row;

public class SimilarRow extends Row {
	public SimilarRow() {
		super("similar_to_blacklisted");
	}
	
	public String word;
	public long blacklist_id;
	public boolean is_verified;
}
