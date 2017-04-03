/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.dto;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author tabramishvili
 */
public class ResolvedTargetResponseDTO {

    private Collection<TargetResponseDTO> targets;
    private Map<String, Collection<TargetResponseDTO>> resolves;

    public Collection<TargetResponseDTO> getTargets() {
        return targets;
    }

    public void setTargets(Collection<TargetResponseDTO> targets) {
        this.targets = targets;
    }

    public Map<String, Collection<TargetResponseDTO>> getResolves() {
        return resolves;
    }

    public void setResolves(Map<String, Collection<TargetResponseDTO>> resolves) {
        this.resolves = resolves;
    }

}
