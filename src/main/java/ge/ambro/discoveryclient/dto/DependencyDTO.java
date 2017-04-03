/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.dto;

/**
 *
 * @author tabramishvili
 */
public class DependencyDTO {
    private String address;
    private int priority;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "{" + "address=" + address + ", priority=" + priority + '}';
    }
    
}
