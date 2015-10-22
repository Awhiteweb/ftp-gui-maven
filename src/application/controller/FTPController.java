package application.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
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
	private List<String> currentPath;
	private ObservableList<FileDetails> tableViewList;
	private Model model;
	private List<FileDetails> files;
	private HashMap<HashKeys, String> connDetails;
	
	@Override
	public void initialize( URL arg0, ResourceBundle arg1 )
	{
		this.selectAccountBox.getItems().addAll( getModel().getAccounts() );
		this.accordian.getPanes();
		this.accordian.setExpandedPane( connectionTitledPane );
		consoleTextLabel.setEditable( false );
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
		boolean remember = rememberDetails.isSelected();
		
		if ( model != null )
			model.logout();

		accordian.setExpandedPane( fileViewerTitledPane );
		wTc( "connecting" );
		connDetails = new HashMap<HashKeys, String>();
		connDetails.put( HashKeys.HOST, hostDetailsField.getText() );
		connDetails.put( HashKeys.USERNAME, usernameField.getText() );
		connDetails.put( HashKeys.PASSWORD, passwordField.getText() );
		currentPath.add( directoryField.getText() );
		connDetails.put( HashKeys.PATH, directoryField.getText() );
		wTc( String.format( "Connecting to: %nHost: %s,%nas User: %s", connDetails.get( HashKeys.HOST ), connDetails.get( HashKeys.USERNAME ) ) );
		wTc( model.connect( connDetails ) );
		files = model.getFileList();
		wTc( "populating file viewer" );
		writeFilesToViewer();
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
		
	}

	@FXML
	public void handleDownload( ActionEvent event )
	{
		
	}
	
	@FXML
	public void handleUpload( ActionEvent event )
	{
		
	}
	
	@FXML
	public void handleEdit( ActionEvent event )
	{
		
	}

	@FXML
	public void handleTreeView( ActionEvent event )
	{
		System.out.println( "tree event" );
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
	
	private void writeFilesToViewer()
	{
		TreeItem<String> root = new TreeItem<String>( "Root" ); 
		for ( FileDetails f : files )
		{
			if ( f.getType().equals( "directory" ) && !f.getName().startsWith( "." ) )
			{
				TreeItem<String> item = new TreeItem<String>( f.getName() );
				root.getChildren().add( item );
			}
		}
		tableViewList = FXCollections.observableArrayList( files );
		tableView.setItems( tableViewList );
		root.setExpanded( true );
		treeView.setRoot( root );
	}



}
