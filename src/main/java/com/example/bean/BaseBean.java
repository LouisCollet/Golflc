package com.example.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

public abstract class BaseBean {

    protected void info(String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    protected void error(String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }
}
