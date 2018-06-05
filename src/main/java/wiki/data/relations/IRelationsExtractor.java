package wiki.data.relations;

public interface IRelationsExtractor<Result> {
    Result extract(String lineToExtractFrom);
}
