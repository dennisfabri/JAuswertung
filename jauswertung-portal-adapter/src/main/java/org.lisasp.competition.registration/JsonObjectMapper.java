package org.lisasp.competition.registration;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonObjectMapper {

    private static final ObjectMapper INSTANCE = new ObjectMapper();

    static {
        INSTANCE.findAndRegisterModules();
        //INSTANCE.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        //INSTANCE.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    public static ObjectMapper getInstance() {
        return INSTANCE;
    }
}
