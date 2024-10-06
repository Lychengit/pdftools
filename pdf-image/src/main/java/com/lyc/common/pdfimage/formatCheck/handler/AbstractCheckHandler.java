package com.lyc.common.pdfimage.formatCheck.handler;

import com.lyc.common.pdfimage.formatCheck.handler.exception.CheckException;
import com.lyc.common.pdfimage.formatCheck.handler.impl.CheckHandlerDefault;
import org.slf4j.LoggerFactory;

/**
 * @description : pdf格式校验链
 *
 * @author : 		刘勇成
 * @date : 		2023/10/10 11:21
 *
 * @param
 * @return
 */
public abstract class AbstractCheckHandler<T> {
	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(AbstractCheckHandler.class);

	public static final String CHECK_ERR_MESSAGE = "当前文件内部格式有损坏，可使用浏览器打印另存为功能修复pdf后，再尝试重新上传";

	protected AbstractCheckHandler<T> delegate = null;

	// 各个校验节点的名称
	protected abstract String getServiceName();

	// 执行主要监控的业务
	public final void doHandler(T request) throws Throwable{

		if (request != null) {
			toDoHandler(request);
		}

		if (delegate != null) {
			delegate.doHandler(request);
		}
	}

	// 执行主要监控的业务
	protected abstract void toDoHandler(T request) throws Throwable;

	// 抛出异常提示
	protected void throwErrMsg(Integer code, String type, String message, String msgDetail, Throwable cause, Object data){
		if (message == null) {
			message = CHECK_ERR_MESSAGE;
		}
		throw new CheckException(code, type, message, msgDetail, cause, data);
	}

	// 每个校验节点的类型key值
	public abstract String getType();

	private void next(AbstractCheckHandler<T> handler) {
		this.delegate = handler;
	}

	public final static class Builder<T> {

		private AbstractCheckHandler<T> head;

		public Builder(AbstractCheckHandler<T> head) {
			if (head == null) {
				this.head = CheckHandlerDefault.getInstance();
			} else {
				this.head = head;
			}
		}

		public Builder() {
			this.head = CheckHandlerDefault.getInstance();
		}

		public Builder<T> perHandler(AbstractCheckHandler<T> handler) {
			handler.next(this.head);
			this.head = handler;
			return this;
		}

		public AbstractCheckHandler build() {
			return this.head;
		}
	}

}
