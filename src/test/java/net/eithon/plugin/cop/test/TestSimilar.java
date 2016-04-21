package net.eithon.plugin.cop.test;

import static org.junit.Assert.assertEquals;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.db.DbBlacklist;
import net.eithon.plugin.cop.db.DbSimilar;

import org.junit.Test;

public class TestSimilar {

	@Test
	public void create() {
		String word = "a";
		boolean isVerified = false;
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbBlacklist blacklist = DbBlacklist.create(database, "x", false);
		DbSimilar similar = DbSimilar.create(database, word, blacklist.getDbId(), isVerified);
		assertEquals(word, similar.getWord());
		assertEquals(blacklist.getDbId(), similar.getBlacklistId());
		assertEquals(isVerified, similar.getIsVerified());
	}	
	
	@Test
	public void getByName() {
		String word = "a";
		boolean isVerified = false;
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbBlacklist blacklist = DbBlacklist.create(database, "x", false);
		DbSimilar similar = DbSimilar.create(database, word, blacklist.getDbId(), isVerified);
		similar = DbSimilar.getByWord(database, word);
		assertEquals(word, similar.getWord());
		assertEquals(blacklist.getDbId(), similar.getBlacklistId());
		assertEquals(isVerified, similar.getIsVerified());
	}	
	
	@Test
	public void update() {
		String word = "a";
		boolean isVerified = false;
		Database database = TestSupport.getDatabaseAndTruncateTables();
		DbBlacklist blacklist = DbBlacklist.create(database, "x", false);
		DbSimilar similar = DbSimilar.create(database, word, blacklist.getDbId(), isVerified);
		similar = DbSimilar.getByWord(database, word);
		isVerified = !isVerified;
		similar.update(isVerified);
		similar = DbSimilar.getByWord(database, word);
		assertEquals(blacklist.getDbId(), similar.getBlacklistId());
		assertEquals(isVerified, similar.getIsVerified());
	}

}
