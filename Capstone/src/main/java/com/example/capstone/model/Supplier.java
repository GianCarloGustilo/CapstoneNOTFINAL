package com.example.capstone.model;

/**
 * Supplier inherits from BaseEntity.
 * This shows INHERITANCE.
 */
public class Supplier extends BaseEntity {

    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;

    public Supplier() {
    }

    public int getSupplierId() {
        return id;
    }

    public void setSupplierId(int supplierId) {
        this.id = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getDisplayLabel() {
        return name;
    }
}
