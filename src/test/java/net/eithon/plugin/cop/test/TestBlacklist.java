package net.eithon.plugin.cop.test;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.db.BlacklistRow;
import net.eithon.plugin.cop.db.BlacklistTable;

import org.junit.Test;

public class TestBlacklist {

	@Test
	public void create() {
		try {
			String word = "a";
			boolean isLiteral = true;
			Database database = TestSupport.getDatabaseAndTruncateTables();
			BlacklistTable handler = new BlacklistTable(database);
			BlacklistRow row = handler.create(word, isLiteral);
			assertEquals(word, row.word);
			assertEquals(isLiteral, row.is_literal);
		} catch (Exception e) {
			Assert.fail();
		}
	}	

	@Test
	public void getByName() {
		try  {
			String word = "a";
			boolean isLiteral = true;
			Database database = TestSupport.getDatabaseAndTruncateTables();
			BlacklistTable handler = new BlacklistTable(database);
			BlacklistRow row = handler.create(word, isLiteral);
			row = handler.getByWord(word);
			assertEquals(word, row.word);
			assertEquals(isLiteral, row.is_literal);
		} catch (Exception e) {
			Assert.fail();
		}
	}	

	@Test
	public void update() {
		try {
			String word = "a";
			boolean isLiteral = true;
			Database database = TestSupport.getDatabaseAndTruncateTables();
			BlacklistTable handler = new BlacklistTable(database);
			BlacklistRow row = handler.create(word, isLiteral);
			row = handler.getByWord(word);
			isLiteral = !isLiteral;
			row.is_literal = isLiteral;
			handler.update(row);
			row = handler.getByWord(word);
			assertEquals(isLiteral, row.is_literal);
		} catch (Exception e) {
			Assert.fail();
		}
	}

}
