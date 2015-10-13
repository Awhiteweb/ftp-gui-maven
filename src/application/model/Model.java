package application.model;

import java.util.HashMap;
import application.model.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model
{
	public static final ObservableList<String> HOSTS = FXCollections.observableArrayList( "ftp.whiteslife.com", "php.dev.ovalbusinesssolutions.co.uk" );
	private ObservableList<String> accounts;
	private HashMap<String, String[]> loginDetails;
	
	public Model()
	{
		accounts = FXCollections.observableArrayList();
		loginDetails = new HashMap<String, String[]>();
		JsonReader reader = new JsonReader();
		Accounts accs = reader.getData();
		for ( Account acc : accs.getAccounts() )
		{
			accounts.add( acc.getAccount() );
			loginDetails.put( acc.getAccount(), new String[]{ acc.getUsername(), acc.getPassword() } );
		}
	}
	
	/**
	 * returns an ObservableList<String> of account hosts
	 * @return ObservableList<String> of account hosts
	 */
	public ObservableList<String> getAccounts()
	{
		return accounts;
	}
	
	/**
	 * returns the username and password for the given account
	 * @param host account
	 * @return String[] of username, passsword
	 */
	public String[] getLoginDetails( String account )
	{
		return loginDetails.get( account );
	}
	
}
