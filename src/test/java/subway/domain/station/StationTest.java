package subway.domain.station;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import subway.domain.line.Line;
import subway.domain.section.Section;

public class StationTest {
    @Test
    public void addSection() {
        Line line = new Line("line");
        Station source = new Station("source");
        Station sink = new Station("sink");
        Section section = new Section(line, source, sink, 0, 0);
        assertThat(source.getSectionList()).hasSize(0);
        source.addSection(section);
        assertThat(source.getSectionList()).hasSize(1);
    }

    @Test
    public void addSection__SectionAddingOtherSourceStationReceiveException() {
        Line line = new Line("line");
        Station source = new Station("source");
        Station other = new Station("other");
        Station sink = new Station("sink");
        Section section = new Section(line, other, sink, 0, 0);
        String message = "구간 추가시 다른 시작 지점 역을 받았습니다.";
        assertThatThrownBy(() -> source.addSection(section)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }
}
