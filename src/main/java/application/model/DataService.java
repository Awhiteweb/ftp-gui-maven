package application.model;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;

import application.model.data.DirFile;

@Stateless
public class DataService
{

	@PersistenceContext( unitName = "directory-map" )
	private EntityManager entityManager;

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

}
