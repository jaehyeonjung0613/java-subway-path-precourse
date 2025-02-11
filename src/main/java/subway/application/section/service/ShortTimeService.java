package subway.application.section.service;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import subway.domain.section.Section;

class ShortTimeService extends ShortCostService {
    public ShortTimeService() {
        super(new DirectedWeightedMultigraph<>(
            DefaultWeightedEdge.class));
    }

    @Override
    protected double getWeight(Section section) {
        return section.getTime();
    }
}
