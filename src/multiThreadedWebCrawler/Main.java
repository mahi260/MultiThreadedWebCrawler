package multiThreadedWebCrawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        WebCrawler.addSeedUrl("https://abcnews.go.com");
        WebCrawler.addSeedUrl("https://www.npr.org");
        WebCrawler.addSeedUrl("https://www.nytimes.com");

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i=1; i<=3; i++) {
            executor.submit(new WebCrawler(i));
        }

        executor.shutdown();
        WebCrawler.shutdownWriter();
    }
}
