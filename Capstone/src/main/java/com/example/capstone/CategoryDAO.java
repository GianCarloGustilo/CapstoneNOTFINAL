package com.example.capstone.database;

import com.example.capstone.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO implements GenericDAO<Category> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public boolean add(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error adding category: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, category.getCategoryId());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating category: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";

        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Category category = new Category();
                category.setCategoryId(resultSet.getInt("category_id"));
                category.setName(resultSet.getString("name"));
                category.setDescription(resultSet.getString("description"));
                categories.add(category);
            }
        } catch (Exception e) {
            System.out.println("Error loading categories: " + e.getMessage());
        }

        return categories;
    }

    @Override
    public Category getById(int id) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Category category = new Category();
                category.setCategoryId(resultSet.getInt("category_id"));
                category.setName(resultSet.getString("name"));
                category.setDescription(resultSet.getString("description"));
                return category;
            }
        } catch (Exception e) {
            System.out.println("Error finding category: " + e.getMessage());
        }

        return null;
    }
}
