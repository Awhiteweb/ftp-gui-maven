package application.database;

import org.apache.derby.jdbc.EmbeddedDriver;

import application.model.Account;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Connector
{
	private Connection conn;
	private String dbURL = "jdbc:derby:C:/Users/Alex.White/MyDB/root;create=true";
	private String user = "root";
	private String password = "password";

	public Connector()
	{
		try
		{
			connect();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void connect() throws SQLException
	{
		if ( conn != null )
			return;
		DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
		conn = DriverManager.getConnection( dbURL, user, password );
		if ( conn != null )
			System.out.println( "connect to db" );
		getMetaData();
	}
	
	private void getMetaData()
	{
		String[] types = {"TABLE"};
		try
		{
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet rs = dbm.getTables( null, null, "%", null );
			while ( rs.next() )
			{
				System.out.printf( "name: %s%ncatalog: %s%nschema: %s%n", rs.getString( 3 ), rs.getString( 1 ), rs.getString( 2 ) );				
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public List<Account> getAccountsFromSQL()
	{
		try
		{
			ResultSet rs = conn.prepareStatement( "SELECT * FROM users" ).executeQuery();
			while( rs.next() )
			{
				System.out.println( "record" );
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}		
		return new ArrayList<Account>();
	}

}
