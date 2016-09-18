package net.eithon.plugin.cop.test;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.db.BlacklistRow;
import net.eithon.plugin.cop.db.BlacklistTable;
import net.eithon.plugin.cop.db.SimilarRow;
import net.eithon.plugin.cop.db.SimilarTable;

import org.junit.Test;

public class TestSimilar {

	@Test
	public void create() {
		try {
			String word = "a";
			boolean isVerified = false;
			Database database = TestSupport.getDatabaseAndTruncateTables();
			BlacklistTable blacklistTable = new BlacklistTable(database);
			SimilarTable similarTable = new SimilarTable(database);
			BlacklistRow blacklistRow = blacklistTable.create("x", false);
			SimilarRow similar = similarTable.create(word, blacklistRow.id, isVerified);
			assertEquals(word, similar.word);
			assertEquals(blacklistRow.id, similar.blacklist_id);
			assertEquals(isVerified, similar.is_verified);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}	

	@Test
	public void getByName() {
		try {
			String word = "a";
			boolean isVerified = false;
			Database database = TestSupport.getDatabaseAndTruncateTables();
			BlacklistTable blacklistTable = new BlacklistTable(database);
			SimilarTable similarTable = new SimilarTable(database);
			BlacklistRow blacklistRow = blacklistTable.create("x", false);
			SimilarRow similarRow = similarTable.create(word, blacklistRow.id, isVerified);
			similarRow = similarTable.getByWord(word);
			assertEquals(word, similarRow.word);
			assertEquals(blacklistRow.id, similarRow.blacklist_id);
			assertEquals(isVerified, similarRow.is_verified);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}	

	@Test
	public void update() {
		try {
			String word = "a";
			boolean isVerified = false;
			Database database = TestSupport.getDatabaseAndTruncateTables();
			BlacklistTable blacklistTable = new BlacklistTable(database);
			SimilarTable similarTable = new SimilarTable(database);
			BlacklistRow blacklistRow = blacklistTable.create("x", false);
			SimilarRow similarRow = similarTable.create(word, blacklistRow.id, isVerified);
			similarRow = similarTable.getByWord(word);
			isVerified = !isVerified;
			similarRow.is_verified = isVerified;
			similarTable.update(similarRow);
			similarRow = similarTable.getByWord(word);
			assertEquals(blacklistRow.id, similarRow.blacklist_id);
			assertEquals(isVerified, similarRow.is_verified);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}
