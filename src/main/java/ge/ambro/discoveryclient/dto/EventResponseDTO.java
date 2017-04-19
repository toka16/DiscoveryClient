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
public class EventResponseDTO extends EventDTO {

    private int serviceId;
    private String service;
    private String base;

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

    @Override
    public String toString() {
        return super.toString() + " EventResponseDTO{" + "serviceId=" + serviceId + ", service=" + service + ", base=" + base + '}';
    }

}
