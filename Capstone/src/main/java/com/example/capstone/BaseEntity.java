package com.example.capstone.model;

import java.time.LocalDateTime;

/**
 * Base class for all entities.
 * This shows ABSTRACTION because common fields are placed here.
 * Child classes inherit from this class.
 */
public abstract class BaseEntity {

    protected int id;
    protected LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public abstract String getDisplayLabel();

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
