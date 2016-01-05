package application.database.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;

import application.model.data.DirFile;

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
		if ( filter < 1 )
		{
			return findRoot();
		}
		return entityManager.createNamedQuery( "DirFile.findById", DirFile.class )
				.setParameter( "filter", filter )
				.getResultList();		
	}

	public List<DirFile> findByParent( int filter )
	{
		if ( filter < 1 )
		{
			return findRoot();
		}
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
					{ "index.html", 0 }, { "people", 0 }, { "about", 0 }, { "friends", 0 }, { "data", 0 }, 
					{ "index.html", 1 }, { "index.php", 2 }, { "home.php", 3 }, { "index.html", 4 }, { "people.html", 5 }, 
					{ "people.php", 1 }, { "service.php", 2 }, { "filea.css", 3 }, { "fileb.js", 4 }, { "filec.html", 5 } };
			for ( Object[] file : files )
			{
				DirFile df = new DirFile();
				df.setName( (String) file[0] );
				df.setParent( (Integer) file[1] );
				saveOrPersist( df );
			}
		}
	}
	
	public void resetTestData()
	{
		if ( !findAll().isEmpty() )
			entityManager.createQuery( "DELETE FROM DirFile f WHERE f.id > 0" ).executeUpdate();
		ensureTestData();
	}

}
