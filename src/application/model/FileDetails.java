package application.model;

public class FileDetails
{
	private String name;
	private String type;
	private long size;
	
	public FileDetails( String name, String type, long size )
	{
		this.name = name;
		this.type = type;
		this.size = size;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSize()
	{
		return String.format( "%8d", Long.valueOf( size ) );
	}
	
	public long getSizeLong()
	{
		return size;
	}
	
	public String getType()
	{
		return type;
	}
}
