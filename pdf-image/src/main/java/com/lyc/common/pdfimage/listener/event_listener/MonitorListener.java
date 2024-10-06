package com.lyc.common.pdfimage.listener.event_listener;

import com.lyc.common.pdfimage.listener.event.AbstractEvent;

public interface MonitorListener<E extends AbstractEvent> {

    //响应事件
    void onEvent(E event);
}
