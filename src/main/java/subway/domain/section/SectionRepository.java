package subway.domain.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SectionRepository {
    private static final List<Section> sections = new ArrayList<>();

    public static List<Section> sections() {
        return Collections.unmodifiableList(sections);
    }

    public static void addSection(Section section) {
        sections.add(section);
    }

    public static void deleteAll() {
        sections.clear();
    }

    public static boolean exists(Section other) {
        return sections.stream()
            .anyMatch(section -> section.getLine() == other.getLine() && section.getSource() == other.getSource()
                && section.getSink() == other.getSink());
    }
}
