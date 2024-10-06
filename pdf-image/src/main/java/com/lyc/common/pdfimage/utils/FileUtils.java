package com.lyc.common.pdfimage.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * @author : 		刘勇成
 * @description :
 * @date : 		2023/3/23 16:55
 */
public class FileUtils {

	private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

	public static void delete(File file, String msg) throws Throwable {
		if (file != null && file.exists()) {
			Path path = file.toPath();
			log.info("尝试删除【{}】文件，文件存在路径：【{}】", msg, path);
			try {
				if (StringUtils.isNotBlank(path.toString())) {
					Files.delete(path);
				}
			} catch (Throwable e) {
				if (e instanceof NoSuchFileException) {
					log.error("关闭【"+ msg+"】file异常，删除的文件未找到，文件存在路径：" + path, e);
				} else if (e instanceof DirectoryNotEmptyException) {
					log.error("关闭【"+ msg+"】file异常，删除的文件为什么是文件夹？而且非空的文件夹，文件存在路径：" + path, e);
				} else if (e instanceof SecurityException) {
					log.error("关闭【"+ msg+"】file异常，存在安全违规，文件存在路径：" + path, e);
				} else {
					log.error("关闭【"+ msg+"}】file异常，尝试延续当前文件缓存的有效期，文件存在路径：" + path, e);
				}
				throw e;
			}
		}
	}

	public static boolean isValidFile(File file){
		if (!file.exists()){
			log.error("文件：【{}】不存在，属于无效文件", file.toPath());
			return false;
		}
		if (!file.isFile()){
			log.error("文件：【{}】不是文件类型的，属于无效文件", file.toPath());
			return false;
		}
		if (file.length() <= 0){
			log.error("文件：【{}】长度小于等于0，属于无效文件", file.toPath());
			return false;
		}
		return true;
	}

	public static String getTempDir(String child) {
		String separator = File.separator;
		String tempDir = System.getProperty("java.io.tmpdir") + separator + "pdftools" + separator + "temp" + separator;
		if (StringUtils.isNotBlank(child)) {
			tempDir += child + separator;
		}
		File tempFile = new File(tempDir);
		if (!tempFile.exists()) {
			boolean mkdirs = tempFile.mkdirs();
			if (!mkdirs) {
				log.error("文件目录创建失败：{}", tempDir);
			}
		}
		return tempDir;
	}
}
