package application.model.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
@NamedQueries( { 
	@NamedQuery( name = "Account.findAll", 
			query = "SELECT a FROM Account a" ),
	@NamedQuery( name = "Account.findByName", 
			query = "SELECT a FROM Account a WHERE LOWER(a.account) LIKE :filter" ),
	@NamedQuery( name = "Account.findById", 
			query = "SELECT a FROM Account a WHERE a.id = :filter" ),
})
@Entity
public class Account
{
	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	private int id;
	private String account; 
	private String username;
	private String password;
	private String directory;

	public Account(){}
	
	public Account( String account, String username,
					String password, String directory )
	{
		this.account = account;
		this.username = username;
		this.password = password;
		this.directory = directory;
	}
	
	public void setId( int id )
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getAccount()
	{
		return account;
	}

	public void setAccount( String account )
	{
		this.account = account;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword( String password )
	{
		this.password = password;
	}

	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory( String directory )
	{
		this.directory = directory;
	}
}