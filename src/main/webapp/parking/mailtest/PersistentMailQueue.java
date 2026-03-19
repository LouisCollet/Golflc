/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mailtest;



import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistentMailQueue {

    private final File queueFile;
    private final Queue<MailMessage> mailQueue;
    private final AtomicInteger pendingCount;

    public PersistentMailQueue(String filePath) {
        this.queueFile = new File(filePath);
        this.mailQueue = loadQueue();
        this.pendingCount = new AtomicInteger(mailQueue.size());
    }

    @SuppressWarnings("unchecked")
    private Queue<MailMessage> loadQueue() {
        if (!queueFile.exists()) return new LinkedList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(queueFile))) {
            return (Queue<MailMessage>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    private void persistQueue() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(queueFile))) {
            oos.writeObject(mailQueue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void enqueue(MailMessage message) {
        mailQueue.add(message);
        pendingCount.incrementAndGet();
        persistQueue();
    }

    public synchronized MailMessage dequeue() {
        MailMessage msg = mailQueue.poll();
        if (msg != null) pendingCount.decrementAndGet();
        persistQueue();
        return msg;
    }

    public int getPendingCount() {
        return pendingCount.get();
    }
}
