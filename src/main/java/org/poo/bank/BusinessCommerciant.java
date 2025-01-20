package org.poo.bank;

import java.util.ArrayList;
/**
 * Represents a commerciant with separate lists for managing users in the roles of managers and employees.
 * Enables monitoring of individual spending by each manager and employee associated with this commerciant.
 */
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
    /** Returns the list of managers that spent money at this commerciant */
    public ArrayList<User> getManagers() {
        return managers;
    }
    /** Sets the list of managers that spent money at this commerciant */
    public void setManagers(ArrayList<User> managers) {
        this.managers = managers;
    }
    /** Returns the list of employees that spent money at this commerciant */
    public ArrayList<User> getEmployees() {
        return employees;
    }
    /** Sets the list of employees that spent money at this commerciant */
    public void setEmployees(ArrayList<User> employees) {
        this.employees = employees;
    }
    /** Returns the money spent at this commerciant */
    public double getSpent() {
        return spent;
    }
    /** Sets the money spent at this commerciant */
    public void setSpent(double spent) {
        this.spent = spent;
    }
    /** Returns the name of this commerciant */
    public String getName() {
        return name;
    }
    /** Sets the name of this commerciant */
    public void setName(String name) {
        this.name = name;
    }
}
