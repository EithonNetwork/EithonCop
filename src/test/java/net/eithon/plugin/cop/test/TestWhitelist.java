package net.eithon.plugin.cop.test;

import static org.junit.Assert.assertEquals;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.db.DbBlacklist;
import net.eithon.plugin.cop.db.DbWhitelist;

import org.junit.Test;

public class TestWhitelist {

	@Test
	public void create() {
		String word = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbBlacklist blacklist = DbBlacklist.create(database, "x", false);
		DbWhitelist whitelist = DbWhitelist.create(database, word, blacklist.getDbId());
		assertEquals(word, whitelist.getWord());
		assertEquals(blacklist.getDbId(), whitelist.getBlacklistId());
	}	
	
	@Test
	public void getByName() {
		String word = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbBlacklist blacklist = DbBlacklist.create(database, "x", false);
		DbWhitelist whitelist = DbWhitelist.create(database, word, blacklist.getDbId());
		whitelist = DbWhitelist.getByWord(database, word);
		assertEquals(word, whitelist.getWord());
		assertEquals(blacklist.getDbId(), whitelist.getBlacklistId());
	}	
	
	@Test
	public void update() {
		String word = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbBlacklist blacklist = DbBlacklist.create(database, "x", false);
		DbWhitelist whitelist = DbWhitelist.create(database, word, blacklist.getDbId());
		whitelist = DbWhitelist.getByWord(database, word);
		whitelist.update();
		whitelist = DbWhitelist.getByWord(database, word);
		assertEquals(blacklist.getDbId(), whitelist.getBlacklistId());
	}

}
