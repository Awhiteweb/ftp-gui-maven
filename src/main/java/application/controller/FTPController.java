package application.controller;

import java.awt.Desktop;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import application.model.data.AccountKeys;
import application.model.data.DirFile;
import application.model.data.DirFileType;
import application.model.ModelJPA;
import application.model.data.Account;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
	@FXML private TableColumn<DirFile, String> fileNameCol;
	@FXML private TableColumn<DirFile, Integer> fileSizeCol;
	@FXML private TableColumn<DirFile, DirFileType> fileTypeCol;
	@FXML private ScrollPane consoleScrollPane;
	@FXML private Ellipse progressEllipse;
	@FXML private TitledPane connectionTitledPane;
	@FXML private TitledPane fileViewerTitledPane;
	@FXML private TitledPane consoleTitledPane;
	@FXML private TextArea consoleTextLabel;
	@FXML private TableView<DirFile> tableView;
	@FXML private Button refreshButton;
	@FXML private Button downloadButton;
	@FXML private Button editButton;
	@FXML private Button uploadButton;
	private TreeItem<String> root;
	private Desktop desktop = Desktop.getDesktop();
	private String currentPath;
	private String rootPath;
	private ObservableList<DirFile> tableViewList;
	private ModelJPA model;
	private HashMap<AccountKeys, String> connDetails;
	
	@Override
	public void initialize( URL arg0, ResourceBundle arg1 )
	{
		this.selectAccountBox.getItems().addAll( getModel().getAccounts() );
		this.accordian.getPanes();
		this.accordian.setExpandedPane( connectionTitledPane );
		consoleTextLabel.setEditable( false );
		initTree();
		initTable();
		currentPath = "/";
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
		setConnectionDetails( remember );
		currentPath += directoryField.getText();
		rootPath = currentPath;
		model.startDirectory( currentPath, directoryField.getText(), "root" );
		wTc( String.format( "Connecting to: %nHost: %s,%nas User: %s", 
				connDetails.get( AccountKeys.HOST ), 
				connDetails.get( AccountKeys.USERNAME ) ) );
		wTc( model.connect( connDetails ) );
		wTc( "getting server contents" );
		updateTableView( model.getFileList( currentPath ) );
		setTreeRoot();
		wTc( "done" );
		accordian.setExpandedPane( fileViewerTitledPane );
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
		writeTree();
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

	public ModelJPA getModel()
	{
		if ( model == null )
			model = new ModelJPA();
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
		this.treeView.addEventHandler( MouseEvent.MOUSE_CLICKED, ( MouseEvent event ) -> {
			handleTreeView( event );
		} );
		root = new TreeItem<String>( "root" );
	}

	private void initTable()
	{
		fileSizeCol.setResizable( false );
		fileTypeCol.setResizable( false );
		fileNameCol.setCellValueFactory( 
				new PropertyValueFactory<DirFile, String>( "name" ) );
		fileTypeCol.setCellValueFactory( 
				new PropertyValueFactory<DirFile, DirFileType>( "type" ) );
		fileSizeCol.setCellValueFactory( 
				new PropertyValueFactory<DirFile, Integer>( "size" ) );
	}
	
	private void setConnectionDetails( Boolean remember )
	{
		connDetails = new HashMap<AccountKeys, String>();
		connDetails.put( AccountKeys.HOST, hostDetailsField.getText() );
		connDetails.put( AccountKeys.USERNAME, usernameField.getText() );
		connDetails.put( AccountKeys.PASSWORD, passwordField.getText() );
		connDetails.put( AccountKeys.PATH, directoryField.getText() );
		connDetails.put( AccountKeys.REMEMBER, remember.toString() );
	}

	private void setTreeRoot()
	{
		root.getChildren().addAll( writeTree() );
		root.setExpanded( true );
		treeView.setRoot( root );
	}

	private void handleTreeView( MouseEvent event )
	{
		System.out.println( "tree event: " + event.getEventType().getName() );
		TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
		System.out.println( item.getValue() );
		currentPath = model.getCurrentDirectoryString( item, rootPath );
		if ( item.isLeaf() )
		{
			if ( model.changeDirectory( currentPath ) )
			{
				
//				Path p = new Path();
//				p.setName( item.getValue() );
//				p.setParent( item.getParent().getValue() );
//				p.setContents( model.getFileList( currentPath ) );
//				model.addDirectory( currentPath, p );
//				pathMap.put( currentPath, p );
//				Path child = model.findFamily( item, pathRoot );
//				item.getChildren().addAll( model.addLeaves( p.getFolders() ) );
				if ( !item.isExpanded() )
					item.setExpanded( true );
			}
		}		
		updateTableView( model.getFileList( currentPath ) );
	}
	
	private void updateTableView( List<DirFile> list )
	{
		tableViewList = FXCollections.observableArrayList( list );
		tableView.setItems( tableViewList );
	}

	private List<TreeItem<String>> writeTree()
	{
		return model.writeTree();
	}
	
}
