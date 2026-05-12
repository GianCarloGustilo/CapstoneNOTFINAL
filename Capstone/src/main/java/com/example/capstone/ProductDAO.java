package com.example.capstone.database;

import com.example.capstone.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements GenericDAO<Product> {

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public boolean add(Product product) {
        String sql = "INSERT INTO products (category_id, supplier_id, name, description, quantity, price, low_stock_threshold) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            setForeignKeys(statement, product);
            statement.setString(3, product.getName());
            statement.setString(4, product.getDescription());
            statement.setInt(5, product.getQuantity());
            statement.setDouble(6, product.getPrice());
            statement.setInt(7, product.getLowStockThreshold());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET category_id = ?, supplier_id = ?, name = ?, description = ?, quantity = ?, price = ?, low_stock_threshold = ? "
                + "WHERE product_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            setForeignKeys(statement, product);
            statement.setString(3, product.getName());
            statement.setString(4, product.getDescription());
            statement.setInt(5, product.getQuantity());
            statement.setDouble(6, product.getPrice());
            statement.setInt(7, product.getLowStockThreshold());
            statement.setInt(8, product.getProductId());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating product: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();
        String sql = getBaseSelect() + " ORDER BY p.name";

        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                products.add(readProduct(resultSet));
            }
        } catch (Exception e) {
            System.out.println("Error loading products: " + e.getMessage());
        }

        return products;
    }

    @Override
    public Product getById(int id) {
        String sql = getBaseSelect() + " WHERE p.product_id = ?";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return readProduct(resultSet);
            }
        } catch (Exception e) {
            System.out.println("Error finding product: " + e.getMessage());
        }

        return null;
    }

    public List<Product> search(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = getBaseSelect() + " WHERE p.name LIKE ? OR p.description LIKE ? ORDER BY p.name";

        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            String searchValue = "%" + keyword + "%";
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                products.add(readProduct(resultSet));
            }
        } catch (Exception e) {
            System.out.println("Error searching products: " + e.getMessage());
        }

        return products;
    }

    public List<Product> getLowStock() {
        List<Product> products = new ArrayList<>();
        String sql = getBaseSelect() + " WHERE p.quantity < p.low_stock_threshold ORDER BY p.quantity";

        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                products.add(readProduct(resultSet));
            }
        } catch (Exception e) {
            System.out.println("Error loading low stock products: " + e.getMessage());
        }

        return products;
    }

    public synchronized boolean adjustStock(int productId, int changeAmount, String note, Integer userId) {
        Product product = getById(productId);

        if (product == null) {
            return false;
        }

        int oldQuantity = product.getQuantity();
        int newQuantity = oldQuantity + changeAmount;

        if (newQuantity < 0) {
            return false;
        }

        String updateSql = "UPDATE products SET quantity = ? WHERE product_id = ?";
        String logSql = "INSERT INTO inventory_logs (product_id, user_id, change_type, quantity_before, quantity_change, quantity_after, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement updateStatement = getConnection().prepareStatement(updateSql);
            updateStatement.setInt(1, newQuantity);
            updateStatement.setInt(2, productId);
            updateStatement.executeUpdate();

            PreparedStatement logStatement = getConnection().prepareStatement(logSql);
            logStatement.setInt(1, productId);

            if (userId == null) {
                logStatement.setNull(2, Types.INTEGER);
            } else {
                logStatement.setInt(2, userId);
            }

            if (changeAmount >= 0) {
                logStatement.setString(3, "add");
            } else {
                logStatement.setString(3, "remove");
            }

            logStatement.setInt(4, oldQuantity);
            logStatement.setInt(5, changeAmount);
            logStatement.setInt(6, newQuantity);
            logStatement.setString(7, note);
            logStatement.executeUpdate();

            return true;
        } catch (Exception e) {
            System.out.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    public int[] getSummary() {
        int[] summary = new int[2];
        String sql = "SELECT COUNT(*) AS total_skus, SUM(quantity < low_stock_threshold) AS low_stock_count FROM products";

        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                summary[0] = resultSet.getInt("total_skus");
                summary[1] = resultSet.getInt("low_stock_count");
            }
        } catch (Exception e) {
            System.out.println("Error loading summary: " + e.getMessage());
        }

        return summary;
    }

    public double getTotalValue() {
        String sql = "SELECT SUM(quantity * price) AS total_value FROM products";

        try {
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                return resultSet.getDouble("total_value");
            }
        } catch (Exception e) {
            System.out.println("Error computing total value: " + e.getMessage());
        }

        return 0;
    }

    private String getBaseSelect() {
        return "SELECT p.*, "
                + "c.name AS category_name, "
                + "s.name AS supplier_name "
                + "FROM products p "
                + "LEFT JOIN categories c ON p.category_id = c.category_id "
                + "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id";
    }

    private Product readProduct(ResultSet resultSet) throws Exception {
        Product product = new Product();
        product.setProductId(resultSet.getInt("product_id"));
        product.setCategoryId(resultSet.getInt("category_id"));
        product.setSupplierId(resultSet.getInt("supplier_id"));
        product.setName(resultSet.getString("name"));
        product.setDescription(resultSet.getString("description"));
        product.setQuantity(resultSet.getInt("quantity"));
        product.setPrice(resultSet.getDouble("price"));
        product.setLowStockThreshold(resultSet.getInt("low_stock_threshold"));
        product.setCategoryName(resultSet.getString("category_name"));
        product.setSupplierName(resultSet.getString("supplier_name"));
        return product;
    }

    private void setForeignKeys(PreparedStatement statement, Product product) throws Exception {
        if (product.getCategoryId() > 0) {
            statement.setInt(1, product.getCategoryId());
        } else {
            statement.setNull(1, Types.INTEGER);
        }

        if (product.getSupplierId() > 0) {
            statement.setInt(2, product.getSupplierId());
        } else {
            statement.setNull(2, Types.INTEGER);
        }
    }
}
