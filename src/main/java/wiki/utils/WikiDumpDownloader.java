package wiki.utils;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WikiDumpDownloader {
    public static void main(String[] args) throws IOException {
        String fileUrl = "https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles14.xml-p7697595p7744800.bz2";
        String saveAt = "dumps/exper.xml.bz2";
        String filePresentName = "articles14";

        URL url = new URL(fileUrl);
        HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
        long completeFileSize = httpConnection.getContentLength();

        try(InputStream inputStream = url.openStream();
            CountingInputStream cis = new CountingInputStream(inputStream);
            FileOutputStream fileOS = new FileOutputStream(saveAt);
            ProgressBar pb = new ProgressBar(filePresentName, Math.floorDiv(completeFileSize, 1000),
                    1000, System.err, ProgressBarStyle.ASCII, "KB",
                    1, false, null)) {

            pb.setExtraMessage("Downloading...");

            new Thread(() -> {
                try {
                    IOUtils.copyLarge(cis, fileOS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (cis.getByteCount() < completeFileSize) {
                pb.stepTo(Math.floorDiv(cis.getByteCount(), 1000));
            }

            pb.stepTo(Math.floorDiv(cis.getByteCount(), 1000));
            System.out.println("File downloaded successfully!");
        }
    }
}
