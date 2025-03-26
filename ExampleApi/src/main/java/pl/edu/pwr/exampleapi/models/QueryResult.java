package pl.edu.pwr.exampleapi.models;

import java.util.Collection;


public record QueryResult<T>(Collection<T> items, Long totalItemCount) {
}
