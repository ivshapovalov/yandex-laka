package ru.yandex.yandexlavka.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ru.yandex.yandexlavka.model.entity.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegionListSerializer extends StdSerializer<List<Region>> {

    public RegionListSerializer() {
        this(null);
    }

    protected RegionListSerializer(Class<List<Region>> t) {
        super(t);
    }

    @Override
    public void serialize(
            List<Region> regions,
            JsonGenerator generator,
            SerializerProvider provider)
            throws IOException, JsonProcessingException {

        List<Integer> ids = new ArrayList<>();
        for (Region region : regions) {
            ids.add(region.getId());
        }
        generator.writeObject(ids);
    }
}