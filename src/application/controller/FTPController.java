package application.controller;

import java.awt.Desktop;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Ellipse;
import application.model.Account;
import application.model.FileDetails;
import application.model.HashKeys;
import application.model.Model;

public class FTPController implements Initializable
{
	@FXML private Accordion accordian;
	@FXML private ComboBox<String> selectAccountBox;
	@FXML private TextField hostDetailsField;
	@FXML private TextField usernameField;
	@FXML private TextField passwordField;
	@FXML private TextField directoryField;
	@FXML private CheckBox rememberDetails;
	@FXML private TreeView<String> treeView;
	@FXML private TableColumn<FileDetails, String> fileNameCol;
	@FXML private TableColumn<FileDetails, String> fileSizeCol;
	@FXML private TableColumn<FileDetails, String> fileTypeCol;
	@FXML private ScrollPane consoleScrollPane;
	@FXML private Ellipse progressEllipse;
	@FXML private TitledPane connectionTitledPane;
	@FXML private TitledPane fileViewerTitledPane;
	@FXML private TitledPane consoleTitledPane;
	@FXML private TextArea consoleTextLabel;
	@FXML private TableView<FileDetails> tableView;
	@FXML private Button refreshButton;
	@FXML private Button downloadButton;
	@FXML private Button editButton;
	@FXML private Button uploadButton;
	private Desktop desktop = Desktop.getDesktop();
	private List<String> currentPath;
	private ObservableList<FileDetails> tableViewList;
	private Model model;
	private HashMap<HashKeys, String> connDetails;
	
	@Override
	public void initialize( URL arg0, ResourceBundle arg1 )
	{
		this.selectAccountBox.getItems().addAll( getModel().getAccounts() );
		this.accordian.getPanes();
		this.accordian.setExpandedPane( connectionTitledPane );
		consoleTextLabel.setEditable( false );
		initTree();
		initTable();
		currentPath = new ArrayList<String>();
		
	}

	@FXML 
	public void handleComboBoxSelect( ActionEvent event ) 
	{
		Account account = getModel().getLoginDetails( selectAccountBox.getSelectionModel().getSelectedItem() );
		hostDetailsField.setText( selectAccountBox.getSelectionModel().getSelectedItem() );
		usernameField.setText( account.getUsername() );
		passwordField.setText( account.getPassword() );
		directoryField.setText( account.getDirectory() );
	}
	
