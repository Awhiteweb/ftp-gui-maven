package application.model.data;

public enum DirFileType
{
	FOLD ("folder" ),
	FILE ("file" ),
	SYMB ( "symbolic link" ),
	UNKN ( "unkown" );
	
	private String name;
	
	DirFileType( String name )
	{
		this.name = name;
	}
	
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
