/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Conversations;


import java.io.Serializable;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named(value="cDIConversationScopedBean")
@ConversationScoped
public class CDIConversationScopedBean implements Serializable, interfaces.Log {

	private static final long serialVersionUID = -6541718762358561835L;

	@Inject
    private Conversation conversation;
	
	private String message;
	private String[] words = {"Hello!!","Have a nice day!!","Goodbye..","Hi!","Goodmorning!","Bye..","Good evening.."};

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Conversation getConversation() {
		return conversation;
	}

	@PostConstruct
	public void init(){
            LOG.info("init conversation message");
		message = "Hello from the JavaCodeGeeks..";
	}
	
	public void initConversation(){
		if (!FacesContext.getCurrentInstance().isPostback() 
			&& conversation.isTransient())
                {
			LOG.info("conversation - begin");
			conversation.begin();
		}
	}
	
	public void sendMessage(){
		message = words[new Random().nextInt(7)];
	}
	
	public String next(){
            LOG.info("conversation - next message");
		return "conversation_secondpage.xhtml?faces-redirect=true";
	}
	
	public String endConversation(){
		if(!conversation.isTransient()){
                     LOG.info("conversation - end");
			conversation.end();
		}
		return "conversation_firstpage.xhtml?faces-redirect=true";
	}
	
}
