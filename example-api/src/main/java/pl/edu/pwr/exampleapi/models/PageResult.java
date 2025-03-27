package pl.edu.pwr.exampleapi.models;

import lombok.Getter;

import java.util.Collection;

@Getter
public class PageResult<T> {
    private final Collection<T> items;
    private final int totalPages;
    private final int itemsFrom;
    private final int itemsTo;
    private final int totalItemCount;

    public PageResult(Collection<T> items, int totalItemCount, int pageSize, int pageNumber) {
        this.items = items;
        this.totalItemCount = totalItemCount;
        this.itemsFrom = pageSize * (pageNumber - 1) + 1;
        this.itemsTo = this.itemsFrom + pageSize - 1;
        this.totalPages = (int) Math.ceil(this.totalItemCount / (double) pageSize);
    }
}
