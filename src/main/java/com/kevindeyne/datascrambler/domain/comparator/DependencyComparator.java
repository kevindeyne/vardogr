package com.kevindeyne.datascrambler.domain.comparator;

import com.kevindeyne.datascrambler.domain.Dependency;

import java.util.Comparator;

public class DependencyComparator implements Comparator<Dependency> {
    public int compare(Dependency c1, Dependency c2) {
        return new Integer(c1.getChildren().size()).compareTo(c2.getChildren().size());
    }
}