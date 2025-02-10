package subway.domain.station;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import subway.domain.section.Section;

public class Station {
    private static final String SECTION_ADDING_OTHER_SOURCE_STATION_RECEIVE_MESSAGE = "구간 추가시 다른 시작 지점 역을 받았습니다.";

    private final String name;
    private final List<Section> sectionList = new ArrayList<>();

    public Station(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Section> getSectionList() {
        return Collections.unmodifiableList(this.sectionList);
    }

    public void addSection(Section section) {
        this.validateSection(section);
        this.sectionList.add(section);
    }

    public void validateSection(Section section) {
        if(this != section.getSource()) {
            throw new IllegalArgumentException(SECTION_ADDING_OTHER_SOURCE_STATION_RECEIVE_MESSAGE);
        }
    }
}
