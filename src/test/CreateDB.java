package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * creates a new database in the project at the root level
 * ( same level as the src folder ) if doesn't exist then
 * the folder is created
 */
public class CreateDB
{
	public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	public static final String JDBC_URL = "jdbc:derby:myDB;create=true";


	public static void main ( String[] args ) throws ClassNotFoundException, SQLException
	{
		Class.forName( DRIVER );
		Connection connection = DriverManager.getConnection( JDBC_URL );
		connection.createStatement().execute( "create table users ( host varchar(100), " +
			"username varchar(100), password varchar(100), path varchar(100) )" );

	}
}