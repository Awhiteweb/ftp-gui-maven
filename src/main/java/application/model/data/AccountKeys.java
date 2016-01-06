package application.model.data;

public enum AccountKeys
{
	HOST( "host" ),
	USERNAME( "username" ),
	PASSWORD( "password" ),
	PATH( "path" ), 
	REMEMBER( "remember" );
	
	private String name;
	
	AccountKeys( String name )
	{
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}


}
