package dev.lumas.shops.components.backing;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.components.requirement.MistralRequirement;
import dev.lumas.shops.components.requirement.PermissionRequirement;
import dev.lumas.shops.interfaces.Codec;

import java.io.IOException;
import java.util.Locale;

/** Serializes the optional permission gate attached to a market item. */
public final class PermissionRequirementCodec extends Codec<PermissionRequirement> {

    private static final TypeToken<PermissionRequirement> TYPE = TypeToken.get(PermissionRequirement.class);

    @Override
    public TypeToken<PermissionRequirement> type() {
        return TYPE;
    }

    @Override
    public void write(JsonWriter out, PermissionRequirement value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        if (value instanceof MistralRequirement) {
            out.name("type").value("MISTRAL");
        } else {
            throw new IOException("Unknown permission requirement: " + value.getClass().getName());
        }
        out.endObject();
    }

    @Override
    public PermissionRequirement read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String type = null;
        in.beginObject();
        while (in.hasNext()) {
            if (in.nextName().equals("type")) {
                type = in.nextString();
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        if (type == null) {
            throw new IOException("Missing permission requirement type");
        }
        return switch (type.toUpperCase(Locale.ROOT)) {
            case "MISTRAL" -> new MistralRequirement();
            default -> throw new IOException("Unknown permission requirement type: " + type);
        };
    }
}
