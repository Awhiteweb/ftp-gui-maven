package application.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import application.model.Account;
import application.model.HashKeys;

public class Connector
{
	private Connection conn;
	private final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private final String JDBC_URL = "jdbc:derby:database;create=true;"; //user=users;

	/**
	 * used for testing purposes
	 * @param args
	 */
	public static void main(String[] args)
	{
		Connector c = new Connector();
//		HashMap<HashKeys, String> map = new HashMap<HashKeys, String>();
//		map.put( HashKeys.HOST, "ftp.whiteslife.com" );
//		c.removeAccount( map );
		for ( Account account : c.getAccounts() )
		{
			System.out.printf( "dir: %s%nuser: %s%n", account.getDirectory(), account.getUsername() );
		}
	}
	
	public List<Account> getAccounts()
	{
		try
		{
			connect();
			return returnAccounts();
		}
		catch (SQLException e)
		{
			System.out.println( e.getSQLState() );
			System.out.println( e.getErrorCode() );
			System.out.println( e.getMessage() );
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		return new ArrayList<Account>();
	}
	
	public boolean addAccount( HashMap<HashKeys, String> details )
	{
		boolean success = false;
		
		String host = details.get( HashKeys.HOST ) == null ? "" : details.get( HashKeys.HOST );
		String username = details.get( HashKeys.USERNAME ) == null ? "" : details.get( HashKeys.USERNAME );
		String password = details.get( HashKeys.PASSWORD ) == null ? "" : details.get( HashKeys.PASSWORD );
		String path = details.get( HashKeys.PATH ) == null ? "" : details.get( HashKeys.PATH );
		
		String search = "SELECT * FROM users WHERE " + HashKeys.HOST + " LIKE '%" + host + "%'";
		String update = String.format( "UPDATE users SET %s='%s', %s='%s', %s='%s' WHERE %s='%s'", 
				HashKeys.USERNAME, username, 
				HashKeys.PASSWORD, password, 				
				HashKeys.PATH, path, 
				HashKeys.HOST, host );
		String insert = String.format( "insert into users ( %s, %s, %s, %s ) "
				+ "values ( '%s', '%s', '%s', '%s' )",
				HashKeys.HOST, HashKeys.USERNAME, HashKeys.PASSWORD, HashKeys.PATH,
				host, username,	password, path );
		try
		{
			connect();
			if ( searchDB( details, search ) )
				conn.createStatement().execute( update );
			else
				conn.createStatement().execute( insert );
			success = searchDB( details, search );
		}
		catch (SQLException e)
		{
			System.out.println( e.getSQLState() );
			System.out.println( e.getErrorCode() );
			System.out.println( e.getMessage() );
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
		return success;		
	}

	private boolean searchDB( HashMap<HashKeys, String> details, String search )
			throws SQLException
	{
		ResultSet rs = conn.createStatement().executeQuery( search );
		boolean match = false;
		while ( rs.next() )
		{
			if ( rs.getString( HashKeys.HOST.toString() ).equals( details.get( HashKeys.HOST ) ) )
				match = true;
		}
		return match;
	}

	
	private void close()
	{
		if ( conn != null )
			try
			{
				conn.close();
			}
			catch (SQLException e)
			{
				System.out.println( "unable to close Connection" );
				e.printStackTrace();
			}
	}

	private void connect() throws SQLException, ClassNotFoundException
	{
		if ( conn != null )
			return;
		conn = DriverManager.getConnection( JDBC_URL );
		if ( conn != null )
			System.out.println( "connect to db" );
		if ( !exists() )
			newDatabase();		
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
	
	private void newDatabase() throws ClassNotFoundException, SQLException
	{
		String create = String.format("create table users ( %s varchar(100), "
			+ "%s varchar(100), %s varchar(100), %s varchar(100) )", 
			HashKeys.HOST, HashKeys.USERNAME, HashKeys.PASSWORD, HashKeys.PATH );
		Class.forName( DRIVER );
		Connection connection = DriverManager.getConnection( JDBC_URL );
		connection.createStatement().execute( create );
	}
	
	private List<Account> returnAccounts()
	{
		ArrayList<Account> accounts = new ArrayList<Account>();
		try
		{
			ResultSet rs = conn.createStatement().executeQuery( "SELECT * FROM users" );
			while( rs.next() )
			{
				System.out.println( rs.getString( 1 ) );
				Account a = new Account( rs.getString( 1 ), rs.getString( 2 ), 
										rs.getString( 3 ), rs.getString( 4 ) );
				accounts.add( a );
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
		return accounts;
	}
	
	private void removeAccount( HashMap<HashKeys, String> details )
	{
		String drop = String.format( "DELETE FROM users WHERE %s='%s'", HashKeys.HOST, details.get( HashKeys.HOST ) );
		try
		{
			connect();
			conn.createStatement().execute( drop );
		}
		catch ( ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		catch ( SQLException e )
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
	}
}
