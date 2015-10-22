package application.model;

public enum HashKeys
{
	HOST( "host", 0 ),
	USERNAME( "username", 1 ),
	PASSWORD( "password", 2 ),
	PATH( "path", 3 ), 
	REMEMBER( "remember", 4 );
	
	
	private String name;
	private int ordinal;
	
	HashKeys( String name, int ordinal )
	{
		this.name = name;
		this.ordinal = ordinal;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the ordinal
	 */
	public int getOrdinal()
	{
		return ordinal;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
