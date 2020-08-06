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
}
