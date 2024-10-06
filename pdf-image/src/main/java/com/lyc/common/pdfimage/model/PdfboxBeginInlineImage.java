package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.PdfBoxConvertorProvider;
import com.lyc.common.pdfimage.utils.FileUtils;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.graphics.BeginInlineImage;
import org.apache.pdfbox.contentstream.operator.graphics.GraphicsOperatorProcessor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @description : BI Begins an inline image.
 * 因pdfbox针对内联图片在遇到游程编码算法时解压出来的图片较大时内存占用异常 导致OOM的问题。
 * 这里基于临时文件的方式解决内存占用的问题。
 * @author : 		刘勇成
 * @date : 		2023/5/4 17:24
 *
 * @param
 * @return
 */
public final class PdfboxBeginInlineImage extends GraphicsOperatorProcessor
{

    protected static Logger logger = LoggerFactory.getLogger(PdfboxBeginInlineImage.class);

    private final BeginInlineImage beginInlineImage;
    private final String scratchFileDirectory;
    private final String previewid;

    public PdfboxBeginInlineImage(PDFGraphicsStreamEngine context, BeginInlineImage beginInlineImage, String scratchFileDirectory, String previewid) {
        super(context);
        this.beginInlineImage = beginInlineImage;
        this.scratchFileDirectory = scratchFileDirectory;
        this.previewid = previewid;
    }

    @Override
    public void process(Operator operator, List<COSBase> operands) throws IOException
    {
        if (operator.getImageData() == null || operator.getImageData().length == 0)
        {
            return;
        }
        byte[] imageData = operator.getImageData();
        int inlineImageTmpMaxSize = PdfBoxConvertorProvider.CONVERT_PARAM_CONFIG.getInlineImageTmpMaxSize();
        int sourceInlineImagelength = imageData.length;
        // Because the inline format gives the reader less flexibility in managing the image data, it shall be used only for small images (4 KB or less).
        // 默认大于4kb的使用临时文件的方式减少内存占用，否则还是继续使用pdfbox的现有逻辑处理（大内联图片）。
        if (sourceInlineImagelength > inlineImageTmpMaxSize) {
            logger.info("预览id【{}】，当前内联图片的大小超过【{}】字节, 大小【{}】字节，将以临时文件的方式处理！", this.previewid, inlineImageTmpMaxSize, sourceInlineImagelength);
            COSDictionary imageParameters = operator.getImageParameters();
            List<String> filters = PdfboxPDInlineImage.getFilters(imageParameters);
            // 游程编码算法反解压后容易溢出，这里如果没有使用该算法，否则还是继续使用pdfbox的现有逻辑处理（存在游程编码算法）。
            if (filters != null && !filters.isEmpty()
                    && isRunLengthDecodeFilter(filters)) {
                File fileSource = null;
                File fileDest = null;
                try {
                    fileSource = createFile(imageData, this.scratchFileDirectory, "PDFBoxFileSource");
                    fileDest = createFile(null, this.scratchFileDirectory, "PDFBoxFileDest");
                    logger.info("预览id【{}】，当前内联图片存在游程编码算法，构建的临时文件，源文件临时文件存储路径：【{}】，目标文件临时文件存储路径：【{}】", this.previewid, fileSource.getAbsolutePath(), fileDest.getAbsolutePath());
                    PDFGraphicsStreamEngine context = getGraphicsContext();
                    PDImage image = new PdfboxPDInlineImage(imageParameters, imageData, context.getResources(),
                            fileSource, fileDest, filters);
                    context.drawImage(image);
                } finally {
                    try {
                        if (fileSource != null) {
                            try {
                                FileUtils.delete(fileSource, "Pdfbox内联图片临时文件（原），预览id【"+ this.previewid+ "】");
                                logger.info("预览id【{}】，当前内联图片转换结束，删除构建的临时文件，源文件临时文件存储路径：【{}】", this.previewid, fileSource.getAbsolutePath());
                            } catch (Throwable throwable) {
                                logger.error("预览id【"+ this.previewid+ "】，当前内联图片转换结束，删除构建的临时文件异常，源文件临时文件存储路径：【"+ fileSource.getAbsolutePath()+ "】", throwable);
                            }
                        }
                    } finally {
                        if (fileDest != null) {
                            try {
                                FileUtils.delete(fileDest, "Pdfbox内联图片临时文件（目标），预览id【"+ this.previewid+ "】");
                                logger.info("预览id【{}】，当前内联图片转换结束，删除构建的临时文件，目标文件临时文件存储路径：【{}】", this.previewid, fileDest.getAbsolutePath());
                            } catch (Throwable throwable) {
                                logger.error("预览id【"+ this.previewid+ "】，当前内联图片转换结束，删除构建的临时文件异常，源文件临时文件存储路径：【"+ fileDest.getAbsolutePath()+ "】", throwable);
                            }
                        }
                    }
                }
                return;
            }
        }

		this.beginInlineImage.process(operator, operands);
    }

    // 判断是否存在游程编码算法
    private boolean isRunLengthDecodeFilter(List<String> filters){
        for (int i = 0; i < filters.size(); i++) {
            String filterStr = filters.get(i);
            if (COSName.RUN_LENGTH_DECODE_ABBREVIATION.getName().equals(filterStr) ||
                    COSName.RUN_LENGTH_DECODE.getName().equals(filterStr)){
                return true;
            }
        }
        return false;
    }

    public File createFile(byte[] bfile, String directory, String prefix) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(directory);
            if(!dir.exists() && !dir.isDirectory()){
                dir.mkdirs();
            }
            //创建临时文件的api参数 (文件前缀,文件后缀,存放目录)
            file = File.createTempFile(prefix+ UUID.randomUUID().toString(), ".tmp", dir);
            if (bfile != null && bfile.length > 0) {
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                bos.write(bfile);
            }
            return file;
        } catch (Throwable e) {
            throw new RuntimeException("字节数组转文件异常", e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (Throwable e) {
                    logger.error("字节数组转文件输入流异常", e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable e) {
                    logger.error("字节数组转文件输出流异常", e);
                }
            }
        }
    }

    @Override
    public String getName()
    {
        return this.beginInlineImage.getName();
    }
}
