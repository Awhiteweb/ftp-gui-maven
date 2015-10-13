package application.model;

import java.util.List;

public class Accounts
{
	private List<Account> accounts;
	
	public List<Account> getAccounts()
	{
		return accounts;
	}

	public void setAccounts( List<Account> accounts )
	{
		this.accounts = accounts;
	}

	
	class Account
	{
		private String host, username, password;
	
		public String getHost()
		{
			return host;
		}
	
		public void setHost( String host )
		{
			this.host = host;
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
	}
}
