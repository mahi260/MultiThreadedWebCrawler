package multiThreadedWebCrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebCrawler implements Runnable {
    private static final int MAX_DEPTH = 3;
    private static final int TIMEOUT = 5000;
    private static final int RETRIES = 3;
    private static final int DELAY = 500;

    private static final Set<String> globalVisitedLinks = Collections.synchronizedSet(new HashSet<>());
    private static final BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
    private static PrintWriter writer;

    static {
        try {
        	String path = System.getProperty("user.dir") + "/output/crawled_pages.txt";
        	new File("output").mkdir(); // create folder if not exists
        	writer = new PrintWriter(new FileWriter(path), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int ID;

    public WebCrawler(int id) {
        this.ID = id;
    }
    
    private static volatile boolean isStopped = false;

    public static void stopCrawler() {
        isStopped = true;
        queue.clear(); // unblock any waiting threads
    }


    @Override
    public void run() {
        while (!isStopped) {
            try {
                CrawlTask task = queue.poll(10, TimeUnit.SECONDS);
                if (task == null) {
                    Logger.log("Bot ID:" + ID + " shutting down - no more tasks.");
                    break;
                }
                crawl(task);
            } catch (InterruptedException e) {
                Logger.log("Bot ID:" + ID + " interrupted.");
                break;
            }
        }
    }


    private void crawl(CrawlTask task) {
        if (task.level > MAX_DEPTH) return;

        Document doc = requestWithRetry(task.url);
        if (doc != null) {
            writer.println(task.url + " | " + doc.title());
            Logger.log("Crawled: " + task.url + " | " + doc.title());

            for (Element link : doc.select("a[href]")) {
                String next_link = normalizeUrl(link.absUrl("href"));
                if (next_link != null && globalVisitedLinks.add(next_link)) {
                    queue.offer(new CrawlTask(task.level + 1, next_link));
                }
            }
        }

        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Document requestWithRetry(String url) {
        int attempts = 0;
        while (attempts < RETRIES) {
            try {
                Connection con = Jsoup.connect(url)
                        .timeout(TIMEOUT)
                        .userAgent("Mozilla/5.0 (compatible; MyCrawler/1.0)");
                Document doc = con.get();

                if (con.response().statusCode() == 200) {
                    Logger.log("Bot ID:" + ID + " Received webpage at " + url);
                    return doc;
                }
            } catch (IOException e) {
                Logger.log("Bot ID:" + ID + " failed attempt " + (attempts + 1) + " for " + url);
            }
            attempts++;
        }
        Logger.log("Bot ID:" + ID + " giving up on " + url);
        return null;
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        if (!url.startsWith("http://") && !url.startsWith("https://")) return null;
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return url;
    }

    public static void addSeedUrl(String url) {
        String norm = (new WebCrawler(0)).normalizeUrl(url);
        if (norm != null && globalVisitedLinks.add(norm)) {
            queue.offer(new CrawlTask(1, norm));
        }
    }

    public static void shutdownWriter() {
        writer.close();
    }

    static class CrawlTask {
        int level;
        String url;

        CrawlTask(int level, String url) {
            this.level = level;
            this.url = url;
        }
    }
}
