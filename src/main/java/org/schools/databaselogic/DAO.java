package org.schools.databaselogic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.schools.Utilities.isEmptyOrNull;

@Slf4j
public abstract class DAO<T> {

    private EntityManagerFactory entityManagerFactory;
    private Class<T> entityClass;

    protected DAO(EntityManagerFactory entityManagerFactory, Class<T> entityClass) {
        this.entityManagerFactory = entityManagerFactory;
        this.entityClass = entityClass;
        throw new IllegalStateException("Utility class");
    }

    private EntityManager getEntityManager(){
        return entityManagerFactory.createEntityManager();
    }

    public void create(T object){
        EntityManager entityManager = null;
        try{
            entityManager = getEntityManager();
            EntityTransaction entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            entityManager.persist(object);
            entityTransaction.commit();
        }
        finally {
            if (entityManager != null){
                entityManager.close();
            }
        }

    }


    public void update(T object) throws NonexistentEntityException {
        EntityManager entityManager = null;
        try{
            entityManager = getEntityManager();
            EntityTransaction entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            entityManager.merge(object);
            entityTransaction.commit();
        }
        catch (Exception ex){
            String msg = ex.getLocalizedMessage();
            if (isEmptyOrNull(msg)){
                int id = 0;
                try {
                    id = getObjectId(object);
                } catch (NoSuchMethodException|InvocationTargetException|IllegalAccessException e) {
                    log.error(
                            "Ocurrió un error intentando acceder a método getId del objeto de la clase {}",
                            entityClass
                    );
                    throw ex;
                }
                if (exits(id)){
                    throw new NonexistentEntityException("El identificador " + id + " no existe");
                }
            }
            throw ex;
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }

    }

    public boolean exits(int id){
        T ob = findById(id);
        return isEmptyOrNull(ob);
    }


    public T findById(int id) {
        EntityManager entityManager = null;
        entityManager = getEntityManager();
        return entityManager.find(entityClass, id);
    }


    public void delete(T entity) {
        EntityManager entityManager = null;
        try{
            entityManager = getEntityManager();
            EntityTransaction entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
            entityTransaction.commit();
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }


    public List<T> getAll(){
        return getAll(true, -1, -1);
    }


    public List<T> getAll(int maxResults, int firstResult){
        return getAll(false, maxResults, firstResult);
    }


    public List<T> getAll(boolean all, int maxResults, int firstResult){
        EntityManager entityManager = null;
        try{
            entityManager = getEntityManager();
            CriteriaQuery<T> cq = (CriteriaQuery<T>) entityManager.getCriteriaBuilder().createQuery();
            cq.select(cq.from(entityClass));
            Query q = entityManager.createQuery(cq);
            if (!all){
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }


    private int getObjectId(T object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> classObject = object.getClass();
        int id = -1;
        Method method = classObject.getMethod("getId");
        Object ob = method.invoke(object);
        if (!isEmptyOrNull(ob)) {
            id = (int) method.invoke(object);
        }
        return id;
    }
}
