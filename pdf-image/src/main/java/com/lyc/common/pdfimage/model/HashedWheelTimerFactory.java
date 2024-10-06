package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.utils.ShardingUtils;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : 		刘勇成
 * @description : 时间轮工厂
 * @date : 		2023/3/2 14:29
 */
public class HashedWheelTimerFactory {

	private static final Logger log = LoggerFactory.getLogger(HashedWheelTimerFactory.class);
	private final String msg;
	// 延时监控配置
	private final WTConfig WT_CONFIG = new WTConfig();
	private volatile List<HashedWheelTimer> timerList;

	public HashedWheelTimerFactory(String msg) {
		this.msg = msg;
		timerList = buildHashedWheelTimer(WT_CONFIG);
	}

	private List<HashedWheelTimer> buildHashedWheelTimer(WTConfig config) {
		List<HashedWheelTimer> timerList = new ArrayList<>();
		int wTTimerNum = Math.max(1,
				config.getwTTimerNum());
		long wTmaxPendingTimeouts = Math.max(1,
				config.getwTmaxPendingTimeouts());
		for (int i = 0; i < wTTimerNum; i++) {
			timerList.add(new HashedWheelTimer(new DefaultThreadFactory("wheelTimer监控-"+ this.msg+ "-thread-%d"),
					config.getwTTickDuration(), config.getwTUnit(), config.getwTticksPerWheel(),
					config.iswTleakDetection(), wTmaxPendingTimeouts));
		}

		log.info("构建【{}】延时监控器，延时监控器的个数【{}】，队列的长度【{}】", msg, wTTimerNum, wTmaxPendingTimeouts);
		return timerList;
	}

	public String getMsg() {
		return msg;
	}

	public WTConfig getWT_CONFIG() {
		return WT_CONFIG;
	}

	public void rebuildHashedWheelTimer() {
		timerList = buildHashedWheelTimer(WT_CONFIG);
	}

	public List<HashedWheelTimer> getHashedWheelTimer(){
		return timerList;
	}

	public HashedWheelTimer selectHashedWheelTimer(List<HashedWheelTimer> timerList, String key){
		int hashMod = ShardingUtils.calculationHashMod(key, timerList.size());
		return timerList.get(hashMod);
	}

	public HashedWheelTimer selectHashedWheelTimer(String key){
		if (timerList.size() == 1){
			return timerList.get(0);
		}
		int hashMod = ShardingUtils.calculationHashMod(key, timerList.size());
		return timerList.get(hashMod);
	}

}
