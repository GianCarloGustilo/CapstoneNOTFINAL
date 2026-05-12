package com.example.capstone.model;

/**
 * Category inherits from BaseEntity.
 * This shows INHERITANCE.
 */
public class Category extends BaseEntity {

    private String name;
    private String description;

    public Category() {
    }

    public Category(int categoryId, String name, String description) {
        this.id = categoryId;
        this.name = name;
        this.description = description;
    }

    public int getCategoryId() {
        return id;
    }

    public void setCategoryId(int categoryId) {
        this.id = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDisplayLabel() {
        return name;
    }
}
