package org.lisasp.competition.registration.validation;

import lombok.Value;

@Value
public class Range<T> {
    private T min;
    private T max;
}
