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