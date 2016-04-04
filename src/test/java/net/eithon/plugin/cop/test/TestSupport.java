package net.eithon.plugin.cop.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.MySql;

import org.junit.Assert;

public class TestSupport {
	public static Database getDatabaseAndTruncateTables() {
		MySql mySql = new MySql("rookgaard.eithon.net", "3307", "DEV_e_cop", "DEV_e_cop", "ozGWcGs37hQs3aNp");
		try {
			Connection connection = mySql.getOrOpenConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `whitelist`");
			statement.executeUpdate("DELETE FROM `similar_to_blacklisted`");
			statement.executeUpdate("DELETE FROM `blacklist`");
			return mySql;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}
}
