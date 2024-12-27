package de.df.jauswertung.io.portal;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Value
public class Nationality {
    @NonNull
    private final String alpha2Code;
    private final String iocCode;
    @NonNull
    private final String name;
}
