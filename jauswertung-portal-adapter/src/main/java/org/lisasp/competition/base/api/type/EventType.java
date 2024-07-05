package org.lisasp.competition.base.api.type;

public enum EventType {
    Individual, Team;

    public static EventType fromString(String eventType) {
        for (EventType type : EventType.values()) {
            if (type.name().equalsIgnoreCase(eventType)) {
                return type;
            }
        }
        return null;
    }
}
