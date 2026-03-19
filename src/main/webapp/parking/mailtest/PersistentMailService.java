package mailtest;

import static interfaces.Log.LOG;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersistentMailService {

    private final PersistentMailQueue mailQueue;
    private final ExecutorService executor;

    public PersistentMailService(String queueFilePath) {
        this.mailQueue = new PersistentMailQueue(queueFilePath);
        this.executor = Executors.newFixedThreadPool(3); // 3 threads d’envoi
        startWorker();
    }

    private void startWorker() {
        LOG.debug("worker started !");
        executor.submit(() -> {
            while (true) {
                MailMessage msg = mailQueue.dequeue();
                if (msg != null) {
                    try {
                        // Remplace par ton sendHtmlMail
                        System.out.println("Envoi du mail à " + msg.getRecipient());
                        Thread.sleep(1000); // simulation
                    } catch (Exception e) {
                        e.printStackTrace();
                        // ré-enqueue en cas d’échec
                        mailQueue.enqueue(msg);
                    }
                } else {
                    Thread.sleep(500); // pause si queue vide
                }
            }
        });
    }

    public void submitMail(MailMessage msg) {
        mailQueue.enqueue(msg);
    }

    public int getPendingCount() {
        return mailQueue.getPendingCount();
    }
}