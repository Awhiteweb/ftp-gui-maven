package application.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javafx.scene.control.TreeItem;

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
	
	public Set<String> getKeySet()
	{
		return directory.keySet();
	}
	
	public List<TreeItem<String>> getTreeList()
	{
		List<TreeItem<String>> list = new ArrayList<TreeItem<String>>();
		
		Set<String> set = getKeySet();
		for ( String s : set )
		{
			String[] path = splitPath( s );
			if ( path[0].equals( "" ) )
			{
				
			}
		}
		
		return list;
	}
	
	private String[] splitPath( String path )
	{
		return path.split( "/" );
	}
	
}
