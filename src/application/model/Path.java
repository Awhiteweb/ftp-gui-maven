package application.model;

import java.util.ArrayList;
import java.util.List;

public class Path
{
	/*
	 Object needs to be able to represent a tree to remember parents and children
	 either probably a Set of ArrayLists which store hashmaps of folders and files
	 */
	
	private List<String> path;
	
	public Path()
	{
		path = new ArrayList<String>();
	}
	
	public String getCurrent()
	{
		return toString();
	}
	
	public void setCurrent( String dir )
	{
		path.add( dir );
	}
	
	@Override
	public String toString()
	{
		String newPath = "";
		for ( String p : path )
		{
			newPath = p.equals("/") ? p : String.format( "%s/%s", newPath, p );
		}
		return newPath;
	}
}
