package application.model;

import org.apache.commons.net.ftp.FTPFile;

public class FileDetails
{
	private String name;
	private String type;
	private long size;
	
	public FileDetails( FTPFile file )
	{
		name = file.getName();
		type = typeToString( file );
		size = file.getSize();
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSize()
	{
		return String.format( "%8d", Long.valueOf( size ) );
	}
	
	public String getType()
	{
		return type;
	}

	private String typeToString( FTPFile file )
	{
		if ( file.isFile() )
			return "file";
		else if ( file.isDirectory() )
			return "directory";
		else if ( file.isSymbolicLink() )
			return "symbolic link";
		else
			return "unkown";
	}
	
	
}
