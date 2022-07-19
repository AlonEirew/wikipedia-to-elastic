package wiki.config;

public class JsonFileConfiguration {
    private String outIndexDirectory;
    private int pagesPerFile;

    public String getOutIndexDirectory() {
        return outIndexDirectory;
    }

    public void setOutIndexDirectory(String outIndexDirectory) {
        this.outIndexDirectory = outIndexDirectory;
    }

    public int getPagesPerFile() {
        return pagesPerFile;
    }

    public void setPagesPerFile(int pagesPerFile) {
        this.pagesPerFile = pagesPerFile;
    }
}
