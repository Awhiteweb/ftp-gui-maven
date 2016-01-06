package application.model;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import application.database.service.AccountService;
import application.database.service.DataService;
import application.model.data.Account;
import application.model.data.AccountKeys;
import application.model.data.DirFile;
import application.model.data.DirFileType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ModelJPA
{	
	private DataService dataService;
	private AccountService accountService;
	
	private ObservableList<String> accounts;
	private List<String> familyTree;
	private static FTPClient ftp;
	private static FTPClientConfig config;
	private int ftpReplyCode;
	private FTPFile[] ftpFileArray;
	
	/**
	 * sets up model 
	 * Initialises observable list of accounts
	 * Initialises account service
	 * Initialises database service
	 * retrieves existing accounts from database
	 * adds the account host names to the observable list
	 */
	public ModelJPA()
	{
		dataService = new DataService();
		accounts = FXCollections.observableArrayList();
		accountService = new AccountService();
		List<Account> accs = accountService.findAll();
		for ( Account acc : accs )
		{
			accounts.add( acc.getAccount() );
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
		return accountService.findByName( account );
	}
	
	/**
	 * Connects to the ftp server
	 * @param connDetails
	 * @return string message for apps console log
	 */
	public String connect( HashMap<AccountKeys, String> connDetails )
	{ 
		if ( ftp == null )
			configure();
		try
		{
			if ( connDetails.get( AccountKeys.REMEMBER ).equals( "true" ) )
				addAccount( connDetails );
			ftp.connect( connDetails.get( AccountKeys.HOST ) );
			ftp.login( connDetails.get( AccountKeys.USERNAME ), 
					   connDetails.get( AccountKeys.PASSWORD ) );
			ftpReplyCode = ftp.getReplyCode();
			ftp.setControlKeepAliveTimeout(300); // stays alive for 5 mins
			if( !FTPReply.isPositiveCompletion( ftpReplyCode ) )
			{
				ftp.disconnect();
				System.err.println( "FTP server refused connection." );
				System.exit(1);
			}
			ftp.changeWorkingDirectory( connDetails.get( AccountKeys.PATH ) );
			return "connected to server: reply code - " + ftpReplyCode;
		}
		catch (IOException e)
		{
			return e.getMessage();
		}
	}

	private void addAccount( HashMap<AccountKeys, String> connDetails )
	{
		accountService.saveOrPersist( 
				new Account( 
						connDetails.get( AccountKeys.HOST ), 
						connDetails.get( AccountKeys.USERNAME ), 
						connDetails.get( AccountKeys.PASSWORD ), 
						connDetails.get( AccountKeys.PATH ) 
						) 
				);
	}

	private void configure()
	{
		ftp = new FTPClient();
		config = new FTPClientConfig();
		ftp.configure( config );
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
	public List<DirFile> getFileList()
	{
		List<DirFile> files = new ArrayList<DirFile>();
		try
		{
			ftpFileArray = ftp.listFiles();
			for ( FTPFile file : ftpFileArray )
			{
				System.out.printf( "File type: %d | File name: %s%n", file.getType(), file.getName() );
				DirFile df = new DirFile();
				df.setName( file.getName() );
				df.setSize( file.getSize() );
				df.setType( fileType( file.getType() ) );
				files.add( df );
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
	public List<DirFile> getFileList( String dir )
	{
		dataService.resetTestData();
		List<DirFile> files = dataService.findAll();
		return files;
		
//		if ( directory.getDirectory( dir ) != null )
//			return directory.getDirectory( dir ).getContents();
		
//		changeDirectory( dir );
//		List<FileDetails> files = new ArrayList<FileDetails>();
//		try
//		{
//			ftpFileArray = ftp.listFiles();
//			for ( FTPFile file : ftpFileArray )
//			{
//				System.out.printf( "File type: %d | File name: %s%n", file.getType(), file.getName() );
//				files.add( new FileDetails( file.getName(), fileType( file.getType() ) ,file.getSize() ) );
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		return files;
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
	
	private DirFileType fileType( int type )
	{
		switch( type )
		{
		case 0:
			return DirFileType.FILE;
		case 1:
			return DirFileType.FOLD;
		case 2:
			return DirFileType.SYMB;
		default:
			return DirFileType.UNKN;
		}
	}

	private Stage getStage( ActionEvent event )
	{
		Node node = (Node) event.getSource();
		return (Stage) node.getScene().getWindow();
	}

	/**
	 * @return a list of TreeItems from the database
	 * 		using file names
	 */
	public List<TreeItem<String>> writeTree() 
	{
		List<TreeItem<String>> list = addChildren( 0 );
		if ( list != null )
			return list;
		return new ArrayList<TreeItem<String>>();
	}

	private List<TreeItem<String>> addChildren( int parent )
	{
		List<TreeItem<String>> list = new ArrayList<TreeItem<String>>();
		for ( DirFile f : dataService.findByParent( parent ) )
		{
			if ( f.getType() == DirFileType.FOLD )
			{
				TreeItem<String> item = new TreeItem<String>( f.getName() );
				List<TreeItem<String>> children = addChildren( f.getId() );
				if ( children != null )
					item.getChildren().addAll( children );
				list.add( item );
			}
		}
		if ( list.size() < 1 )
			return null;
		list.sort( ( t1, t2 ) -> ( t1.getValue().compareTo( t2.getValue() ) ) );
		return list;
	}
	
	public TreeItem<String> getTreeItems() 
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
	
//	private Path getPathObject( Path root, String name )
//	{
//		if ( root.getChild( name ) != null )
//			return root.getChild( name );
//		String dir = "";
//		for ( String s : familyTree )
//		{
//			dir = s + "/" + dir;
//		}
//		List<FileDetails> list = getFileList( dir );
//		Path item = new Path();
//		item.setName( name );
//		item.setParent( root.getName() );
//		item.setContents( list );
//		return item;
//	}
	
//	public Path findFamily( TreeItem<String> item, Path pathRoot ) 
//	{
//		Path returnItem = pathRoot;
//		familyTree = new ArrayList<String>();
//		listChildren( pathRoot.getName(), item );
//		if ( item.getValue().equals( pathRoot.getName() ) )
//			return pathRoot;
//		if ( item.getParent().getValue().equals( pathRoot.getName() ) )
//			return getPathObject( pathRoot, item.getValue() );
//		for ( int i = familyTree.size() - 2; i == 0; i-- )
//		{
//			if ( i == 0 )
//				returnItem = getPathObject( returnItem, familyTree.get( i ) );
//			else
//				returnItem = returnItem.getChild( familyTree.get( i ) );
//		}
//		return returnItem;
//	}
	
	public List<TreeItem<String>> addLeaves( List<String> list )
	{
		List<TreeItem<String>> branches = new ArrayList<TreeItem<String>>();
		for ( String s : list )
			branches.add( new TreeItem<String>( s ) );
		Collections.sort( branches, ( TreeItem<String> t1, TreeItem<String> t2 ) -> 
							t1.getValue().compareToIgnoreCase( t2.getValue() ) );
		return branches;
	}
	
	public String getCurrentDirectoryString( TreeItem<String> item, String root )
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
	
	public void startDirectory( String path, String name, String parent )
	{
		if ( path == null )
			return; // need to handle pressing connect without any details
//		Path p = new Path();
//		p.setName( name );
//		p.setParent( parent );
//		p.setContents( getFileList( path ) );
//		addDirectory( path, p );
	}
	
//	public void addDirectory( String dir, Path path )
//	{
//		directory.addDirectory( dir, path );
//	}
//
}
