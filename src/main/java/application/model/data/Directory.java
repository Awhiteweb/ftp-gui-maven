package application.model.data;

import java.util.HashMap;

public class Directory {

	private HashMap<String, Path> directory;
	
	public Directory()
	{
		directory = new HashMap<String, Path>();
	}
	
	public Path getDirectory( String dir )
	{
		return directory.get( dir );
	}
	
	public void addDirectory( String dir, Path path )
	{
		directory.put( dir, path );
	}
}
