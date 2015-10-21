package application.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import application.database.Connector;

public class Model
{	
	private ObservableList<String> accounts;
	private HashMap<String, Account> loginDetails;
	private static FTPClient ftp;
	private static FTPClientConfig config;
	private int ftpReplyCode;
	private FTPFile[] ftpFileArray;
	private Connector conn;
	
	/**
	 * sets up model 
	 * initialises observable list of accounts
	 * initialises hashmap for login accounts
	 * initialises database connector
	 * retrieves existing accounts from database
	 * adds the account host names to the observable list
	 * adds the accounts to the hashmap
	 */
	public Model()
	{
		accounts = FXCollections.observableArrayList();
		loginDetails = new HashMap<String, Account>();
		conn = new Connector();
		List<Account> accs = conn.getAccounts();
		for ( Account acc : accs )
		{
			accounts.add( acc.getAccount() );
			loginDetails.put( acc.getAccount(), acc );
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
	 * @return Account object with all details
	 */
	public Account getLoginDetails( String account )
	{
		return loginDetails.get( account );
	}
	
	/**
	 * Connects to the ftp server
	 * @param connDetails
	 * @return string message for apps console log
	 */
	public String connect( HashMap<HashKeys, String> connDetails )
	{
		if ( ftp == null )
			configure();
		try
		{
			if ( connDetails.get( HashKeys.REMEMBER ).equals( "true" ) )
				addHost( connDetails );
			ftp.connect( connDetails.get( HashKeys.HOST ) );
			ftp.login( connDetails.get( HashKeys.USERNAME ), 
					   connDetails.get( HashKeys.PASSWORD ) );
			ftpReplyCode = ftp.getReplyCode();
			ftp.setControlKeepAliveTimeout(300); // stays alive for 5 mins
			if( !FTPReply.isPositiveCompletion( ftpReplyCode ) )
			{
				ftp.disconnect();
				System.err.println( "FTP server refused connection." );
				System.exit(1);
			}
			ftp.changeWorkingDirectory( connDetails.get( HashKeys.PATH ) );
			return "connected to server: reply code - " + ftpReplyCode;
		}
		catch (IOException e)
		{
			return e.getMessage();
		}
	}
	
	/**
	 * logs out of the ftp server
	 */
	public void logout()
	{
		if( ftp != null && ftp.isConnected() )
			try 
			{
				ftp.logout();
				System.out.println( "logged out" );
				ftp.disconnect();
			} 
			catch( IOException ioe )
			{
				System.err.println( "ftp disconnect error" );
				ioe.printStackTrace();
			}
	}
	
	/**
	 * used to change directory of on the ftp server
	 * @param dir String directory to change to
	 * @return boolean of success or failure
	 * @throws IOException
	 */
	public boolean changeDirectory( String dir ) throws IOException
	{
		if ( ftp.isConnected() )
		{
			ftp.changeWorkingDirectory( dir );
			return true;
		}
		return false;
	}
	
	/**
	 * gets a list of the files available
	 * @return List<FileDetails>
	 */
	public List<FileDetails> getFileList()
	{
		List<FileDetails> files = new ArrayList<FileDetails>();
		try
		{
			ftpFileArray = ftp.listFiles();
			for ( FTPFile file : ftpFileArray )
			{
				System.out.printf( "File type: %d | File name: %s%n", file.getName(), file.getType() );
				files.add( new FileDetails( file ) );
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return files;
	}
	
	private void configure()
	{
		ftp = new FTPClient();
		config = new FTPClientConfig();
		ftp.configure( config );
	}
	
	private void addHost( HashMap<HashKeys, String> connDetails )
	{
		conn = new Connector();
		if ( conn.addAccount( connDetails ) )
			System.out.println( "account added" );
	}

	private void download( String dir, String file )
	{
		String dl = dir + "/" + file;
		try ( FileOutputStream fs1 = new FileOutputStream( new File("./src/main/resources/" + file ) ) )
		{
			if ( ftp.retrieveFile( dl, fs1 ) )
				System.out.println( dl + " downloaded" );
		}
		catch( IOException ioe )
		{
			System.err.println( "download error" );
			ioe.printStackTrace();
		}
	}
	
	private void upload( String dir, String file )
	{
		String ul = dir + "/" + file;
		try (FileInputStream fis = new FileInputStream( new File("./src/main/resources/" + file ) ) )
		{
			ftp.setFileType( FTP.BINARY_FILE_TYPE );
			ftp.setFileTransferMode( FTP.BINARY_FILE_TYPE );
			
			if ( ftp.storeFile( ul, fis ) )
				System.out.println( "file uploaded" );			
		}
		catch ( IOException ioe )
		{
			System.err.println( "upload error" );
			ioe.printStackTrace();
		}
	}
		
}
