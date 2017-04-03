/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.dto;

import java.util.List;

/**
 *
 * @author tabramishvili
 */
public class TargetDTO extends AbstractEndpoint{
    private List<DependencyDTO> dependencies;

    public List<DependencyDTO> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyDTO> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return "{" + "dependencies=" + dependencies + '}';
    }
    
    
}
