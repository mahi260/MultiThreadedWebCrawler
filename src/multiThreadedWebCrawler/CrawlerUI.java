package multiThreadedWebCrawler;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrawlerUI extends JFrame {
    private JTextField urlField;
    private JButton addButton, startButton, stopButton;
    private JTextArea logArea;
    private ExecutorService executor;

    public CrawlerUI() {
        setTitle("Multi-threaded Web Crawler");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout());
        urlField = new JTextField(40);
        addButton = new JButton("Add URL");
        startButton = new JButton("Start Crawler");
        stopButton = new JButton("Stop Crawler");
        stopButton.setEnabled(false); // disable by default
        stopButton.setVisible(true);  // ensure it's visible

        topPanel.add(new JLabel("Seed URL:"));
        topPanel.add(urlField);
        topPanel.add(addButton);
        topPanel.add(startButton);
        topPanel.add(stopButton);
        add(topPanel, BorderLayout.NORTH);

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(800,500));
        add(logScroll, BorderLayout.CENTER);

        // Logger setup
        Logger.setLogArea(logArea);

        // Action Listeners
        addButton.addActionListener(e -> {
            String url = urlField.getText().trim();
            if (!url.isEmpty()) {
                WebCrawler.addSeedUrl(url);
                Logger.log("Added seed URL: " + url);
                urlField.setText("");
            }
        });

        startButton.addActionListener(e -> startCrawler());
        stopButton.addActionListener(e -> stopCrawler());

        setVisible(true);
    }

    private void startCrawler() {
        if (executor != null && !executor.isShutdown()) {
            Logger.log("Crawler already running.");
            return;
        }

        executor = Executors.newFixedThreadPool(3);
        for (int i=1; i<=3; i++) {
            executor.submit(new WebCrawler(i));
        }
        Logger.log("Crawler started.");

        startButton.setEnabled(false);
        stopButton.setEnabled(true); // enable stop when crawling starts
    }


    private void stopCrawler() {
        if (executor != null) {
            WebCrawler.stopCrawler();
            executor.shutdownNow();
            WebCrawler.shutdownWriter();
            Logger.log("Crawler stopped.");

            stopButton.setEnabled(false); // disable stop after stopping
            startButton.setEnabled(true); // re-enable start if needed
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(CrawlerUI::new);
    }
}
