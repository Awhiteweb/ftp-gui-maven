package application.controller;

import java.net.URL;
import java.util.ResourceBundle;

import application.model.Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class FTPController implements Initializable
{
	@FXML private ComboBox<String> selectAccountBox;
	@FXML private TextField hostDetailsField;
	@FXML private TextField usernameField;
	@FXML private TextField passwordField;
	@FXML private CheckBox rememberDetails;
	@FXML private TableColumn<Integer, String> fileNameCol;
	@FXML private TableColumn<Integer, String> fileSizeCol;
	@FXML private TableColumn<Integer, String> fileTypeCol;
	
	@FXML 
	public void handleLoginButton(ActionEvent event) 
	{
		System.out.printf( "Host: %s,%nUser: %s,%nPassword: %s", hostDetailsField.getText(), usernameField.getText(), passwordField.getText() );
	}

	@Override
	public void initialize( URL arg0, ResourceBundle arg1 )
	{
		Model m = new Model();
		this.selectAccountBox.getItems().addAll( m.getAccounts() );
	}

	@FXML 
	public void handleComboBoxSelect(ActionEvent event) 
	{
		Model m = new Model();
		String[] details = m.getLoginDetails( selectAccountBox.getSelectionModel().getSelectedItem() );
		usernameField.setText( details[0] );
		passwordField.setText( details[1] );
	}
}
