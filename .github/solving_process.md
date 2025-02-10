# 🧐 지하철 노선도 경로 조회 미션

[우아한테크코스](https://github.com/woowacourse) precourse 문제
중 [지하철 노선도 경로 조회 미션](https://github.com/woowacourse/java-subway-path-precourse) 풀이 기록하기.

DDD 구조와 MVC 패턴을 적용하여 TDD 방식으로 개발하고, 입출력 및 프로그래밍 요구사항을 부합하도록 풀어 볼 예정.

## 0. 설계

### application

|  비즈니스   | 기능                                                                |
|:-------:|:------------------------------------------------------------------|
| section | - 구간 CRUD<br/> - 그래프 노드 추가<br/> - 최단거리 경로 구하기 <br/> - 최소시간 경로 구하기 |

### domain

|  비즈니스   | 기능                     |
|:-------:|:-----------------------|
|  line   | - 노선 객체<br/> - 노선 CRUD |
| section | - 구간 객체<br/> - 구간 CRUD |
| station | - 역 객체<br/> - 역 CRUD   |

### infrastructure

|      클래스      | 기능                      |
|:-------------:|:------------------------|
|  FileParser   | - abstract<br/> - 파일 검증 |
| XmlFileParser | - xml 파일 로드 및 데이터 파싱    |

### presentation

|         클래스         | 기능                             |
|:-------------------:|:-------------------------------|
|   ViewController    | - interface                    |
| IntroViewController | - 도메인 데이터 초기화<br/> - 인트로 화면 제어 |
| MainViewController  | - 메인 화면 제어                     |
| PathViewController  | - 경로 기준(화면) 제어                 |
|   LineController    | - 노선 비즈니스 처리                   |
|  SectionController  | - 구간 비즈니스 처리                   |
|  StationController  | - 역 비즈니스 처리                    |
| ShortCostController | - 최단(최소) 경로 처리                 |

### ui

|   클래스   | 기능          |
|:-------:|:------------|
| Console | - 콘솔 입출력 처리 |

### view

|        클래스        | 기능                                              |
|:-----------------:|:------------------------------------------------|
|       View        | - interface                                     |
|     MenuView      | - 메뉴 출력<br/> - 항목 입력 <br/> - 메뉴 선택 이벤트 발생       |
|       Menu        | - interface<br/> - 메뉴 목록 조회<br/> - 명령어 기준 단건 조회 |
| MenuEventRegister | - 메뉴 선택 이벤트 처리 등록                               |

### util

|    클래스     | 기능          |
|:----------:|:------------|
| Validation | - 공통 유효성 검증 |

## 1. Line CRUD

```java
// LineDTOTest.java

package subway.domain.line;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LineDTOTest {
    @Test
    public void new__LineNameEssentialException() {
        String message = "노선 이름은 필수입니다.";
        assertThatThrownBy(() -> new LineDTO(null)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
        assertThatThrownBy(() -> new LineDTO("")).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }
}
```

```java
// LineDTO.java

package subway.domain.line;

public class LineDTO {
    private static final String LINE_NAME_ESSENTIAL_MESSAGE = "노선 이름은 필수입니다.";

    private final String name;

    public LineDTO(String name) {
        this.validate(name);
        this.name = name;
    }

    private void validate(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(LINE_NAME_ESSENTIAL_MESSAGE);
        }
    }

    public String getName() {
        return this.name;
    }
}
```

다양한 계층에서 쓰일 기본 LineDTO 구현.

```java
// LineService.java

package subway.domain.line;

import java.util.List;

public class LineService {
    public List<Line> findAll() {
        return LineRepository.lines();
    }

    public void deleteAll() {
        LineRepository.deleteAll();
    }
}
```

기본 전체 조회 및 삭제 기능 생성.

### 1-1. CREATE

```java
// LineRepositoryTest.java

package subway.domain.line;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LineRepositoryTest {
    @Test
    public void exists() {
        Line line = new Line("test");
        assertThat(LineRepository.exists(line)).isEqualTo(false);
        LineRepository.addLine(line);
        assertThat(LineRepository.exists(line)).isEqualTo(true);
    }

    @AfterEach
    public void init() {
        LineRepository.deleteAll();
    }
}
```

```java
// LineRepository.java

package subway.domain.line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LineRepository {
    public static boolean exists(Line other) {
        return lines.stream().anyMatch(line -> line.getName().equals(other.getName()));
    }
}
```

노선 중복 생성 방지를 위해 이름 기준으로 존재 여부 확인 기능 구현.

```java
// LineServiceTest.java

package subway.domain.line;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LineServiceTest {
    private final LineService lineService = new LineService();

    @Test
    public void addLine() {
        LineDTO lineDTO = new LineDTO("test");
        assertThat(lineService.findAll()).hasSize(0);
        this.lineService.addLine(lineDTO);
        assertThat(lineService.findAll()).hasSize(1);
    }

    @Test
    public void addLine__AlreadyExistsLineException() {
        LineDTO lineDTO = new LineDTO("test");
        String message = "이미 등록되어있는 노선입니다.";
        this.lineService.addLine(lineDTO);
        assertThatThrownBy(() -> this.lineService.addLine(lineDTO)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }

    @AfterEach
    public void init() {
        this.lineService.deleteAll();
    }
}
```

```java
// LineService.java

package subway.domain.line;

import java.util.List;

public class LineService {
    private static final String ALREADY_EXISTS_LINE_MESSAGE = "이미 등록되어있는 노선입니다.";

    public void addLine(LineDTO lineDTO) {
        Line line = new Line(lineDTO.getName());
        if (LineRepository.exists(line)) {
            throw new IllegalArgumentException(ALREADY_EXISTS_MESSAGE);
        }
        LineRepository.addLine(line);
    }
}
```

노선 추가 기능 구현.

```java
// LineController.java

package subway.presentation;

import subway.domain.line.LineDTO;
import subway.domain.line.LineService;

public class LineController {
    private final LineService lineService = new LineService();

    public void addLine(String lineName) {
        LineDTO lineDTO = new LineDTO(lineName);
        this.lineService.addLine(lineDTO);
    }
}
```

제어 계층에 노선 추가 기능 매핑.

### 1-2. READ

```java
// LineRepositoryTest.java

package subway.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import subway.domain.line.Line;
import subway.domain.line.LineRepository;

public class LineRepositoryTest {
    @Test
    public void findByName() {
        String name = "test";
        Line line = new Line(name);
        assertThat(LineRepository.findByName(name)).isNotPresent();
        LineRepository.addLine(line);
        assertThat(LineRepository.findByName(name)).isPresent();
    }
}
```

```java
// LineRepository.java

package subway.domain.line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LineRepository {
    public static Optional<Line> findByName(String name) {
        return lines.stream().filter(line -> line.getName().equals(name)).findFirst();
    }
}
```

```java
// LineServiceTest.java

package subway.domain.line;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LineServiceTest {
    @Test
    public void findOneByName() {
        String name = "test";
        LineDTO lineDTO = new LineDTO(name);
        this.lineService.addLine(lineDTO);
        Line line = this.lineService.findOneByName(name);
        assertThat(line.getName()).isEqualTo(name);
    }

    @Test
    public void findOneByName__NotExistsLineException() {
        String message = "존재하지 않은 노선입니다.";
        assertThatThrownBy(() -> this.lineService.findOneByName("test")).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }
}
```

```java
// LineService.java

package subway.domain.line;

import java.util.List;

public class LineService {
    private static final String NOT_EXISTS_LINE_MESSAGE = "존재하지 않은 노선입니다.";

    public Line findOneByName(String name) {
        return LineRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException(NOT_EXISTS_MESSAGE));
    }
}
```

노선 이름 기준 단건 조회 기능 구현.

## 2. Station CRUD

```java
// StationDTOTest.java

package subway.domain.station;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class StationDTOTest {
    @Test
    public void new__StationNameEssentialException() {
        String message = "역 이름은 필수입니다.";
        assertThatThrownBy(() -> new StationDTO(null)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
        assertThatThrownBy(() -> new StationDTO("")).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }
}
```

```java
// StationDTO.java

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
```

다양한 계층에서 쓰일 기본 StationDTO 구현.

```java
// StationService.java

package subway.domain.station;

import java.util.List;

public class StationService {
    public List<Station> findAll() {
        return StationRepository.stations();
    }
    
    public void deleteAll() {
        StationRepository.deleteAll();
    }
}

```

기본 전체 조회 및 삭제 기능 생성.

### 2-1. CREATE

```java
// StationRepositoryTest.java

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
}
```

```java
// StationRepository.java

package subway.domain.station;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StationRepository {
    public static boolean exists(Station other) {
        return stations().stream().anyMatch(station -> station.getName().equals(other.getName()));
    }
}
```

역 중복 생성 방지를 위해 이름 기준으로 존재 여부 확인 기능 구현.

```java
// StationServiceTest.java

package subway.domain.station;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class StationServiceTest {
    private final StationService stationService = new StationService();

    @Test
    public void addStation() {
        StationDTO stationDTO = new StationDTO("test");
        assertThat(this.stationService.findAll()).hasSize(0);
        this.stationService.addStation(stationDTO);
        assertThat(this.stationService.findAll()).hasSize(1);
        this.stationService.deleteAll();
    }

    @Test
    public void addStation__AlreadyExistsStationException() {
        StationDTO stationDTO = new StationDTO("test");
        String message = "이미 등록되어있는 역입니다.";
        this.stationService.addStation(stationDTO);
        assertThatThrownBy(() -> this.stationService.addStation(stationDTO)).isInstanceOf(
            IllegalArgumentException.class).hasMessage(message);
        this.stationService.deleteAll();
    }
}
```

```java
// StationService.java

package subway.domain.station;

import java.util.List;

public class StationService {
    private static final String ALREADY_EXISTS_STATION_MESSAGE = "이미 등록되어있는 역입니다.";
    
    public void addStation(StationDTO stationDTO) {
        Station station = new Station(stationDTO.getName());
        if (StationRepository.exists(station)) {
            throw new IllegalArgumentException(ALREADY_EXISTS_STATION_MESSAGE);
        }
        StationRepository.addStation(station);
    }
}
```

역 추가 기능 구현.

```java
// StationController.java

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
```

제어 계층에 역 추가 기능 매핑.

### 2-2. READ

```java
// StationRepositoryTest.java

package subway.domain.station;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class StationRepositoryTest {
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
```

```java
// StationRepository.java

package subway.domain.station;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StationRepository {
    public static Optional<Station> findByName(String name) {
        return stations.stream().filter(station -> station.getName().equals(name)).findFirst();
    }
}
```

```java
// StationRepositoryTest.java

package subway.domain.station;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class StationServiceTest {
    @Test
    public void findOneByName() {
        String name = "test";
        StationDTO stationDTO = new StationDTO(name);
        this.stationService.addStation(stationDTO);
        Station station = this.stationService.findOneByName(name);
        assertThat(station.getName()).isEqualTo(name);
        this.stationService.deleteAll();
    }

    @Test
    public void findOneByName__NotExistsStationException() {
        String message = "존재하지 않은 역입니다.";
        assertThatThrownBy(() -> this.stationService.findOneByName("test")).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }
}
```

```java
// StationService.java

package subway.domain.station;

import java.util.List;

public class StationService {
    private static final String NOT_EXISTS_STATION_MESSAGE = "존재하지 않은 역입니다.";

    public Station findOneByName(String name) {
        return StationRepository.findByName(name)
            .orElseThrow(() -> new IllegalArgumentException(NOT_EXISTS_STATION_MESSAGE));
    }
}
```

역 이름 기준 단건 조회 기능 구현.

## 3. Section CRUD

```java
// Section.java

package subway.domain.section;

import subway.domain.line.Line;
import subway.domain.station.Station;

public class Section {
    private final Line line;
    private final Station source;
    private final Station sink;
    private final int distance;
    private final int time;

    public Section(Line line, Station source, Station sink, int distance, int time) {
        this.line = line;
        this.source = source;
        this.sink = sink;
        this.distance = distance;
        this.time = time;
    }

    public Line getLine() {
        return line;
    }

    public Station getSource() {
        return source;
    }

    public Station getSink() {
        return sink;
    }

    public int getDistance() {
        return distance;
    }

    public int getTime() {
        return time;
    }
}
```

구간 객체 생성.

```java
// LineTest.java

package subway.domain.line;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import subway.domain.section.Section;
import subway.domain.station.Station;

public class LineTest {
    @Test
    public void addSection() {
        Line line = new Line("line");
        Station source = new Station("source");
        Station sink = new Station("sink");
        Section section = new Section(line, source, sink, 0, 0);
        assertThat(line.getSectionList()).hasSize(0);
        line.addSection(section);
        assertThat(line.getSectionList()).hasSize(1);
    }

    @Test
    public void addSection__SectionAddingOtherLineReceiveException() {
        Line line = new Line("line");
        Line other = new Line("other");
        Station source = new Station("source");
        Station sink = new Station("sink");
        Section section = new Section(other, source, sink, 0, 0);
        String message = "구간 추가시 다른 노선을 받았습니다.";
        assertThatThrownBy(() -> line.addSection(section)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }
}
```

```java
// Line.java

package subway.domain.line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import subway.domain.section.Section;

public class Line {
    private static final String SECTION_ADDING_OTHER_LINE_RECEIVE_MESSAGE = "구간 추가시 다른 노선을 받았습니다.";
    
    private final List<Section> sectionList = new ArrayList<>();
    
    public void addSection(Section section) {
        this.validateSection(section);
        this.sectionList.add(section);
    }

    private void validateSection(Section section) {
        if(this != section.getLine()) {
            throw new IllegalArgumentException(SECTION_ADDING_OTHER_LINE_RECEIVE_MESSAGE);
        }
    }
}
```

Line 1 : N  매칭.

Section 추가 기능 구현.

```java
// StationTest.java

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
```

```java
// Station.java

package subway.domain.station;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import subway.domain.section.Section;

public class Station {
    private static final String SECTION_ADDING_OTHER_SOURCE_STATION_RECEIVE_MESSAGE = "구간 추가시 다른 시작 지점 역을 받았습니다.";
    
    private final List<Section> sectionList = new ArrayList<>();
    
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
```

Station 1 : N  매칭.

Section 추가 기능 구현.

```java
// SectionDTOTest.java

package subway.application.section.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import subway.domain.line.LineDTO;
import subway.domain.station.StationDTO;

public class SectionDTOTest {
    @Test
    public void new__SectionAddingLineInfoEssentialException() {
        StationDTO sourceDTO = new StationDTO("source");
        StationDTO sinkDTO = new StationDTO("sink");
        String message = "구간 생성시 노선 정보는 필수입니다.";
        assertThatThrownBy(() -> new SectionDTO(null, sourceDTO, sinkDTO)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }

    @Test
    public void new__SectionAddingSourceStationInfoEssentialException() {
        LineDTO lineDTO = new LineDTO("line");
        StationDTO sinkDTO = new StationDTO("sink");
        String message = "구간 생성시 시작 지점 역 정보는 필수입니다.";
        assertThatThrownBy(() -> new SectionDTO(lineDTO, null, sinkDTO)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }

    @Test
    public void new__SectionAddingSinkStationInfoEssentialException() {
        LineDTO lineDTO = new LineDTO("line");
        StationDTO sourceDTO = new StationDTO("source");
        String message = "구간 생성시 종료 지점 역 정보는 필수입니다.";
        assertThatThrownBy(() -> new SectionDTO(lineDTO, sourceDTO, null)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
    }
}
```

```java
// SectionDTO.java

package subway.application.section.dto;

import subway.domain.line.LineDTO;
import subway.domain.station.StationDTO;

public class SectionDTO {
    private static final String SECTION_ADDING_LINE_INFO_ESSENTIAL_MESSAGE = "구간 생성시 노선 정보는 필수입니다.";
    private static final String SECTION_ADDING_SOURCE_STATION_INFO_ESSENTIAL_MESSAGE = "구간 생성시 시작 지점 역 정보는 필수입니다.";
    private static final String SECTION_ADDING_SINK_STATION_INFO_ESSENTIAL_MESSAGE = "구간 생성시 종료 지점 역 정보는 필수입니다.";

    private final LineDTO lineDTO;
    private final StationDTO sourceDTO;
    private final StationDTO sinkDTO;
    private int distance;
    private int time;

    public SectionDTO(LineDTO lineDTO, StationDTO sourceDTO, StationDTO sinkDTO) {
        this(lineDTO, sourceDTO, sinkDTO, 0, 0);
    }

    public SectionDTO(LineDTO lineDTO, StationDTO sourceDTO, StationDTO sinkDTO, int distance, int time) {
        this.validate(lineDTO, sourceDTO, sinkDTO);
        this.lineDTO = lineDTO;
        this.sourceDTO = sourceDTO;
        this.sinkDTO = sinkDTO;
        this.distance = distance;
        this.time = time;
    }

    private void validate(LineDTO lineDTO, StationDTO sourceDTO, StationDTO sinkDTO) {
        if (lineDTO == null) {
            throw new IllegalArgumentException(SECTION_ADDING_LINE_INFO_ESSENTIAL_MESSAGE);
        }
        if (sourceDTO == null) {
            throw new IllegalArgumentException(SECTION_ADDING_SOURCE_STATION_INFO_ESSENTIAL_MESSAGE);
        }
        if (sinkDTO == null) {
            throw new IllegalArgumentException(SECTION_ADDING_SINK_STATION_INFO_ESSENTIAL_MESSAGE);
        }
    }

    public LineDTO getLineDTO() {
        return lineDTO;
    }

    public StationDTO getSourceDTO() {
        return sourceDTO;
    }

    public StationDTO getSinkDTO() {
        return sinkDTO;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
```

다양한 계층에서 쓰일 기본 SectionDTO 구현.

```java
// SectionRepository.java

package subway.domain.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SectionRepository {
    private static final List<Section> sections = new ArrayList<>();

    public static List<Section> sections() {
        return Collections.unmodifiableList(sections);
    }

    public static void deleteAll() {
        sections.clear();
    }
}
```

```java
// SectionService.java

package subway.application.section.service;

import java.util.List;

import subway.domain.section.Section;
import subway.domain.section.SectionRepository;

public class SectionService {
    public List<Section> findAll() {
        return SectionRepository.sections();
    }

    public void deleteAll() {
        SectionRepository.deleteAll();
    }
}
```

기본 전체 조회 및 삭제 기능 생성.

### 3-1. CREATE

```java
// SectionRepositoryTest.java

package subway.domain.section;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import subway.domain.line.Line;
import subway.domain.station.Station;

public class SectionRepositoryTest {
    @Test
    public void exists() {
        Line line = new Line("line");
        Station source = new Station("source");
        Station sink = new Station("sink");
        Section section = new Section(line, source, sink, 0, 0);
        assertThat(SectionRepository.exists(section)).isEqualTo(false);
        SectionRepository.addSection(section);
        assertThat(SectionRepository.exists(section)).isEqualTo(true);
        SectionRepository.deleteAll();
    }
}
```

```java
// SectionRepository.java

package subway.domain.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SectionRepository {
    public static void addSection(Section section) {
        sections.add(section);
    }

    public static boolean exists(Section other) {
        return sections.stream()
            .anyMatch(section -> section.getLine() == other.getLine() && section.getSource() == other.getSource()
                && section.getSink() == other.getSink());
    }
}
```

구간 데이터 추가 기능 구현.

구간 중복 생성 방지를 위해 노선, 시작 지점 역, 종료 지점 역 기준으로 존재 여부 확인 기능 구현.

```java
// SectionServiceTest.java

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
```

```java
// SectionService.java

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
}
```

구간 추가 기능 구현.

```java
// ValidationTest.java

package subway.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ValidationTest {
    @Test
    public void isNumeric() {
        assertThat(Validation.isNumeric("1")).isEqualTo(true);
        assertThat(Validation.isNumeric("")).isEqualTo(false);
        assertThat(Validation.isNumeric("number")).isEqualTo(false);
    }
}
```

```java
// Validation.java

package subway.util;

import java.util.regex.Pattern;

public final class Validation {
    private Validation() {
    }

    public static boolean isNumeric(String str) {
        return str != null && !str.isEmpty() && Pattern.matches("^[0-9]*$", str);
    }
}
```

숫자 형식의 문자열 확인 기능 구현.

```java
// SectionController.java

package subway.presentation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SectionControllerTest {
    private final SectionController sectionController = new SectionController();

    @Test
    public void addSection__InputEssentialDistanceException() {
        String lineName = "line";
        String sourceName = "source";
        String sinkName = "sink";
        String time = "0";
        String message = "거리 입력은 필수입니다.";
        assertThatThrownBy(
            () -> this.sectionController.addSection(lineName, sourceName, sinkName, null, time)).isInstanceOf(
            IllegalArgumentException.class).hasMessage(message);
        assertThatThrownBy(
            () -> this.sectionController.addSection(lineName, sourceName, sinkName, "", time)).isInstanceOf(
            IllegalArgumentException.class).hasMessage(message);
    }

    @Test
    public void addSection__OnlyPossibleNumericInputDistanceException() {
        String lineName = "line";
        String sourceName = "source";
        String sinkName = "sink";
        String time = "0";
        String message = "거리는 숫자 형식의 입력만 가능합니다.";
        assertThatThrownBy(
            () -> this.sectionController.addSection(lineName, sourceName, sinkName, "distance", time)).isInstanceOf(
            IllegalArgumentException.class).hasMessage(message);
    }

    @Test
    public void addSection__InputEssentialTimeException() {
        String lineName = "line";
        String sourceName = "source";
        String sinkName = "sink";
        String distance = "0";
        String message = "시간 입력은 필수입니다.";
        assertThatThrownBy(
            () -> this.sectionController.addSection(lineName, sourceName, sinkName, distance, null)).isInstanceOf(
            IllegalArgumentException.class).hasMessage(message);
        assertThatThrownBy(
            () -> this.sectionController.addSection(lineName, sourceName, sinkName, distance, "")).isInstanceOf(
            IllegalArgumentException.class).hasMessage(message);
    }

    @Test
    public void addSection__OnlyPossibleNumericInputTimeException() {
        String lineName = "line";
        String sourceName = "source";
        String sinkName = "sink";
        String distance = "0";
        String message = "시간은 숫자 형식의 입력만 가능합니다.";
        assertThatThrownBy(
            () -> this.sectionController.addSection(lineName, sourceName, sinkName, distance, "time")).isInstanceOf(
            IllegalArgumentException.class).hasMessage(message);
    }
}
```

```java
// SectionController.java

package subway.presentation;

import subway.application.section.dto.SectionDTO;
import subway.application.section.service.SectionService;
import subway.domain.line.LineDTO;
import subway.domain.station.StationDTO;
import subway.util.Validation;

public class SectionController {
    private static final String INPUT_ESSENTIAL_DISTANCE_MESSAGE = "거리 입력은 필수입니다.";
    private static final String ONLY_POSSIBLE_NUMERIC_INPUT_DISTANCE_MESSAGE = "거리는 숫자 형식의 입력만 가능합니다.";
    private static final String INPUT_ESSENTIAL_TIME_MESSAGE = "시간 입력은 필수입니다.";
    private static final String ONLY_POSSIBLE_NUMERIC_INPUT_TIME_MESSAGE = "시간은 숫자 형식의 입력만 가능합니다.";

    private final SectionService sectionService = new SectionService();

    public void addSection(String lineName, String sourceName, String sinkName, String _distance, String _time) {
        LineDTO lineDTO = new LineDTO(lineName);
        StationDTO sourceDTO = new StationDTO(sourceName);
        StationDTO sinkDTO = new StationDTO(sinkName);
        this.validateDistance(_distance);
        int distance = Integer.parseInt(_distance);
        this.validateTime(_time);
        int time = Integer.parseInt(_time);
        SectionDTO sectionDTO = new SectionDTO(lineDTO, sourceDTO, sinkDTO, distance, time);
        this.sectionService.addSection(sectionDTO);
    }

    private void validateDistance(String distance) {
        if(distance == null || distance.trim().isEmpty()) {
            throw new IllegalArgumentException(INPUT_ESSENTIAL_DISTANCE_MESSAGE);
        }
        if (!Validation.isNumeric(distance)) {
            throw new IllegalArgumentException(ONLY_POSSIBLE_NUMERIC_INPUT_DISTANCE_MESSAGE);
        }
    }

    private void validateTime(String time) {
        if(time == null || time.trim().isEmpty()) {
            throw new IllegalArgumentException(INPUT_ESSENTIAL_TIME_MESSAGE);
        }
        if (!Validation.isNumeric(time)) {
            throw new IllegalArgumentException(ONLY_POSSIBLE_NUMERIC_INPUT_TIME_MESSAGE);
        }
    }
}
```

제어 계층에 구간 추가 기능 매핑.





