package net.eithon.plugin.cop.test;

import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.db.BlacklistRow;
import net.eithon.plugin.cop.db.BlacklistTable;
import net.eithon.plugin.cop.db.WhitelistRow;
import net.eithon.plugin.cop.db.WhitelistTable;

import org.junit.Test;

public class TestWhitelist {

	@Test
	public void create() {
		try {
			String word = "a";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			BlacklistTable blacklistTable = new BlacklistTable(database);
			WhitelistTable whitelistTable = new WhitelistTable(database);
			BlacklistRow blacklistRow = blacklistTable.create("x", false);
			WhitelistRow whitelistRow = whitelistTable.create(word, blacklistRow.id);
			Assert.assertEquals(word, whitelistRow.word);
			Assert.assertEquals(blacklistRow.id, whitelistRow.blacklist_id);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}	

	@Test
	public void getByName() {
		try {
			String word = "a";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			BlacklistTable blacklistTable = new BlacklistTable(database);
			WhitelistTable whitelistTable = new WhitelistTable(database);
			BlacklistRow blacklistRow = blacklistTable.create("x", false);
			WhitelistRow whitelistRow = whitelistTable.create(word, blacklistRow.id);
			whitelistRow = whitelistTable.getByWord(word);
			Assert.assertEquals(word, whitelistRow.word);
			Assert.assertEquals(blacklistRow.id, whitelistRow.blacklist_id);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}	

	@Test
	public void update() {
		try {
			String word = "a";
			Database database = TestSupport.getDatabaseAndTruncateTables();
			BlacklistTable blacklistTable = new BlacklistTable(database);
			WhitelistTable whitelistTable = new WhitelistTable(database);
			BlacklistRow blacklistRow = blacklistTable.create("x", false);
			Assert.assertNotNull(blacklistRow);
			WhitelistRow whitelistRow = whitelistTable.create(word, blacklistRow.id);
			Assert.assertNotNull(whitelistRow);
			whitelistRow = whitelistTable.getByWord(word);
			Assert.assertNotNull(whitelistRow);
			word = "b";
			whitelistRow.word = word;
			whitelistTable.update(whitelistRow);
			whitelistRow = whitelistTable.getByWord(word);
			Assert.assertNotNull(whitelistRow);
			Assert.assertEquals(blacklistRow.id, whitelistRow.blacklist_id);
			Assert.assertEquals(word, whitelistRow.word);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}
