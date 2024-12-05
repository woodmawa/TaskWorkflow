package org.softwood.basics

/**
 *
 * @param <T>
 *
 *     does work got from a queue and executes the
 */
interface Worker<T> {
    T execute ()
}