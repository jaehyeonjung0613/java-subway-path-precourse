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
        this.lineService.deleteAll();
    }

    @Test
    public void addLine__AlreadyExistsLineException() {
        LineDTO lineDTO = new LineDTO("test");
        String message = "이미 등록되어있는 노선입니다.";
        this.lineService.addLine(lineDTO);
        assertThatThrownBy(() -> this.lineService.addLine(lineDTO)).isInstanceOf(IllegalArgumentException.class)
            .hasMessage(message);
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
        this.lineService.deleteAll();
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