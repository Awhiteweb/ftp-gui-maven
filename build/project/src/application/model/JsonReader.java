package application.model;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonReader
{
	private File data = new File( "resources/accounts.json" );
	
	public Accounts getData()
	{
		ObjectMapper om = new ObjectMapper();
		try
		{
			return om.readValue( data, Accounts.class );
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
