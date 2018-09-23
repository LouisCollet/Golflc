/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

/**
 *
 * @author Collet
 */
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ContextParam;

@Named
@RequestScoped
public class cdiContextParamBean {

    @Inject @ContextParam(name="javax.faces.FACELETS_BUFFER_SIZE")
    private String faceletsBufferSize;

    @Inject @ContextParam(name="javax.faces.FACELETS_LIBRARIES")
    private String faceletsLibraries;

    @Inject @ContextParam(name="javax.faces.FACELETS_SKIP_COMMENTS")
    private String faceletsSkipComments;

    public String getFaceletsBufferSize() {
        return faceletsBufferSize;
    }

    public String getFaceletsLibraries() {
        return faceletsLibraries;
    }

    public String getFaceletsSkipComments() {
        return faceletsSkipComments;
    }

}
