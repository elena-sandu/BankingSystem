package org.poo.bank;

import java.util.ArrayList;

public class BusinessCommerciant {
    private ArrayList<User> managers;
    private ArrayList<User> employees;
    private double spent;
    private String name;
    public BusinessCommerciant(String name) {
        managers = new ArrayList<>();
        employees = new ArrayList<>();
        this.name = name;
        this.spent = 0;
    }

    public ArrayList<User> getManagers() {
        return managers;
    }

    public void setManagers(ArrayList<User> managers) {
        this.managers = managers;
    }

    public ArrayList<User> getEmployees() {
        return employees;
    }

    public void setEmployees(ArrayList<User> employees) {
        this.employees = employees;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
