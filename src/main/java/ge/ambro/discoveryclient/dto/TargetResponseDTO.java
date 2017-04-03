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
public class TargetResponseDTO extends TargetDTO {

    private int serviceId;
    private String service;
    private String base;
    private String address;

    public TargetResponseDTO() {

    }

    public TargetResponseDTO(TargetDTO target) {
        preset(target);
    }

    private void preset(TargetDTO target) {
        setName(target.getName());
        setMethod(target.getMethod());
        setPath(target.getPath());
        setDependencies(target.getDependencies());
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
