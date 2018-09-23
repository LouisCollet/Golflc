/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listeners;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

/**
 *
 * @author Collet
 */
public class LifeCycleListener implements interfaces.Log{
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    public void beforePhase(PhaseEvent event) {
        LOG.info("START PHASE " + event.getPhaseId());
    }

    public void afterPhase(PhaseEvent event) {
        LOG.info("END PHASE " + event.getPhaseId());
    }

}
