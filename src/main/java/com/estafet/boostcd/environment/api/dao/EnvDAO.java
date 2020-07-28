package com.estafet.boostcd.environment.api.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.estafet.boostcd.environment.api.model.Env;

@Repository
public class EnvDAO {

	@PersistenceContext
	private EntityManager entityManager;
		
	@SuppressWarnings("unchecked")
	public List<Env> getEnvs() {
		return entityManager.createQuery("Select e from Env e").getResultList();
	}
	
	public Env getEnv(String productId, String envId) {
		TypedQuery<Env> query = entityManager.createQuery("Select e from Env e where e.id = :id and e.productId = :productId", Env.class);
		List<Env> envs = query.setParameter("id", envId).setParameter("productId", productId).getResultList();
		return !envs.isEmpty() ? envs.get(0) : null;
	}

	public Env update(Env env) {
		entityManager.merge(env);
		return env;
	}

	public Env create(Env env) {
		entityManager.persist(env);
		return env;
	}
	
}
