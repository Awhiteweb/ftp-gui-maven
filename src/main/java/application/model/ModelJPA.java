package application.model;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

/**
 * 
 * @author Alex.White
 * TODO
 * <ul>
 * <li>debug ftp getting initial set of files</li>
 * </ul>
 */
public class ModelJPA
{	
	private DataService dataService;
	private AccountService accountService;
	
	private ObservableList<String> accounts;
	private static FTPClient ftp;
//	private static FTPClientConfig config;
	private int ftpReplyCode;
	private FTPFile[] ftpFileArray;
	private String root;
	
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
		accountService.resetData();
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
	 * @param account the host account
	 * @return Account object with all details
	 */
	public Account getLoginDetails( String account )
	{
		return accountService.findByName( account );
	}
	
	/**
	 * Connects to the ftp server
	 * @param connDetails connection details
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
			root = connDetails.get( AccountKeys.PATH );
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
		FTPClientConfig config = new FTPClientConfig();
		ftp.configure( config );
	}
	
	/**
	 * logs out of the ftp server
	 */
	public void logout()
	{
		dataService.truncate();
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
	 * compares stored data to dir, if it does not exist
	 * the connects to ftp and retrieves files
	 * @param dir String directory to change to
	 * @return boolean of success or failure
	 */
	public boolean changeDirectory( String dir )
	{
		if ( pathExists( dir ) )
			return true;
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
	
	/*
	 * TODO: needs to recognise if the inital root had a path name i.e. public_html 
	 * and update the path[] so it searches for: name&parent( path[1], 0 ) 
	 */
	private boolean pathExists( String dir )
	{
		String[] path = dirToArray( dir );
		if ( path.length < 1 )
			return false;
		
		DirFile parent = dataService.findByNameAndParent( path[0], 0 );
		if ( parent == null )
			return false;
		for ( int i = 1; i < path.length; i++ )
		{
			parent = dataService.findByNameAndParent( path[i], parent.getId() );
			if ( parent == null )
				return false;
		}
		return true;
	}

	/**
	 * Only used with login
	 * @return file list for root directory from ftp server
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
				DirFile f = new DirFile( file.getName(), 0, 
						file.getSize(), fileType( file.getType() ) );
				dataService.saveOrPersist( f );
				if ( f.getType() != DirFileType.FOLD )
					files.add( f );
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return sortDirFileList( files );
	}

	
	/**
	 * gets a list of the files available for the given directory
	 * @return List of directory files
	 */
	public List<DirFile> getFileList( String dir )
	{
		if ( dir.equals( "/" ) )
		{
			dataService.resetTestData();
			return sortDirFileList( dataService.findAll() );
		}
		else if ( pathExists( dir ) )
			return getDBFiles( dir );
		
		changeDirectory( dir );
		createParents( dir );
		int parentId = getParentId( dir );
		List<DirFile> files = new ArrayList<DirFile>();
		try
		{
			ftpFileArray = ftp.listFiles();
			for ( FTPFile file : ftpFileArray )
			{
				System.out.printf( "File type: %d | File name: %s%n", file.getType(), file.getName() );
				DirFile f = new DirFile( file.getName(), parentId, 
						file.getSize(), fileType( file.getType() ) );
				dataService.saveOrPersist( f );
				files.add( f );
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return sortDirFileList( files );
	}

	/**
	 * @param dir path to search
	 * @return sorted List from database
	 */
	private List<DirFile> getDBFiles( String dir )
	{
		String[] path = dirToArray( dir );
		DirFile parent = dataService.findByNameAndParent( path[0], 0 );
		for ( int i = 1; i < path.length; i++ )
		{
			parent = dataService.findByNameAndParent( path[i], parent.getId() );
		}
		return sortDirFileList( dataService.findByParent( parent.getId() ) );
	}

	/**
	 * @param dir path to split
	 * @return ordered array of the directory
	 */
	private String[] dirToArray( String dir )
	{
		dir = dir.substring( 0, 1 ).equals( "/" ) ? dir.substring( 1 ) : dir;
		return dir.split( "/" );
	}
	
	private void createParents( String dir )
	{
		String[] path = dirToArray( dir );
		DirFile parent = dataService.findByNameAndParent( path[0], 0 );
		if ( parent == null )
			return;
		for ( int i = 1; i < path.length; i++ )
		{
			parent = addChild( path[i], parent.getId() );
		}
	}

	private DirFile addChild( String path, int parent )
	{
		DirFile child = dataService.findByNameAndParent( path, parent );
		if ( child == null )
		{
			dataService.saveOrPersist( new DirFile( path, parent, DirFileType.FOLD ) );
			addChild( path, parent );
		}
		return child;
	}
	
	private int getParentId( String dir )
	{
		String[] path = dirToArray( dir );
		DirFile parent = dataService.findByNameAndParent( path[0], 0 );
		if ( parent == null )
			return 0;
		for ( int i = 1; i < path.length; i++ )
		{
			parent = dataService.findByNameAndParent( path[i], parent.getId() );
		}
		return parent.getId();
	}
	
	
	/**
	 * opens a file explorer window
	 * @param event object
	 * @param desktop object
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
	public List<TreeItem<DirFile>> writeTree() 
	{
		List<TreeItem<DirFile>> list = addChildren( 0 );
		if ( list != null )
			return list;
		return new ArrayList<TreeItem<DirFile>>();
	}

	private List<TreeItem<DirFile>> addChildren( int parent )
	{
		List<TreeItem<DirFile>> list = new ArrayList<TreeItem<DirFile>>();
		for ( DirFile f : dataService.findByParent( parent ) )
		{
			if ( f.getType() == DirFileType.FOLD )
			{
				TreeItem<DirFile> item = new TreeItem<DirFile>( f );
				List<TreeItem<DirFile>> children = addChildren( f.getId() );
				if ( children != null )
					item.getChildren().addAll( sortTreeList( children ) );
				list.add( item );
			}
		}
		if ( list.size() < 1 )
			return null;
		return sortTreeList( list );
	}
	
	public TreeItem<DirFile> getTreeItems() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param list of items to convert into Tree items
	 * @return list of tree items
	 */
	public List<TreeItem<DirFile>> addLeaves( List<DirFile> list )
	{
		List<TreeItem<DirFile>> branches = new ArrayList<TreeItem<DirFile>>();
		for ( DirFile s : list )
		{
			if ( s.getType() != DirFileType.FOLD )
				branches.add( new TreeItem<DirFile>( s ) );
		}
		return sortTreeList( branches );
	}
	
	/**
	 * @param item treeItem you need to turn into a directory path
	 * @param root the name of the directory root
	 * @return directory path
	 */
	public String getCurrentDirectoryString( TreeItem<DirFile> item, String root )
	{
		String path = "";
		boolean end = false;
		while ( !end )
		{
			if ( item.getValue().toString().equalsIgnoreCase( "root" ) )
			{
				if ( !root.equals( "/" ) )
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
			return; 
		DirFile file = new DirFile();
		List<DirFile> files = dataService.findByName( parent );
		if ( files == null || files.size() < 1 )
			System.out.println( "nothing in database" );
			//	connect to ftp
		else
		// TODO: needs updating 
			dataService.saveOrPersist( file );
	}

	private List<TreeItem<DirFile>> sortTreeList( List<TreeItem<DirFile>> list )
	{
		list.sort( ( t1, t2 ) -> 
					t1.getValue().toString().compareToIgnoreCase( t2.getValue().toString() ) );
		return list;
	}
	
	private List<DirFile> sortDirFileList( List<DirFile> list )
	{
		list.sort( ( f1, f2 ) -> f1.toString().compareToIgnoreCase( f2.toString() ) );
		return list;
	}

}
