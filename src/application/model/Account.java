package application.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Account
{
	@JsonProperty("account") private String account; 
	@JsonProperty("username") private String username;
	@JsonProperty("password") private String password;
	@JsonProperty("directory") private String directory;

	@JsonCreator
	public Account( @JsonProperty("account") String account, 
					@JsonProperty("username") String username,
					@JsonProperty("password") String password,
					@JsonProperty("directory") String directory )
	{
		this.account = account;
		this.username = username;
		this.password = password;
		this.directory = directory;
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