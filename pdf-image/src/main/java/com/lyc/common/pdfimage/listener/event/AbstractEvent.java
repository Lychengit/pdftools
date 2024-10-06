package com.lyc.common.pdfimage.listener.event;

public abstract class AbstractEvent<T> {

    //事件源
    protected T source;

    public T getSource() {
        return source;
    }

    public void setSource(T source) {
        this.source = source;
    }

    public AbstractEvent(T source) {
        this.source = source;
    }
}
