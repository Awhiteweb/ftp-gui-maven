package application.model;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import application.database.Connector;

public class Model
{	
	private ObservableList<String> accounts;
	private List<String> familyTree;
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
	public boolean changeDirectory( String dir )
	{
		if ( ftp.isConnected() )
		{
			try
			{
				ftp.changeWorkingDirectory( dir );
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
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
				System.out.printf( "File type: %d | File name: %s%n", file.getType(), file.getName() );
				files.add( new FileDetails( file.getName(), fileType( file.getType() ) ,file.getSize() ) );
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return files;
	}

	/**
	 * gets a list of the files available for the given directory
	 * @return List<FileDetails>
	 */
	public List<FileDetails> getFileList( String dir )
	{
		changeDirectory( dir );
		List<FileDetails> files = new ArrayList<FileDetails>();
		try
		{
			ftpFileArray = ftp.listFiles();
			for ( FTPFile file : ftpFileArray )
			{
				System.out.printf( "File type: %d | File name: %s%n", file.getType(), file.getName() );
				files.add( new FileDetails( file.getName(), fileType( file.getType() ) ,file.getSize() ) );
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return files;
	}

	/**
	 * opens a file explorer window
	 * @param event
	 * @param desktop
	 * @param direction: <ul><li><strong>true</strong> to download or </li>
	 * 					 <li><strong>false</strong> to upload</li></ul>
	 */
	public void openFileChooser( ActionEvent event, Desktop desktop, boolean direction )
	{
		FileChooser fileChooser = new FileChooser();
		if ( direction )
		{
			fileChooser.setTitle( "download file to..." );
			fileChooser.setInitialFileName( "file.txt" );
			File file = fileChooser.showOpenDialog( getStage( event ) );
			if ( file != null ) {
				save( file );
			}
		}
		else
		{
			fileChooser.setTitle( "File(s) to upload" );
			List<File> list = fileChooser.showOpenMultipleDialog( getStage( event ) );
			if ( list != null ) {
	            for ( File file : list ) {
	                openFile( file, desktop );
	            }
	        }
		}
	}
	
	private void save( File file )
	{
		// TODO: download files from server
	}

	private void openFile( File file, Desktop desktop  ) 
	{
		// TODO: upload files from server
        try 
        {
            desktop.open( file );
        } 
        catch ( IOException ex ) 
        {
        	ex.printStackTrace();
        }
    }
	
	private String fileType( int type )
	{
		switch( type )
		{
		case 0:
			return HashKeys.TYPE_FILE.getName();
		case 1:
			return HashKeys.TYPE_DIR.getName();
		case 2:
			return HashKeys.TYPE_SYMB.getName();
		default:
			return HashKeys.TYPE_UNKN.getName();
		}
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
	
	private Stage getStage( ActionEvent event )
	{
		Node node = (Node) event.getSource();
		return (Stage) node.getScene().getWindow();
	}

		
	private List<TreeItem<String>> addChildren( Path child )
	{
		List<TreeItem<String>> list = new ArrayList<TreeItem<String>>();
		for ( String s : child.getFolders() )
		{
			TreeItem<String> item = new TreeItem<String>( s );
			if ( child.getChild( s ) != null )
				item.getChildren().addAll( addChildren( child.getChild( s ) ) );
			list.add( item );
		}
		Collections.sort( list, ( TreeItem<String> t1, TreeItem<String> t2 ) -> 
							t1.getValue().compareToIgnoreCase( t2.getValue() ) );
		return list;
	}

	public List<TreeItem<String>> writeTree( Path pathRoot ) 
	{
		List<TreeItem<String>> list = new ArrayList<TreeItem<String>>();
		for ( String s : pathRoot.getFolders() )
		{
			TreeItem<String> item = new TreeItem<String>( s );
			if ( pathRoot.getChild( s ) != null )
				item.getChildren().addAll( addChildren( pathRoot.getChild( s ) ) );
			list.add( item );
		}
		Collections.sort( list, ( TreeItem<String> t1, TreeItem<String> t2 ) -> 
							t1.getValue().compareToIgnoreCase( t2.getValue() ) );
		return list;
	}

	public TreeItem<String> getTreeItems(Path pathRoot) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	private void listChildren( String root, TreeItem<String> child )
	{
		familyTree.add( child.getValue() );
		if ( child.getValue().equals( root ) )
			return;
		listChildren( root, child.getParent() );
	}
	
	private Path getPathObject( Path root, String name )
	{
		if ( root.getChild( name ) != null )
			return root.getChild( name );
		String dir = "";
		for ( String s : familyTree )
		{
			dir = s + "/" + dir;
		}
		List<FileDetails> list = getFileList( dir );
		Path item = new Path();
		item.setName( name );
		item.setParent( root.getName() );
		item.setContents( list );
		return item;
	}
	
	public Path findFamily( TreeItem<String> item, Path pathRoot ) 
	{
		Path returnItem = pathRoot;
		familyTree = new ArrayList<String>();
		listChildren( pathRoot.getName(), item );
		if ( item.getValue().equals( pathRoot.getName() ) )
			return pathRoot;
		if ( item.getParent().getValue().equals( pathRoot.getName() ) )
			return getPathObject( pathRoot, item.getValue() );
		for ( int i = familyTree.size() - 2; i == 0; i-- )
		{
			if ( i == 0 )
				returnItem = getPathObject( returnItem, familyTree.get( i ) );
			else
				returnItem = returnItem.getChild( familyTree.get( i ) );
		}
		return returnItem;
	}
	
	public List<TreeItem<String>> addLeaves( List<String> list )
	{
		List<TreeItem<String>> branches = new ArrayList<TreeItem<String>>();
		for ( String s : list )
			branches.add( new TreeItem<String>( s ) );
		Collections.sort( branches, ( TreeItem<String> t1, TreeItem<String> t2 ) -> 
							t1.getValue().compareToIgnoreCase( t2.getValue() ) );
		return branches;
	}
	
	public String getCurrentDirectory( TreeItem<String> item, String root )
	{
		String path = "";
		boolean end = false;
		while ( !end )
		{
			if ( item.getValue().equalsIgnoreCase( "root" ) )
			{
				path = root + path;
				end = true;
			}
			else
			{
				path = "/" + item.getValue() + path;
				item = item.getParent();
			}
		}
		return path;
	}

}
