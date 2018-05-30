package wiki.utils;

import org.junit.Assert;
import org.junit.Test;
import wiki.handlers.ArrayPageHandler;
import wiki.parsers.IWikiParser;
import wiki.parsers.STAXParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class TestWikiToElasticUtils {

    @Test
    public void testParseWikipediaXMLdump() throws IOException {
        InputStream inputStream = null;
        try {
            long startTime = System.currentTimeMillis();
            TestWikiToElasticUtils tu = new TestWikiToElasticUtils();
            URL resource = tu.getClass().getClassLoader().getResource("tinywiki-latest-pages-articles.xml.bz2");
            if(resource != null) {
                String tinyWikifile = resource.getFile();
                inputStream = WikiToElasticUtils.openCompressedFileInputStream(tinyWikifile);

                ArrayPageHandler arrayPageHandler = new ArrayPageHandler();
                IWikiParser parser = new STAXParser(arrayPageHandler);
                parser.parse(inputStream);

                System.out.println("total time took to parse: " + (System.currentTimeMillis() - startTime));
                String output = String.valueOf(arrayPageHandler.getPages().size());
                Assert.assertNotNull(output);
                Assert.assertFalse(output.isEmpty());
            }
        } finally {
            WikiToElasticUtils.closeCompressedFileInputStream(inputStream);
        }
    }
}
