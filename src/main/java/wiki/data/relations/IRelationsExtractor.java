package wiki.data.relations;

public interface IRelationsExtractor<Result> {
    IRelationsExtractor<Result> extract(String lineToExtractFrom) throws Exception;
    Result getResult();
}
