package subway.application.section.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import subway.application.section.dto.SectionDTO;
import subway.domain.line.Line;
import subway.domain.line.LineDTO;
import subway.domain.line.LineService;
import subway.domain.station.Station;
import subway.domain.station.StationDTO;
import subway.domain.station.StationService;

public class SectionServiceTest {
    private final LineDTO lineDTO = new LineDTO("line");
    private final StationDTO sourceDTO = new StationDTO("source");
    private final StationDTO sinkDTO = new StationDTO("sink");

    private final LineService lineService = new LineService();
    private final StationService stationService = new StationService();
    private final SectionService sectionService = new SectionService();

    @BeforeEach
    public void setup() {
        this.lineService.addLine(lineDTO);
        this.stationService.addStation(sourceDTO);
        this.stationService.addStation(sinkDTO);
    }

    @Test
    public void addSection() {
        Line line = this.lineService.findOneByName(lineDTO.getName());
        Station source = this.stationService.findOneByName(sourceDTO.getName());
        SectionDTO sectionDTO = new SectionDTO(lineDTO, sourceDTO, sinkDTO);
        assertThat(this.sectionService.findAll()).hasSize(0);
        assertThat(line.getSectionList()).hasSize(0);
        assertThat(source.getSectionList()).hasSize(0);
        this.sectionService.addSection(sectionDTO);
        assertThat(this.sectionService.findAll()).hasSize(1);
        assertThat(line.getSectionList()).hasSize(1);
        assertThat(source.getSectionList()).hasSize(1);
    }

    @Test
    public void addSection__AlreadyExistsSectionException() {
        SectionDTO sectionDTO = new SectionDTO(lineDTO, sourceDTO, sinkDTO);
        String message = "이미 등록되어있는 구간입니다.";
        this.sectionService.addSection(sectionDTO);
        assertThatThrownBy(() -> this.sectionService.addSection(sectionDTO)).isInstanceOf(
                IllegalArgumentException.class)
            .hasMessage(message);
    }

    @AfterEach
    public void init() {
        lineService.deleteAll();
        stationService.deleteAll();
        sectionService.deleteAll();
    }
}
