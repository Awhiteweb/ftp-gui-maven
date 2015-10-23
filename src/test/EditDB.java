package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EditDB
{
	public static final String SQL_STATEMENT = "select * from channels";

	public static void main( String[] args ) throws SQLException
	{
		Connection connection = DriverManager.getConnection( CreateDB.JDBC_URL );
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery( SQL_STATEMENT );
		while( resultSet.next() )
		{

		}
		if ( resultSet != null ) resultSet.close();
		if ( statement != null ) statement.close();
		if ( connection != null ) connection.close();
	}
}