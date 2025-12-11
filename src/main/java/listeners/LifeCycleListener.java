/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listeners;

import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;


public class LifeCycleListener implements interfaces.Log{
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    public void beforePhase(PhaseEvent event) {
        LOG.debug("START PHASE " + event.getPhaseId());
    }

    public void afterPhase(PhaseEvent event) {
        LOG.debug("END PHASE " + event.getPhaseId());
    }

}
