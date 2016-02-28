package net.eithon.plugin.cop.test;

import static org.junit.Assert.assertEquals;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.db.Blacklist;
import net.eithon.plugin.cop.db.Whitelist;

import org.junit.Test;

public class TestWhitelist {

	@Test
	public void create() {
		String word = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Blacklist blacklist = Blacklist.create(database, "x", false);
		Whitelist whitelist = Whitelist.create(database, word, blacklist.getDbId());
		assertEquals(word, whitelist.getWord());
		assertEquals(blacklist.getDbId(), whitelist.getBlacklistId());
	}	
	
	@Test
	public void getByName() {
		String word = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Blacklist blacklist = Blacklist.create(database, "x", false);
		Whitelist whitelist = Whitelist.create(database, word, blacklist.getDbId());
		whitelist = Whitelist.getByWord(database, word);
		assertEquals(word, whitelist.getWord());
		assertEquals(blacklist.getDbId(), whitelist.getBlacklistId());
	}	
	
	@Test
	public void update() {
		String word = "a";
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Blacklist blacklist = Blacklist.create(database, "x", false);
		Whitelist whitelist = Whitelist.create(database, word, blacklist.getDbId());
		whitelist = Whitelist.getByWord(database, word);
		whitelist.update();
		whitelist = Whitelist.getByWord(database, word);
		assertEquals(blacklist.getDbId(), whitelist.getBlacklistId());
	}

}
