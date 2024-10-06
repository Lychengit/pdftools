# pdftools
pdf相关工具类，如pdf转图片等。


# pdf-image
支持pdf转图片。

自动管理缓存和过期清除pdf的转换实例，实现了增量转换的功能。用户只需简单的传入：唯一标识符、指定的页码、pdf文件回调实现，即可得到图片数据。
且本项目处理过很多内存占用等性能问题。

一些静态参数配置，可自定义调控一些转换相关的操作，详情可查看：
com.lyc.common.pdfimage.model.PbConvertParamConfig
com.lyc.common.pdfimage.PdfBoxConvertorProvider.IMAGE_SCALE_MODEL


代码示例：
```
com.lyc.common.pdfimage.TestW

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
```
