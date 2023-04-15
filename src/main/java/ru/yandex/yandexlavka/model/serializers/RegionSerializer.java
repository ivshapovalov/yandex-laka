package ru.yandex.yandexlavka.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ru.yandex.yandexlavka.model.entity.Region;

import java.io.IOException;

public class RegionSerializer extends StdSerializer<Region> {

    public RegionSerializer() {
        this(null);
    }

    protected RegionSerializer(Class<Region> t) {
        super(t);
    }

    @Override
    public void serialize(
            Region region,
            JsonGenerator generator,
            SerializerProvider provider)
            throws IOException, JsonProcessingException {
        generator.writeObject(region.getId());
    }
}
