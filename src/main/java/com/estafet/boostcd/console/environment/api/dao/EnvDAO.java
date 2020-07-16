package com.estafet.boostcd.console.environment.api.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.estafet.boostcd.console.environment.api.model.Env;

import org.springframework.stereotype.Repository;

@Repository
public class EnvDAO {

	@PersistenceContext
	private EntityManager entityManager;
		
	@SuppressWarnings("unchecked")
	public List<Env> getEnvs() {
		return entityManager.createQuery("Select e from Env e").getResultList();
	}
	
	public Env getEnv(String envId) {
		return entityManager.find(Env.class, envId);
	}

	public Env updateEnv(Env env) {
		entityManager.merge(env);
		return env;
	}

	public Env createEnv(Env env) {
		entityManager.persist(env);
		return env;
	}
	
}
