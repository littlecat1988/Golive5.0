package com.golive.cinema.films;

/**
 * Used with the filter spinner in the films list.
 */
public enum FilmsFilterType {
    /**
     * Do not filter films.
     */
    ALL_FILMS,

    /**
     * Filters only the on now films.
     */
    ON_NOW_FILMS,

    /**
     * Filters only the upcoming films.
     */
    UPCOMING_TASKS,

    /**
     * Filters only the upcoming films.
     */
    ENDING_TASKS
}