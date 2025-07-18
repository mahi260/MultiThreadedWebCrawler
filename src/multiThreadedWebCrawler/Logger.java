package multiThreadedWebCrawler;

import javax.swing.JTextArea;
public class Logger {
	private static JTextArea logArea;

    public static void setLogArea(JTextArea area) {
        logArea = area;
    }

    public static void log(String msg) {
        System.out.println(msg);
        if (logArea != null) {
            logArea.append(msg + "\n");
        }
    }
}