	@FXML 
	public void handleLoginButton( ActionEvent event ) 
	{
		if ( model != null )
			model.logout();
		Boolean remember = rememberDetails.isSelected();
		wTc( "connecting" );
		connDetails = new HashMap<HashKeys, String>();
		connDetails.put( HashKeys.HOST, hostDetailsField.getText() );
		connDetails.put( HashKeys.USERNAME, usernameField.getText() );
		connDetails.put( HashKeys.PASSWORD, passwordField.getText() );
		connDetails.put( HashKeys.PATH, directoryField.getText() );
		connDetails.put( HashKeys.REMEMBER, remember.toString() );
		currentPath.add( directoryField.getText() );
		wTc( String.format( "Connecting to: %nHost: %s,%nas User: %s", connDetails.get( HashKeys.HOST ), connDetails.get( HashKeys.USERNAME ) ) );
		wTc( model.connect( connDetails ) );
		wTc( "getting server contents" );
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception
			{
				writeFilesToViewer( getCurrentPath() );
				accordian.setExpandedPane( fileViewerTitledPane );
				return null;
			}
		};
		new Thread(task).start();
		wTc( "done" );
		accordian.setExpandedPane( consoleTitledPane );
	}

	private String getCurrentPath()
	{
		String path = "";
		for ( String p : currentPath )
		{
			path = p.equals("/") ? p : String.format( "%s/%s", path, p );
		}
		return path;
	}

	@FXML 
	public void handleClose( ActionEvent event ) 
	{
		model.logout();
		Platform.exit();
	}
	
	@FXML
	public void handleRefresh( ActionEvent event )
	{
		writeFilesToViewer( getCurrentPath() );
	}

	@FXML
	public void handleDownload( ActionEvent event )
	{
		model.openFileChooser( event, desktop, true );
	}
	
	@FXML
	public void handleUpload( ActionEvent event )
	{
		model.openFileChooser( event, desktop, false );
	}
	
	@FXML
	public void handleEdit( ActionEvent event )
	{
		
	}

	public void handleTreeView( MouseEvent event )
	{
		System.out.println( "tree event: " + event.getEventType().getName() );
		TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
		System.out.println( item.getValue() );
		expandTreeView( item );
//		if ( model.changeDirectory( item.getValue() ) )
//		{
//			currentPath.add( item.getValue() );
//			List<FileDetails> list = model.getFileList();
//		}	
	}
	
	public Model getModel()
	{
		if ( model == null )
			model = new Model();
		return model;
	}

	public void wTc( String update )
	{
		if ( consoleTextLabel.getText() == null || 
				consoleTextLabel.getText().equals( "" ) )
		{
			consoleTextLabel.setText( update );
		}
		else
		{
			consoleTextLabel.appendText( "\n" + update );
		}
	}

	private void initTree()
	{
		this.treeView.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
		this.treeView.getFocusModel();
		this.treeView.addEventHandler( MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
			handleTreeView(event);
		} );
	}

	private void initTable()
	{
		fileSizeCol.setResizable( false );
		fileTypeCol.setResizable( false );
		fileNameCol.setCellValueFactory( 
				new PropertyValueFactory<FileDetails, String>( "name" ) );
		fileSizeCol.setCellValueFactory( 
				new PropertyValueFactory<FileDetails, String>( "type" ) );
		fileSizeCol.setCellValueFactory( 
				new PropertyValueFactory<FileDetails, String>( "size" ) );
	}
	
	private void writeFilesToViewer( String dir )
	{
		TreeItem<String> root = new TreeItem<String>( "Root" ); 
		
		for ( FileDetails f : model.getFileList( dir ) )
		{
			TreeItem<String> child = addChild( f );
			if ( child != null )
			{					
				String nextDir = ( dir.equals( "/" ) ? String.format( "%s%s", dir, f.getName() ) : String.format( "%s/%s", dir, f.getName() ) );
				for ( FileDetails f1 : model.getFileList( nextDir ) )
				{
					TreeItem<String> gChild = addChild( f1 );
					if ( child != null )
					{					
						child.getChildren().add( gChild );
					}
				}
				child.setExpanded( true );
				root.getChildren().add( child );
			}
		}
		tableViewList = FXCollections.observableArrayList( model.getFileList( dir ) );
		tableView.setItems( tableViewList );
		root.setExpanded( true );
		treeView.setRoot( root );
	}

	private List<TreeItem<String>> addChildren( String dir, TreeItem<String> parent, int level )
	{
		level++;
		System.out.println( level );
		List<FileDetails> files = model.getFileList( dir );
		List<TreeItem<String>> tree = new ArrayList<TreeItem<String>>();
		if ( level < 3 )
		for ( FileDetails f : files )
		{
			String nextDir = ( dir.equals( "/" ) ? String.format( "%s%s", dir, f.getName() ) : String.format( "%s/%s", dir, f.getName() ) );
			TreeItem<String> item = addChild( f );
			if ( item != null )
			{					
				item.getChildren().addAll( addChildren( nextDir, item, level++ ) );
				tree.add( item );
			}
		}
		return tree;
	}

	private TreeItem<String> addChild( FileDetails f )
	{
		if ( f.getType().equals( "directory" ) && !f.getName().startsWith( "." ) )
			return new TreeItem<String>( f.getName() );
		else
			return null;
	}
	
	private void expandTreeView(TreeItem<?> item){
	    if(item != null && !item.isLeaf()){
	        item.setExpanded(true);
	        for(TreeItem<?> child:item.getChildren()){
	            expandTreeView(child);
	        }
	    }
	}

}
