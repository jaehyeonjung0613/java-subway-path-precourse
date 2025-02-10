package subway.domain.station;

public class StationDTO {
    private static final String STATION_NAME_ESSENTIAL_MESSAGE = "역 이름은 필수입니다.";

    private final String name;

    public StationDTO(String name) {
        this.validate(name);
        this.name = name;
    }

    private void validate(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(STATION_NAME_ESSENTIAL_MESSAGE);
        }
    }

    public String getName() {
        return name;
    }
}
