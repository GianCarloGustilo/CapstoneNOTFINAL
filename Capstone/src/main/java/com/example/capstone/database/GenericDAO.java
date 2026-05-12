package com.example.capstone.database;

import java.util.List;

/**
 * Simple DAO interface.
 * This shows ABSTRACTION because all DAO classes follow one common set of methods.
 * It also shows POLYMORPHISM because each DAO class has its own version of these methods.
 */
public interface GenericDAO<T> {

    boolean add(T item);
    boolean update(T item);
    boolean delete(int id);
    List<T> getAll();
    T getById(int id);
}
