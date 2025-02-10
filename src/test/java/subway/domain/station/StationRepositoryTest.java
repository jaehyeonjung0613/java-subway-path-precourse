package subway.domain.station;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class StationRepositoryTest {
    @Test
    public void exists() {
        Station station = new Station("test");
        assertThat(StationRepository.exists(station)).isEqualTo(false);
        StationRepository.addStation(station);
        assertThat(StationRepository.exists(station)).isEqualTo(true);
        StationRepository.deleteAll();
    }

    @Test
    public void findByName() {
        String name = "test";
        Station station = new Station(name);
        assertThat(StationRepository.findByName(name)).isNotPresent();
        StationRepository.addStation(station);
        assertThat(StationRepository.findByName(name)).isPresent();
        StationRepository.deleteAll();
    }
}
