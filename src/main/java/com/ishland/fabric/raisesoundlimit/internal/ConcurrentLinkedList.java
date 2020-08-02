package com.ishland.fabric.raisesoundlimit.internal;

import com.google.common.annotations.Beta;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;

@Beta
public class ConcurrentLinkedList<E> extends ConcurrentLinkedQueue<E> implements List<E> {

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public boolean addAll(int i, Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public E get(int i) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public E set(int i, E e) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public void add(int i, E e) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public E remove(int i) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public ListIterator<E> listIterator(int i) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented and not needed to be implemented
     *
     * @return nothing
     * @throws UnsupportedOperationException anytime
     */
    @Override
    public List<E> subList(int i, int i1) {
        throw new UnsupportedOperationException();
    }
}
