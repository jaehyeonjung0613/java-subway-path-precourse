package subway.presentation;

import subway.domain.station.StationDTO;
import subway.domain.station.StationService;

public class StationController {
    private final StationService stationService = new StationService();

    public void addStation(String name) {
        StationDTO stationDTO = new StationDTO(name);
        stationService.addStation(stationDTO);
    }
}
