


public class EditDB throws SQLException
{
	public static final String SQL_STATEMENT = "select * from channels";

	public static void main( Strings[] args ) throws SQLException
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