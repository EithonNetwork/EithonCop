package net.eithon.plugin.cop.test;

import static org.junit.Assert.assertEquals;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.db.Blacklist;
import net.eithon.plugin.cop.db.Similar;

import org.junit.Test;

public class TestSimilar {

	@Test
	public void create() {
		String word = "a";
		boolean isVerified = false;
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Blacklist blacklist = Blacklist.create(database, "x", false);
		Similar similar = Similar.create(database, word, blacklist.getDbId(), isVerified);
		assertEquals(word, similar.getWord());
		assertEquals(blacklist.getDbId(), similar.getBlacklistId());
		assertEquals(isVerified, similar.getIsVerified());
	}	
	
	@Test
	public void getByName() {
		String word = "a";
		boolean isVerified = false;
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Blacklist blacklist = Blacklist.create(database, "x", false);
		Similar similar = Similar.create(database, word, blacklist.getDbId(), isVerified);
		similar = Similar.getByWord(database, word);
		assertEquals(word, similar.getWord());
		assertEquals(blacklist.getDbId(), similar.getBlacklistId());
		assertEquals(isVerified, similar.getIsVerified());
	}	
	
	@Test
	public void update() {
		String word = "a";
		boolean isVerified = false;
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Blacklist blacklist = Blacklist.create(database, "x", false);
		Similar similar = Similar.create(database, word, blacklist.getDbId(), isVerified);
		similar = Similar.getByWord(database, word);
		isVerified = !isVerified;
		similar.update(isVerified);
		similar = Similar.getByWord(database, word);
		assertEquals(blacklist.getDbId(), similar.getBlacklistId());
		assertEquals(isVerified, similar.getIsVerified());
	}

}
