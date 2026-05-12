package com.example.capstone.controller;

import com.example.capstone.database.CategoryDAO;
import com.example.capstone.database.ProductDAO;
import com.example.capstone.database.SupplierDAO;
import com.example.capstone.model.Category;
import com.example.capstone.model.Product;
import com.example.capstone.model.Supplier;
import com.example.capstone.util.AsyncLoader;
import com.example.capstone.util.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private Label statSkuLabel;
    @FXML private Label statValueLabel;
    @FXML private Label statLowLabel;
    @FXML private Label selectedStockLabel;
    @FXML private TextField searchField;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> supplierColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> qtyColumn;

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Integer> catIdColumn;
    @FXML private TableColumn<Category, String> catNameColumn;
    @FXML private TableColumn<Category, String> catDescColumn;

    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Integer> supIdColumn;
    @FXML private TableColumn<Supplier, String> supNameColumn;
    @FXML private TableColumn<Supplier, String> supContactColumn;
    @FXML private TableColumn<Supplier, String> supPhoneColumn;
    @FXML private TableColumn<Supplier, String> supEmailColumn;

    @FXML private VBox productsPane;
    @FXML private VBox categoriesPane;
    @FXML private VBox suppliersPane;

    @FXML private Button navProducts;
    @FXML private Button navCategories;
    @FXML private Button navSuppliers;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    private static final String ACTIVE_STYLE =
            "-fx-background-color: rgba(110,231,183,0.08); "
                    + "-fx-background-radius: 8; -fx-text-fill: #6EE7B7; "
                    + "-fx-font-family: 'Outfit'; -fx-font-size: 13; "
                    + "-fx-alignment: CENTER_LEFT; -fx-padding: 9 10; -fx-border-width: 0;";

    private static final String INACTIVE_STYLE =
            "-fx-background-color: transparent; "
                    + "-fx-background-radius: 8; -fx-text-fill: #6B7280; "
                    + "-fx-font-family: 'Outfit'; -fx-font-size: 13; "
                    + "-fx-alignment: CENTER_LEFT; -fx-padding: 9 10; -fx-border-width: 0;";

    @FXML
    public void initialize() {
        setupProductTable();
        setupCategoryTable();
        setupSupplierTable();
        setupSelection();
        setupSearch();
        showProducts();
        loadDataInBackground();
    }

    private void setupProductTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        productTable.setRowFactory(table -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);

                if (empty || product == null) {
                    setStyle("");
                } else if (product.isLowStock()) {
                    setStyle("-fx-background-color: rgba(239,159,39,0.08);");
                } else {
                    setStyle("");
                }
            }
        });

        productTable.setItems(productList);
    }

    private void setupCategoryTable() {
        catIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        catNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        catDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryTable.setItems(categoryList);
    }

    private void setupSupplierTable() {
        supIdColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        supContactColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        supPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        supEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        supplierTable.setItems(supplierList);
    }

    private void setupSelection() {
        productTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateSelectedStockLabel(newValue);
        });

        updateSelectedStockLabel(null);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String keyword = newValue.trim();

            if (keyword.isEmpty()) {
                productList.setAll(productDAO.getAll());
            } else {
                productList.setAll(productDAO.search(keyword));
            }

            refreshStatCards();
        });
    }

    private void loadDataInBackground() {
        AsyncLoader.run(
                new AsyncLoader.DataTask<DashboardData>() {
                    @Override
                    public DashboardData run() {
                        DashboardData data = new DashboardData();
                        data.products = productDAO.getAll();
                        data.categories = categoryDAO.getAll();
                        data.suppliers = supplierDAO.getAll();
                        data.summary = productDAO.getSummary();
                        data.totalValue = productDAO.getTotalValue();
                        return data;
                    }
                },
                new AsyncLoader.DataHandler<DashboardData>() {
                    @Override
                    public void handle(DashboardData data) {
                        productList.setAll(data.products);
                        categoryList.setAll(data.categories);
                        supplierList.setAll(data.suppliers);
                        updateStatCards(data.summary, data.totalValue);
                    }
                },
                new AsyncLoader.ErrorHandler() {
                    @Override
                    public void handle(Exception e) {
                        showAlert("Could not load data.");
                    }
                }
        );
    }

    private void reloadAllData() {
        productList.setAll(productDAO.getAll());
        categoryList.setAll(categoryDAO.getAll());
        supplierList.setAll(supplierDAO.getAll());
        refreshStatCards();
    }

    private void refreshStatCards() {
        int[] summary = productDAO.getSummary();
        double totalValue = productDAO.getTotalValue();
        updateStatCards(summary, totalValue);
    }

    private void updateStatCards(int[] summary, double totalValue) {
        statSkuLabel.setText(String.valueOf(summary[0]));
        statValueLabel.setText(String.format("P %,.2f", totalValue));
        statLowLabel.setText(String.valueOf(summary[1]));
    }

    @FXML
    public void showProducts() {
        productsPane.setVisible(true);
        productsPane.setManaged(true);
        categoriesPane.setVisible(false);
        categoriesPane.setManaged(false);
        suppliersPane.setVisible(false);
        suppliersPane.setManaged(false);

        navProducts.setStyle(ACTIVE_STYLE);
        navCategories.setStyle(INACTIVE_STYLE);
        navSuppliers.setStyle(INACTIVE_STYLE);
    }

    @FXML
    public void showCategories() {
        productsPane.setVisible(false);
        productsPane.setManaged(false);
        categoriesPane.setVisible(true);
        categoriesPane.setManaged(true);
        suppliersPane.setVisible(false);
        suppliersPane.setManaged(false);

        navProducts.setStyle(INACTIVE_STYLE);
        navCategories.setStyle(ACTIVE_STYLE);
        navSuppliers.setStyle(INACTIVE_STYLE);

        categoryList.setAll(categoryDAO.getAll());
    }

    @FXML
    public void showSuppliers() {
        productsPane.setVisible(false);
        productsPane.setManaged(false);
        categoriesPane.setVisible(false);
        categoriesPane.setManaged(false);
        suppliersPane.setVisible(true);
        suppliersPane.setManaged(true);

        navProducts.setStyle(INACTIVE_STYLE);
        navCategories.setStyle(INACTIVE_STYLE);
        navSuppliers.setStyle(ACTIVE_STYLE);

        supplierList.setAll(supplierDAO.getAll());
    }

    @FXML
    public void onAddProduct() {
        openProductDialog(null);
    }

    @FXML
    public void onEditProduct() {
        Product product = productTable.getSelectionModel().getSelectedItem();

        if (product == null) {
            showAlert("Select a product to edit.");
            return;
        }

        openProductDialog(product);
    }

    @FXML
    public void onDeleteProduct() {
        Product product = productTable.getSelectionModel().getSelectedItem();

        if (product == null) {
            showAlert("Select a product to delete.");
            return;
        }

        if (showConfirm("Delete " + product.getName() + "?")) {
            productDAO.delete(product.getProductId());
            reloadAllData();
        }
    }

    @FXML
    public void onIncreaseStock() {
        changeSelectedStock(1);
    }

    @FXML
    public void onDecreaseStock() {
        changeSelectedStock(-1);
    }

    @FXML
    public void onAddCategory() {
        openCategoryDialog(null);
    }

    @FXML
    public void onEditCategory() {
        Category category = categoryTable.getSelectionModel().getSelectedItem();

        if (category == null) {
            showAlert("Select a category to edit.");
            return;
        }

        openCategoryDialog(category);
    }

    @FXML
    public void onDeleteCategory() {
        Category category = categoryTable.getSelectionModel().getSelectedItem();

        if (category == null) {
            showAlert("Select a category to delete.");
            return;
        }

        if (showConfirm("Delete category " + category.getName() + "?")) {
            categoryDAO.delete(category.getCategoryId());
            categoryList.setAll(categoryDAO.getAll());
        }
    }

    @FXML
    public void onAddSupplier() {
        openSupplierDialog(null);
    }

    @FXML
    public void onEditSupplier() {
        Supplier supplier = supplierTable.getSelectionModel().getSelectedItem();

        if (supplier == null) {
            showAlert("Select a supplier to edit.");
            return;
        }

        openSupplierDialog(supplier);
    }

    @FXML
    public void onDeleteSupplier() {
        Supplier supplier = supplierTable.getSelectionModel().getSelectedItem();

        if (supplier == null) {
            showAlert("Select a supplier to delete.");
            return;
        }

        if (showConfirm("Delete supplier " + supplier.getName() + "?")) {
            supplierDAO.delete(supplier.getSupplierId());
            supplierList.setAll(supplierDAO.getAll());
        }
    }

    private void openProductDialog(Product oldProduct) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/AddProductView.fxml"));
            Parent root = loader.load();

            AddProductController controller = loader.getController();
            controller.setCategories(categoryDAO.getAll());
            controller.setSuppliers(supplierDAO.getAll());

            if (oldProduct != null) {
                controller.prefill(oldProduct);
            }

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(oldProduct == null ? "Add Product" : "Edit Product");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                Product product = controller.getProduct();

                if (oldProduct == null) {
                    productDAO.add(product);
                } else {
                    product.setProductId(oldProduct.getProductId());
                    productDAO.update(product);
                }

                reloadAllData();
            }
        } catch (IOException e) {
            showAlert("Could not open product form.");
        }
    }

    private void openCategoryDialog(Category oldCategory) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(oldCategory == null ? "Add Category" : "Edit Category");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = styledField(oldCategory == null ? "" : oldCategory.getName());
        TextField descField = styledField(oldCategory == null ? "" : oldCategory.getDescription());

        VBox box = new VBox();
        box.setSpacing(10);
        box.getChildren().addAll(
                styledLabel("Category Name"), nameField,
                styledLabel("Description"), descField
        );

        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                Category category = oldCategory == null ? new Category() : oldCategory;
                category.setName(nameField.getText().trim());
                category.setDescription(descField.getText().trim());
                return category;
            }
            return null;
        });

        dialog.showAndWait();
        Category result = dialog.getResult();

        if (result != null) {
            if (oldCategory == null) {
                categoryDAO.add(result);
            } else {
                categoryDAO.update(result);
            }

            categoryList.setAll(categoryDAO.getAll());
        }
    }

    private void openSupplierDialog(Supplier oldSupplier) {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(oldSupplier == null ? "Add Supplier" : "Edit Supplier");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = styledField(oldSupplier == null ? "" : oldSupplier.getName());
        TextField contactField = styledField(oldSupplier == null ? "" : oldSupplier.getContactName());
        TextField phoneField = styledField(oldSupplier == null ? "" : oldSupplier.getPhone());
        TextField emailField = styledField(oldSupplier == null ? "" : oldSupplier.getEmail());
        TextField addressField = styledField(oldSupplier == null ? "" : oldSupplier.getAddress());

        VBox box = new VBox();
        box.setSpacing(10);
        box.getChildren().addAll(
                styledLabel("Company Name"), nameField,
                styledLabel("Contact Person"), contactField,
                styledLabel("Phone"), phoneField,
                styledLabel("Email"), emailField,
                styledLabel("Address"), addressField
        );

        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                Supplier supplier = oldSupplier == null ? new Supplier() : oldSupplier;
                supplier.setName(nameField.getText().trim());
                supplier.setContactName(contactField.getText().trim());
                supplier.setPhone(phoneField.getText().trim());
                supplier.setEmail(emailField.getText().trim());
                supplier.setAddress(addressField.getText().trim());
                return supplier;
            }
            return null;
        });

        dialog.showAndWait();
        Supplier result = dialog.getResult();

        if (result != null) {
            if (oldSupplier == null) {
                supplierDAO.add(result);
            } else {
                supplierDAO.update(result);
            }

            supplierList.setAll(supplierDAO.getAll());
        }
    }

    private void changeSelectedStock(int changeAmount) {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert("Select a product first.");
            return;
        }

        Integer userId = null;
        if (SessionManager.getInstance().isLoggedIn()) {
            userId = SessionManager.getInstance().getCurrentUser().getUserId();
        }

        String note;
        if (changeAmount > 0) {
            note = "Stock increased from dashboard";
        } else {
            note = "Stock decreased from dashboard";
        }

        boolean success = productDAO.adjustStock(selectedProduct.getProductId(), changeAmount, note, userId);

        if (!success) {
            showAlert("Stock update failed.");
            return;
        }

        Product updatedProduct = productDAO.getById(selectedProduct.getProductId());

        if (updatedProduct == null) {
            reloadAllData();
            return;
        }

        int selectedIndex = productTable.getSelectionModel().getSelectedIndex();
        productList.set(selectedIndex, updatedProduct);
        productTable.getSelectionModel().select(selectedIndex);

        refreshStatCards();
        updateSelectedStockLabel(updatedProduct);
    }

    private void updateSelectedStockLabel(Product product) {
        if (product == null) {
            selectedStockLabel.setText("Selected Stock: --");
        } else {
            selectedStockLabel.setText("Selected Stock: " + product.getQuantity());
        }
    }

    private boolean showConfirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private TextField styledField(String value) {
        TextField textField = new TextField(value);
        textField.setStyle("-fx-background-color: #1A1C1F; -fx-text-fill: #F5F4F0; "
                + "-fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 8; "
                + "-fx-background-radius: 8; -fx-padding: 8 12;");
        return textField;
    }

    private Label styledLabel(String text) {
        Label label = new Label(text.toUpperCase());
        label.setStyle("-fx-font-family: 'DM Mono'; -fx-font-size: 10; -fx-text-fill: #6B7280;");
        return label;
    }

    private static class DashboardData {
        List<Product> products;
        List<Category> categories;
        List<Supplier> suppliers;
        int[] summary;
        double totalValue;
    }
}
