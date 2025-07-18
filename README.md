# Multi-Threaded Web Crawler with Swing UI

Java application to crawl multiple websites concurrently using ExecutorService. Includes a Swing-based GUI for URL input, start/stop controls, and live logs display.

## Features

- Multi-threading with fixed thread pool
- URL normalization and retry mechanism
- Rate limiting and graceful shutdown
- Live logging dashboard in UI
- Saves crawled URLs and page titles to `crawled_pages.txt`

## Libraries Used

- Jsoup (HTML parsing)
- Swing (UI)
- Java Concurrency (ExecutorService)

