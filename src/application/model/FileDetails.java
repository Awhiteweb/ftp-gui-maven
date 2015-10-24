package application.model;

import java.util.ArrayList;
import java.util.List;

public class FileDetails
{
	private String name;
	private String type;
	private long size;
	private List<FileDetails> children;
	
	public FileDetails( String name, String type, long size )
	{
		this.name = name;
		this.type = type;
		this.size = size;
		if ( type.equals( "directory" ) )
			this.children = new ArrayList<FileDetails>();
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
	
	public List<FileDetails> getChildren()
	{
		return children;
	}

	public void setChildren( FileDetails child )
	{
		this.children.add( child );
	}
	
}
