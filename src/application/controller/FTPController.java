package application.controller;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.shape.Ellipse;
import application.model.Account;
import application.model.FileDetails;
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
	@FXML private TableColumn<Integer, String> fileNameCol;
	@FXML private TableColumn<Integer, String> fileSizeCol;
	@FXML private TableColumn<Integer, String> fileTypeCol;
	@FXML private ScrollPane consoleScrollPane;
	@FXML private Ellipse progressEllipse;
	@FXML private TitledPane connectionTitledPane;
	@FXML private TitledPane fileViewerTitledPane;
	@FXML private TitledPane consoleTitledPane;
	@FXML private TextArea consoleTextLabel;
	private Model model;
	private List<FileDetails> files;
	private HashMap<String, String> connDetails;
	
	@Override
	public void initialize( URL arg0, ResourceBundle arg1 )
	{
		this.selectAccountBox.getItems().addAll( getModel().getAccounts() );
		this.accordian.getPanes();
		this.accordian.setExpandedPane( connectionTitledPane );
		consoleTextLabel.setEditable( false );
	}

	@FXML 
	public void handleLoginButton(ActionEvent event) 
	{
		accordian.setExpandedPane( fileViewerTitledPane );
		wTc( "connecting" );
		connDetails = new HashMap<String, String>();
		connDetails.put( "host", hostDetailsField.getText() );
		connDetails.put( "username", usernameField.getText() );
		connDetails.put( "password", passwordField.getText() );
		connDetails.put( "directory", directoryField.getText() );
		wTc( String.format( "Connecting to: %nHost: %s,%nUser: %s", connDetails.get( "host" ), connDetails.get( "username" ) ) );
		wTc( model.connect( connDetails ) );
		files = model.getFileList();
		accordian.setExpandedPane( fileViewerTitledPane );
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

	@FXML 
	public void handleComboBoxSelect(ActionEvent event) 
	{
		Account account = getModel().getLoginDetails( selectAccountBox.getSelectionModel().getSelectedItem() );
		hostDetailsField.setText( selectAccountBox.getSelectionModel().getSelectedItem() );
		usernameField.setText( account.getUsername() );
		passwordField.setText( account.getPassword() );
		directoryField.setText( account.getDirectory() );
	}
	
	public Model getModel()
	{
		if ( model == null )
			model = new Model();
		return model;
	}

	@FXML 
	public void handleClose(ActionEvent event) 
	{
		model.logout();
		Platform.exit();
	}
}
