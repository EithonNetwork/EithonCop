package net.eithon.plugin.cop.test;

import static org.junit.Assert.assertEquals;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.db.Blacklist;

import org.junit.Test;

public class TestBlacklist {

	@Test
	public void create() {
		String word = "a";
		boolean isLiteral = true;
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Blacklist blacklist = Blacklist.create(database, word, isLiteral);
		assertEquals(word, blacklist.getWord());
		assertEquals(isLiteral, blacklist.getIsLiteral());
	}	
	
	@Test
	public void getByName() {
		String word = "a";
		boolean isLiteral = true;
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Blacklist blacklist = Blacklist.create(database, word, isLiteral);
		blacklist = Blacklist.getByWord(database, word);
		assertEquals(word, blacklist.getWord());
		assertEquals(isLiteral, blacklist.getIsLiteral());
	}	
	
	@Test
	public void update() {
		String word = "a";
		boolean isLiteral = true;
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Blacklist blacklist = Blacklist.create(database, word, isLiteral);
		blacklist = Blacklist.getByWord(database, word);
		isLiteral = !isLiteral;
		blacklist.update(isLiteral);
		blacklist = Blacklist.getByWord(database, word);
		assertEquals(isLiteral, blacklist.getIsLiteral());
	}

}
