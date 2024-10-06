package com.lyc.common.pdfimage.formatCheck.handler.impl;

import com.lyc.common.pdfimage.formatCheck.handler.AbstractCheckHandler;
import com.lyc.common.pdfimage.formatCheck.model.ErrorCodes;
import com.lyc.common.pdfimage.formatCheck.model.PbRenderExceptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description :
 *
 * @author : 		刘勇成
 * @date : 		2023/10/10 11:26
 *
 * @param
 * @return
 */
public class PbRenderBandsComponentsExHandler extends AbstractCheckHandler<PbRenderExceptionRequest> {

	protected static final Logger logger = LoggerFactory.getLogger(PbRenderBandsComponentsExHandler.class);
	public static final String TYPE = "BandsComponentsNotMatchException";

	@Override
	protected String getServiceName() {
		return "源栅格波段和源色彩空间分量的数量不匹配";
	}

	@Override
	protected void toDoHandler(PbRenderExceptionRequest request) throws Throwable {
		Throwable throwable = request.getBody();
		if (throwable == null) {
			return;
		}

		if (throwable instanceof IllegalArgumentException
				&& throwable.getMessage() != null && throwable.getMessage().contains("Numbers of source Raster bands and source color space components do not match")) {
			log.error("{}, 发现异常点，该文件源栅格波段和源色彩空间分量的数量不匹配", getServiceName());
			throwErrMsg(ErrorCodes.PB_RENDER_ERR.getCode(), "", ErrorCodes.PB_RENDER_ERR.getDesc(), "源栅格波段和源色彩空间分量的数量不匹配, "+ ErrorCodes.PB_RENDER_ERR.getDesc(), null, null);
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
