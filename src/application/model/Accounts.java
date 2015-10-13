package application.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Accounts
{
	@JsonCreator
	public Accounts( 
			@JsonProperty("accounts") List<Account> accounts 
			)
	{
		this.accounts = accounts;
	}
	
	@JsonProperty("accounts")
	private List<Account> accounts;
	
	public List<Account> getAccounts()
	{
		return accounts;
	}

	public void setAccounts( List<Account> accounts )
	{
		this.accounts = accounts;
	}
}
