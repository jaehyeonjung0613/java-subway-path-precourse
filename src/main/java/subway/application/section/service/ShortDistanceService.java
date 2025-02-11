package subway.application.section.service;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import subway.domain.section.Section;

class ShortDistanceService extends ShortCostService {
    public ShortDistanceService() {
        super(new DirectedWeightedMultigraph<>(
            DefaultWeightedEdge.class));
    }

    @Override
    protected double getWeight(Section section) {
        return section.getDistance();
    }
}
