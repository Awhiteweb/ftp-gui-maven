package application.database.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import application.model.data.DirFile;
import application.model.data.DirFileType;

public class DataService
{

	private EntityManager entityManager;
	
	public DataService()
	{
		entityManager = Persistence.createEntityManagerFactory( "directory-map" )
				.createEntityManager();
		entityManager.getTransaction().begin();
	}

	public void saveOrPersist( DirFile entity )
	{
		if ( entity.getId() > 0 )
		{
			entityManager.merge( entity );
		}
		else
		{
			entityManager.persist( entity );
		}
	}

	public void deleteEntity( DirFile entity )
	{
		if ( entity.getId() > 0 )
		{
			entity = entityManager.merge( entity );
			entityManager.remove( entity );
		}
	}

	public List<DirFile> findAll()
	{
		CriteriaQuery<DirFile> cq = entityManager.getCriteriaBuilder()
				.createQuery( DirFile.class );
		cq.select( cq.from( DirFile.class ) );
		return entityManager.createQuery( cq ).getResultList();
	}

	public List<DirFile> findByName( String filter )
	{
		if ( filter == null || filter.isEmpty() )
		{
			return findAll();
		}
		filter = filter.toLowerCase();
		return entityManager.createNamedQuery( "DirFile.findByName", DirFile.class )
				.setParameter( "filter", filter + "%" ).getResultList();
	}
	
	public List<DirFile> findById( int filter )
	{
		return entityManager.createNamedQuery( "DirFile.findById", DirFile.class )
				.setParameter( "filter", filter )
				.getResultList();		
	}
	
	public DirFile findByNameAndParent( String name, int parent )
	{
		return catchResult( entityManager.createNamedQuery( "DirFile.findByNameAndParent", DirFile.class )
				.setParameter( "name", name )
				.setParameter( "parent", parent ) );
	}

	public List<DirFile> findByParent( int filter )
	{
		return entityManager.createNamedQuery( "DirFile.findByParent", DirFile.class )
				.setParameter( "filter", filter )
				.getResultList();
	}
	
	public List<DirFile> findRoot()
	{
		return entityManager.createNamedQuery( "DirFile.findRoot", DirFile.class )
				.getResultList();
	}
	
	public void ensureTestData()
	{
		if ( findAll().isEmpty() )
		{
			final Object[][] files = { 
					{ "index.html", 0, DirFileType.FILE }, { "people", 0, DirFileType.FOLD }, 
					{ "about", 0, DirFileType.FOLD }, { "friends", 0, DirFileType.FOLD }, 
					{ "data", 0, DirFileType.FOLD }, { "index.html", 1, DirFileType.FILE }, 
					{ "index.php", 2, DirFileType.FILE }, { "home.php", 3, DirFileType.FILE }, 
					{ "index.html", 4, DirFileType.FILE }, { "people.html", 5, DirFileType.FILE }, 
					{ "people.php", 1, DirFileType.FILE }, { "service.php", 2, DirFileType.FILE }, 
					{ "filea.css", 3, DirFileType.FILE }, { "fileb.js", 4, DirFileType.FILE }, 
					{ "filec.html", 5, DirFileType.FILE } };
			for ( Object[] file : files )
			{
				DirFile df = new DirFile();
				df.setName( (String) file[0] );
				df.setParent( (Integer) file[1] );
				df.setType( (DirFileType) file[2] );
				saveOrPersist( df );
			}
		}
	}
	
	private DirFile catchResult( TypedQuery<DirFile> query )
	{
		try
		{
			return query.getSingleResult();
		}
		catch( NoResultException | NonUniqueResultException e )
		{
			return null;
		}
	}
	
	public void resetTestData()
	{
		if ( !findAll().isEmpty() )
			entityManager.createQuery( "DELETE FROM DirFile f WHERE f.id > 0" ).executeUpdate();
		ensureTestData();
	}

}
