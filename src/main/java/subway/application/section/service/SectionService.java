package subway.application.section.service;

import java.util.List;

import subway.application.section.dto.SectionDTO;
import subway.domain.line.Line;
import subway.domain.line.LineService;
import subway.domain.section.Section;
import subway.domain.section.SectionRepository;
import subway.domain.station.Station;
import subway.domain.station.StationService;

public class SectionService {
    private static final String ALREADY_EXISTS_SECTION_MESSAGE = "이미 등록되어있는 구간입니다.";

    private final LineService lineService = new LineService();
    private final StationService stationService = new StationService();

    public List<Section> findAll() {
        return SectionRepository.sections();
    }

    public void addSection(SectionDTO sectionDTO) {
        Line line = this.lineService.findOneByName(sectionDTO.getLineDTO().getName());
        Station source = this.stationService.findOneByName(sectionDTO.getSourceDTO().getName());
        Station sink = this.stationService.findOneByName(sectionDTO.getSinkDTO().getName());
        Section section = new Section(line, source, sink, sectionDTO.getDistance(), sectionDTO.getTime());
        if (SectionRepository.exists(section)) {
            throw new IllegalArgumentException(ALREADY_EXISTS_SECTION_MESSAGE);
        }
        SectionRepository.addSection(section);
        line.addSection(section);
        source.addSection(section);
    }

    public void deleteAll() {
        SectionRepository.deleteAll();
    }
}
