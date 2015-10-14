package application.model;

import org.apache.commons.net.ftp.FTPFile;

public class FileDetails
{
	private String name;
	private int type;
	private long size;
	
	public FileDetails( FTPFile file )
	{
		name = file.getName();
		type = file.getType();
		size = file.getSize();
	}
	
	public String getName()
	{
		return name;
	}
	
	public long getSize()
	{
		return size;
	}
	
	public int getType()
	{
		return type;
	}

}
