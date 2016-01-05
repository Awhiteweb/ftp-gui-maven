package application.database.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;

import application.model.data.Account;

public class AccountService
{

	private EntityManager entityManager;
	
	public AccountService()
	{
		entityManager = Persistence.createEntityManagerFactory( "accounts" )
				.createEntityManager();
		entityManager.getTransaction().begin();
	}

	public void saveOrPersist( Account entity )
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

	public void deleteEntity( Account entity )
	{
		if ( entity.getId() > 0 )
		{
			entity = entityManager.merge( entity );
			entityManager.remove( entity );
		}
	}

	public List<Account> findAll()
	{
		CriteriaQuery<Account> cq = entityManager.getCriteriaBuilder()
				.createQuery( Account.class );
		cq.select( cq.from( Account.class ) );
		return entityManager.createQuery( cq ).getResultList();
	}

	public Account findByName( String filter )
	{
		if ( filter == null || filter.isEmpty() )
		{
			return findAll().get( 0 );
		}
		filter = filter.toLowerCase();
		return entityManager.createNamedQuery( "Account.findByName", Account.class )
				.setParameter( "filter", filter + "%" ).getSingleResult();
	}
	
	public Account findById( int filter )
	{
		if ( filter < 1 )
		{
			return findAll().get( 0 );
		}
		return entityManager.createNamedQuery( "Account.findById", Account.class )
				.setParameter( "filter", filter )
				.getSingleResult();		
	}

}
