package com.keyman.watcher.parser;

public class ObjectHolder<L, R> {
    private L left;
    private R right;

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


    public void putLeft(L left) {
        this.left = left;
    }

    public void putRight(R right) {
         this.right = right;
    }

    public static <L, R> ObjectHolder<L, R> of(L left, R right) {
        return new ObjectHolder<>(left, right);
    }
}
