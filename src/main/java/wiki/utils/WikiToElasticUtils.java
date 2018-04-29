/**
 * @author  Alon Eirew
 */

package wiki.utils;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WikiToElasticUtils {

    private final static Logger LOGGER = LogManager.getLogger(WikiToElasticUtils.class);

    public static InputStream openCompressedFileInputStream(String fileIn) throws IOException {
        LOGGER.debug("opening compressed input stream");
        FileInputStream fin = new FileInputStream(fileIn);
        boolean multiStream = fileIn.contains("multistream");
        return new BZip2CompressorInputStream(fin, multiStream);
    }

    public static void closeCompressedFileInputStream(InputStream is) throws IOException {
        if(is != null) {
            LOGGER.debug("closing compressed input stream");
            is.close();
        }
    }

    public static String getFileContent(String fileName) {
        String fileContent = null;
        try {
            if(fileName != null) {
                URL resource = WikiToElasticUtils.class.getClassLoader().getResource(fileName);
                if(resource != null) {
                    String file = resource.getFile();
                    fileContent = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
                }
            }
        } catch (IOException e) {
            LOGGER.debug("Failed loading file-" + fileName, e);
        }

        return fileContent;
    }
}
