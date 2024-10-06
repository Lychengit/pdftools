package com.lyc.common.pdfimage.listener.broadcaster.impl;

import com.lyc.common.pdfimage.listener.broadcaster.EventBroadCaster;
import com.lyc.common.pdfimage.listener.event.AbstractEvent;
import com.lyc.common.pdfimage.listener.event_listener.MonitorListener;

import java.util.Vector;

/**
 * @description :
 * 定时轮询广播（每10s广播一次，可提前唤醒广播，可用于实现自定义参数的变动监控）
 * @author : 		刘勇成
 * @date : 		2022/12/16 16:04
 *
 * @param
 * @return
 */
public class SimpleEventBroadCasterImpl implements EventBroadCaster {

    private Vector<MonitorListener> eventObjectEventListeners = new Vector<>();

    @Override
    public void broadCastEvent(AbstractEvent event) {

        for (MonitorListener eventListener : this.eventObjectEventListeners) {

            eventListener.onEvent(event);
        }

    }

    @Override
    public void addEventListener(MonitorListener<?> listener) {
        this.eventObjectEventListeners.add(listener);
    }

    @Override
    public void removeEventListener(MonitorListener<?> listener) {
        this.eventObjectEventListeners.remove(listener);
    }
}
