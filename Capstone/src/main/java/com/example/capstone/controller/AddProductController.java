package com.example.capstone.controller;

import com.example.capstone.model.Category;
import com.example.capstone.model.Product;
import com.example.capstone.model.Supplier;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class AddProductController {

    @FXML private TextField nameField;
    @FXML private TextField descField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private TextField thresholdField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private ComboBox<Supplier> supplierCombo;

    private boolean saved;
    private Product product;

    public void setCategories(List<Category> categories) {
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
    }

    public void setSuppliers(List<Supplier> suppliers) {
        supplierCombo.setItems(FXCollections.observableArrayList(suppliers));
    }

    public void prefill(Product product) {
        nameField.setText(product.getName());
        descField.setText(product.getDescription());
        priceField.setText(String.valueOf(product.getPrice()));
        quantityField.setText(String.valueOf(product.getQuantity()));
        thresholdField.setText(String.valueOf(product.getLowStockThreshold()));

        selectCategory(product.getCategoryId());
        selectSupplier(product.getSupplierId());
    }

    @FXML
    public void onSave() {
        if (!hasRequiredFields()) {
            showMessage("Name, price, and quantity are required.");
            return;
        }

        try {
            Product newProduct = new Product();
            newProduct.setName(nameField.getText().trim());
            newProduct.setDescription(descField.getText().trim());
            newProduct.setPrice(Double.parseDouble(priceField.getText().trim()));
            newProduct.setQuantity(Integer.parseInt(quantityField.getText().trim()));

            if (thresholdField.getText().trim().isEmpty()) {
                newProduct.setLowStockThreshold(10);
            } else {
                newProduct.setLowStockThreshold(Integer.parseInt(thresholdField.getText().trim()));
            }

            Category category = categoryCombo.getValue();
            Supplier supplier = supplierCombo.getValue();

            if (category != null) {
                newProduct.setCategoryId(category.getCategoryId());
            }

            if (supplier != null) {
                newProduct.setSupplierId(supplier.getSupplierId());
            }

            product = newProduct;
            saved = true;
            closeWindow();
        } catch (NumberFormatException e) {
            showMessage("Price, quantity, and low stock value must be valid numbers.");
        }
    }

    @FXML
    public void onCancel() {
        closeWindow();
    }

    public boolean isSaved() {
        return saved;
    }

    public Product getProduct() {
        return product;
    }

    private boolean hasRequiredFields() {
        return !nameField.getText().trim().isEmpty()
                && !priceField.getText().trim().isEmpty()
                && !quantityField.getText().trim().isEmpty();
    }

    private void selectCategory(int categoryId) {
        for (Category category : categoryCombo.getItems()) {
            if (category.getCategoryId() == categoryId) {
                categoryCombo.setValue(category);
                break;
            }
        }
    }

    private void selectSupplier(int supplierId) {
        for (Supplier supplier : supplierCombo.getItems()) {
            if (supplier.getSupplierId() == supplierId) {
                supplierCombo.setValue(supplier);
                break;
            }
        }
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
