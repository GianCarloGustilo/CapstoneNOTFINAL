package com.example.capstone.database;

import com.example.capstone.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO implements GenericDAO<Supplier> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public boolean add(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact_name, phone, email, address) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, supplier.getName());
            statement.setString(2, supplier.getContactName());
            statement.setString(3, supplier.getPhone());
            statement.setString(4, supplier.getEmail());
            statement.setString(5, supplier.getAddress());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error adding supplier: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact_name = ?, phone = ?, email = ?, address = ? WHERE supplier_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, supplier.getName());
            statement.setString(2, supplier.getContactName());
            statement.setString(3, supplier.getPhone());
            statement.setString(4, supplier.getEmail());
            statement.setString(5, supplier.getAddress());
            statement.setInt(6, supplier.getSupplierId());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating supplier: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting supplier: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Supplier> getAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";

        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Supplier supplier = new Supplier();
                supplier.setSupplierId(resultSet.getInt("supplier_id"));
                supplier.setName(resultSet.getString("name"));
                supplier.setContactName(resultSet.getString("contact_name"));
                supplier.setPhone(resultSet.getString("phone"));
                supplier.setEmail(resultSet.getString("email"));
                supplier.setAddress(resultSet.getString("address"));
                suppliers.add(supplier);
            }
        } catch (Exception e) {
            System.out.println("Error loading suppliers: " + e.getMessage());
        }

        return suppliers;
    }

    @Override
    public Supplier getById(int id) {
        String sql = "SELECT * FROM suppliers WHERE supplier_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Supplier supplier = new Supplier();
                supplier.setSupplierId(resultSet.getInt("supplier_id"));
                supplier.setName(resultSet.getString("name"));
                supplier.setContactName(resultSet.getString("contact_name"));
                supplier.setPhone(resultSet.getString("phone"));
                supplier.setEmail(resultSet.getString("email"));
                supplier.setAddress(resultSet.getString("address"));
                return supplier;
            }
        } catch (Exception e) {
            System.out.println("Error finding supplier: " + e.getMessage());
        }

        return null;
    }
}
