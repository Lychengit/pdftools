package com.lyc.common.pdfimage;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.lyc.common.pdfimage.model.PdfRendererTask;
import com.lyc.common.pdfimage.service.PdfRendererCallable;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * @author : 		刘勇成
 * @description :
 * @date : 		2024/5/31 17:34
 */
public class TestW {

	static {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<Logger> loggerList = loggerContext.getLoggerList();
		loggerList.forEach(logger -> {
			logger.setLevel(Level.DEBUG);
		});
	}

	public static void main(String[] args) throws Throwable {

		for (int i = 0; i < 800; i++) {
			int pageIndex = i;
			final BufferedImage bufferedImage = PdfBoxConvertorProvider.loadBalancingToImage("唯一标识符123", pageIndex, new PdfRendererTask(new PdfRendererCallable() {
				@Override
				public File initPdfRenderer(PdfRendererTask task) {
					File file = new File("C:\\Users\\LYCIT\\Downloads\\设计模式之禅（第2版） (秦小波) (z-lib.org).pdf");
					return file;
				}
			}));

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File("D:\\fcs\\test/test6/image" + pageIndex + ".jpg"));
				ImageIO.write(bufferedImage, "jpg", fos);
				System.out.println();
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}
	}
}
