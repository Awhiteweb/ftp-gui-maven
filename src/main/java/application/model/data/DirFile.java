package application.model.data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
/**
 * 
 * @author Alex
 *<br/>
 * <p>Constructors:<br/>
 * <ul>
 * <li>empty</li>
 * <li>String name</li>
 * <li>String name, int parent</li>
 * <li>String name, int parent, long size</li>
 * <li>String name, int parent, DirFileType type</li>
 * <li>String name, int parent, long size, DirFileType type</li>
 * </ul>
 * <br/>
 * object returns name as toString()</p>
 *
 */
@NamedQueries( { 
	@NamedQuery( name = "DirFile.findAll", 
			query = "SELECT f FROM DirFile f" ),
	@NamedQuery( name = "DirFile.findByName", 
			query = "SELECT f FROM DirFile f WHERE LOWER(f.name) LIKE :filter" ),
	@NamedQuery( name = "DirFile.findById", 
			query = "SELECT f FROM DirFile f WHERE f.id = :filter" ),
	@NamedQuery( name = "DirFile.findByParent", 
			query = "SELECT f FROM DirFile f WHERE f.parent = :filter" ),
	@NamedQuery( name = "DirFile.findRoot", 
			query = "SELECT f FROM DirFile f WHERE f.id = 0" ),
	@NamedQuery( name = "DirFile.findByNameAndParent",
			query = "SELECT f FROM DirFile f WHERE LOWER(f.name) LIKE :name "
					+ "AND f.parent = :parent" )
})
@Entity
public class DirFile implements Serializable
{
	private static final long serialVersionUID = 8370297286264971828L;

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	private int id;

	private String name;
	private int parent;
	private DirFileType type;
	private Number size;

	
	public DirFile(){}
	public DirFile( String name )
	{
		this.name = name;
	}
	public DirFile( String name, int parent )
	{
		this.name = name;
		this.parent = parent;
	}
	public DirFile( String name, int parent, long size )
	{
		this.name = name;
		this.parent = parent;
		this.size = size;
	}
	public DirFile( String name, int parent, DirFileType type )
	{
		this.name = name;
		this.parent = parent;
		this.type = type;
	}
	public DirFile( String name, int parent, long size, DirFileType type )
	{
		this.name = name;
		this.parent = parent;
		this.size = size;
		this.type = type;
	}
	
	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId( int id )
	{
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName( String name )
	{
		this.name = name;
	}

	/**
	 * @return the parent
	 */
	public int getParent()
	{
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent( int parent )
	{
		this.parent = parent;
	}

	/**
	 * @return the type
	 */
	public DirFileType getType()
	{
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType( DirFileType type )
	{
		this.type = type;
	}

	/**
	 * @return the size
	 */
	public Number getSize()
	{
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize( Number size )
	{
		this.size = size;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
