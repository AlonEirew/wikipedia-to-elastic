package wiki.utils.parsers;

import com.google.gson.stream.JsonReader;

import java.io.IOException;

public interface IWikidataJsonParser<T> {
    T read(JsonReader reader) throws IOException;
}
