package com.lyc.common.pdfimage.formatCheck.handler.impl;

import com.lyc.common.pdfimage.formatCheck.handler.AbstractCheckHandler;
import com.lyc.common.pdfimage.formatCheck.model.ErrorCodes;
import com.lyc.common.pdfimage.formatCheck.model.PbRenderExceptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @description :
 *
 * @author : 		刘勇成
 * @date : 		2023/10/10 11:26
 *
 * @param
 * @return
 */
public class PbRenderInvalidBlockExHandler extends AbstractCheckHandler<PbRenderExceptionRequest> {

	protected static final Logger logger = LoggerFactory.getLogger(PbRenderInvalidBlockExHandler.class);
	public static final String TYPE = "InvalidBlockException";

	@Override
	protected String getServiceName() {
		return "无效的块类型";
	}

	@Override
	protected void toDoHandler(PbRenderExceptionRequest request) throws Throwable {
		Throwable throwable = request.getBody();
		if (throwable == null) {
			return;
		}

		if (throwable instanceof IOException
				&& throwable.getMessage() != null && throwable.getMessage().contains("java.util.zip.DataFormatException: invalid block type")) {
			log.error("{}, 发现异常点，存在无效的块类型数据", getServiceName());
			throwErrMsg(ErrorCodes.PB_RENDER_ERR.getCode(), "", ErrorCodes.PB_RENDER_ERR.getDesc(), "存在无效的块类型数据, "+ ErrorCodes.PB_RENDER_ERR.getDesc(), null, null);
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
