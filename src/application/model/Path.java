package application.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Path
{
	/*
	 Object needs to be able to represent a tree to remember parents and children
	 either probably a Set of ArrayLists which store hashmaps of folders and files
	 */
	private HashMap<String,Path> children;
	private List<FileDetails> contents;
	private String name;
	private String parent;
	private List<String> folders;
	private List<FileDetails> files;
	
	public Path()
	{
		children = new HashMap<String,Path>();
	}

	public void setName( String name )
	{
		this.name = name;
	}
	
	public void setParent( String parent )
	{
		this.parent = parent;
	}
	
	public void setContents( List<FileDetails> contents )
	{
		this.contents = contents;
		splitContents();
	}
	
	public void addContents( FileDetails content )
	{
		this.contents.add( content );
		splitContents();
	}
	
	public void addChild( String name, Path contents )
	{
		this.children.put( name, contents );
	}

	public String getName()
	{
		return name;
	}
	
	public String getParent()
	{
		return parent;
	}
	
	public List<FileDetails> getContents()
	{
		return contents;
	}
	
	public List<String> getFolders()
	{
		return folders;
	}
	
	public List<FileDetails> getFiles()
	{
		return files;
	}
	
	public Path getChild( String name )
	{
		return children.get( name );
	}
	
	private void splitContents()
	{
		folders = new ArrayList<String>();
		files = new ArrayList<FileDetails>();
		for ( FileDetails f : contents )
		{
			if ( f.getType().equals( HashKeys.TYPE_DIR.getName() ) )
				if ( !f.getName().equals(".") && !f.getName().equals( ".." ) )
					folders.add( f.getName() );
			else
				files.add( f );
		}
	}
	
}
