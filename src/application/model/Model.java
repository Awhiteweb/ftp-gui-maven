package application.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import application.model.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model
{
	public static final ObservableList<String> HOSTS = FXCollections.observableArrayList( "ftp.whiteslife.com", "php.dev.ovalbusinesssolutions.co.uk" );
	private ObservableList<String> accounts;
	private HashMap<String, Account> loginDetails;
	private static FTPClient ftp;
	private static FTPClientConfig config;
	private int ftpReplyCode;
	private FTPFile[] ftpFileArray;

	public Model()
	{
		accounts = FXCollections.observableArrayList();
		loginDetails = new HashMap<String, Account>();
		JsonReader reader = new JsonReader();
		Accounts accs = reader.getData();
		for ( Account acc : accs.getAccounts() )
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
	 * @return String[] of username, passsword
	 */
	public Account getLoginDetails( String account )
	{
		return loginDetails.get( account );
	}
	
	public String connect( HashMap<String, String> connDetails )
	{
		if ( ftp == null )
			configure();
		try
		{
			ftp.connect( connDetails.get( "host" ) );
			ftp.login( connDetails.get( "username" ), 
					   connDetails.get( "password" ) );
			ftpReplyCode = ftp.getReplyCode();
			ftp.setControlKeepAliveTimeout(300); // stays alive for 5 mins
			if( !FTPReply.isPositiveCompletion( ftpReplyCode ) )
			{
				ftp.disconnect();
				System.err.println( "FTP server refused connection." );
				System.exit(1);
			}
			return "connected to server: reply code - " + ftpReplyCode;
		}
		catch (IOException e)
		{
			return e.getMessage();
		}
	}
	
	private void configure()
	{
		ftp = new FTPClient();
		config = new FTPClientConfig();
		ftp.configure( config );
	}
	
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
	
	public boolean changeDirectory( String dir ) throws IOException
	{
		if ( ftp.isConnected() )
		{
			ftp.changeWorkingDirectory( dir );
			return true;
		}
		return false;
	}
	
	public List<FileDetails> getFileList()
	{
		List<FileDetails> files = new ArrayList<FileDetails>();
		try
		{
			ftpFileArray = ftp.listFiles();
			for ( FTPFile file : ftpFileArray )
			{
				System.out.printf( "File name: %s%nfile type: %d%n", file.getName(), file.getType() );
				files.add( new FileDetails( file ) );
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return files;
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
