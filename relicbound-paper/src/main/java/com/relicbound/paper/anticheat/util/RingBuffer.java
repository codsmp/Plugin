package com.relicbound.paper.anticheat.util;

import java.util.ArrayList;
import java.util.List;

public final class RingBuffer<T> {
    private final Object[] values;
    private int cursor;
    private int size;

    public RingBuffer(int capacity) {
        this.values = new Object[Math.max(1, capacity)];
    }

    public void add(T value) {
        this.values[this.cursor] = value;
        this.cursor = (this.cursor + 1) % this.values.length;
        if (this.size < this.values.length) {
            this.size++;
        }
    }

    public int size() {
        return this.size;
    }

    @SuppressWarnings("unchecked")
    public T latest() {
        if (this.size == 0) {
            return null;
        }
        int index = this.cursor - 1;
        if (index < 0) {
            index += this.values.length;
        }
        return (T) this.values[index];
    }

    @SuppressWarnings("unchecked")
    public T getFromLatest(int offset) {
        if (offset < 0 || offset >= this.size) {
            return null;
        }
        int index = this.cursor - 1 - offset;
        while (index < 0) {
            index += this.values.length;
        }
        return (T) this.values[index];
    }

    @SuppressWarnings("unchecked")
    public List<T> snapshot() {
        List<T> copy = new ArrayList<>(this.size);
        for (int i = this.size - 1; i >= 0; i--) {
            int index = this.cursor - 1 - i;
            while (index < 0) {
                index += this.values.length;
            }
            copy.add((T) this.values[index]);
        }
        return copy;
    }
}