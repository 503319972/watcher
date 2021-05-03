package com.keyman.watcher.parser;

public class ObjectHolder<L, R> {
    private final L left;
    private final R right;

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    private ObjectHolder(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> ObjectHolder<L, R> of(L left, R right) {
        return new ObjectHolder<>(left, right);
    }
}
