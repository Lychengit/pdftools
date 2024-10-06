package com.lyc.common.pdfimage.formatCheck.handler.impl;

import com.lyc.common.pdfimage.formatCheck.handler.AbstractCheckHandler;
import com.lyc.common.pdfimage.formatCheck.model.PbRenderExceptionRequest;

/**
 * @description :
 *
 * @author : 		刘勇成
 * @date : 		2023/10/10 11:27
 *
 * @param
 * @return
 */
public class CheckHandlerDefault extends AbstractCheckHandler<PbRenderExceptionRequest> {

	private static final CheckHandlerDefault INSTANCE = new CheckHandlerDefault();

	public static AbstractCheckHandler getInstance() {
		return INSTANCE;
	}

	@Override
	protected String getServiceName() {
		return "pdf格式校验链默认实例";
	}

	@Override
	protected void toDoHandler(PbRenderExceptionRequest request) throws Throwable {

	}

	@Override
	public String getType() {
		return null;
	}

}
