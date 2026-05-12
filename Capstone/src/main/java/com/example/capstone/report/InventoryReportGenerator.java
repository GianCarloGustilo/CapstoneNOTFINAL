package com.example.capstone.report;

import com.example.capstone.database.ProductDAO;
import com.example.capstone.model.Product;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * This class shows REPORT GENERATION and FILE HANDLING.
 */
public class InventoryReportGenerator {

    private final ProductDAO productDAO = new ProductDAO();

    public int generateCSV(File file) {
        List<Product> products = productDAO.getAll();

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.println("Product ID,Name,Category,Supplier,Quantity,Price,Low Stock Threshold,Status");

            for (Product product : products) {
                String status = product.isLowStock() ? "LOW STOCK" : "OK";
                writer.println(
                        product.getProductId() + ","
                                + clean(product.getName()) + ","
                                + clean(product.getCategoryName()) + ","
                                + clean(product.getSupplierName()) + ","
                                + product.getQuantity() + ","
                                + product.getPrice() + ","
                                + product.getLowStockThreshold() + ","
                                + status
                );
            }

            writer.close();
            return products.size();
        } catch (Exception e) {
            System.out.println("Error creating CSV report: " + e.getMessage());
            return -1;
        }
    }

    public int generateTextSummary(File file) {
        List<Product> products = productDAO.getAll();
        List<Product> lowStockProducts = productDAO.getLowStock();
        double totalValue = productDAO.getTotalValue();

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.println("CORESTOCK INVENTORY REPORT");
            writer.println("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a")));
            writer.println();
            writer.println("Total Products: " + products.size());
            writer.println("Low Stock Products: " + lowStockProducts.size());
            writer.println("Total Inventory Value: P " + String.format("%,.2f", totalValue));
            writer.println();
            writer.println("Low Stock List:");

            for (Product product : lowStockProducts) {
                writer.println("- " + product.getName() + " | Qty: " + product.getQuantity() + " | Min: " + product.getLowStockThreshold());
            }

            writer.println();
            writer.println("All Products:");

            for (Product product : products) {
                writer.println(
                        product.getProductId() + " | "
                                + product.getName() + " | "
                                + product.getCategoryName() + " | "
                                + product.getQuantity() + " | "
                                + product.getPrice()
                );
            }

            writer.close();
            return products.size();
        } catch (Exception e) {
            System.out.println("Error creating text report: " + e.getMessage());
            return -1;
        }
    }

    public String defaultFilename(String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return "corestock_report_" + LocalDateTime.now().format(formatter) + "." + extension;
    }

    private String clean(String text) {
        if (text == null) {
            return "";
        }

        return text.replace(",", " ");
    }
}
