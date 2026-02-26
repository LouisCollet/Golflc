/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mailtest;


import java.io.Serializable;

public class MailMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String title;
    private final String content;
    private final String recipient;

    public MailMessage(String title, String content, String recipient) {
        this.title = title;
        this.content = content;
        this.recipient = recipient;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getRecipient() { return recipient; }
}
