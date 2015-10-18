package application.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.model.Account;
import application.model.HashKeys;

public class Connector
{
	private Connection conn;
	private String user = "root";
	private String password = "password";
	private final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private final String JDBC_URL = "jdbc:derby:database;user=users;create=true;";


	public Connector()
	{
		try
		{
			connect();
			if ( !exists() )
				newDatabase();
		}
		catch ( ClassNotFoundException ce )
		{
			ce.printStackTrace();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void connect() throws SQLException, ClassNotFoundException
	{
		if ( conn != null )
			return;
		DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
		conn = DriverManager.getConnection( JDBC_URL, user, password );
		if ( conn != null )
			System.out.println( "connect to db" );
	}
	
	private boolean exists() throws SQLException
	{
		ResultSet rs = conn.getMetaData().getTables(null, null, "%", null);
		while ( rs.next() )
		{
			if ( rs.getString( 3 ).equalsIgnoreCase( "users" ) )
			{
				rs.close();
				return true;
			}
		}
		rs.close();
		return false;
	}

	public List<Account> getAccountsFromSQL()
	{
		try
		{
			ResultSet rs = conn.createStatement().executeQuery( "SELECT * FROM users" );
			while( rs.next() )
			{
				System.out.println( "record" );
			}
			rs.close();
		}
		catch (SQLException e)
		{
			System.out.println( e.getSQLState() );
			System.out.println( e.getErrorCode() );
			System.out.println( e.getMessage() );
			e.printStackTrace();
		}		
		return new ArrayList<Account>();
	}
	
	private void newDatabase() throws ClassNotFoundException, SQLException
	{
		String create = String.format("create table users ( %s varchar(100), "
			+ "%s varchar(100), %s varchar(100), %s varchar(100) )", 
			HashKeys.HOST, HashKeys.USERNAME, HashKeys.PASSWORD, HashKeys.PATH );
		Class.forName( DRIVER );
		Connection connection = DriverManager.getConnection( JDBC_URL );
		connection.createStatement().execute( create );
	}
	
	public boolean addAccount() throws SQLException
	{
		String insert = String.format( "insert into users ( %s, %s, %s, %s ) "
				+ "values ( 'ftp.whiteslife.com', 'whitesli', 'blue\\Ch1lcroft', '/' )", 
				HashKeys.HOST, HashKeys.USERNAME, HashKeys.PASSWORD, HashKeys.PATH );
		return conn.createStatement().execute( insert );
	}
}
