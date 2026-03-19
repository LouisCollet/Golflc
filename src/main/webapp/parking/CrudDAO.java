package dao;

import java.util.List;

/**
 * Interface CRUD générique pour toute entité
 */
public interface CrudDAO<T> {
    boolean create(T entity) throws Exception;
    T read(T entity) throws Exception;
    boolean update(T entity) throws Exception;
    boolean delete(T entity) throws Exception;
    List<T> findAll() throws Exception;
}