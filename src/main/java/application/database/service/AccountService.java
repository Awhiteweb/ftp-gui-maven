package application.database.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
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
		return catchResult(  entityManager.createNamedQuery( "Account.findByName", Account.class )
				.setParameter( "filter", filter + "%" ) );
	}
	
	public Account findById( int filter )
	{
		if ( filter < 1 )
		{
			return findAll().get( 0 );
		}
		return catchResult( entityManager.createNamedQuery( "Account.findById", Account.class )
				.setParameter( "filter", filter ) );		
	}

	private Account catchResult( TypedQuery<Account> query )
	{
		try
		{
			return query.getSingleResult();
		}
		catch( NoResultException | NonUniqueResultException e )
		{
			return new Account();
		}
	}
	
	public void resetData()
	{
		String[][] data = {{ "ftp.whiteslife.com", "whitesli", "blue\\Ch1lcroft", "/public_html" },
				{ "php.dev.ovalbusinesssolutions.co.uk", "phpdevovalbusinesssolutionscouk", "I23acXipwFkP", "httpdocs" }};
		for ( String[] s : data )
			saveOrPersist( new Account( s[0], s[1], s[2], s[3] ) );
	}
}
