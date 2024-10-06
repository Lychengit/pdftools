package org.apache.pdfbox.pdmodel.font;

import com.lyc.common.pdfimage.model.ToPdfTask;
import com.lyc.common.pdfimage.utils.FileUtils;
import com.lyc.common.pdfimage.utils.unsafe.UnsafeAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : 		刘勇成
 * @description : pdfbox字体相关类
 * @date : 		2023/10/19 16:41
 */
public class QysFontMapperImpl {
	private static final Log logger = LogFactory.getLog(QysFontMapperImpl.class);

	public final static String PDFBOX = "PDFBOX";
	private static final ReentrantLock lock = new ReentrantLock();
	private static final ReentrantLock lockSubstitutes = new ReentrantLock();

	// pdfbox默认使用的字体相关实例
	private static FontMapperImpl fontMapper = null;
	// pdfbox默认使用的保底字体文件
	private static volatile TrueTypeFont pdfboxDefaultLastResortFont = null;
	// 自定义的保底字体
	public static volatile String ttfNameLastResortFont = PDFBOX;

	// pdfbox默认的字体别名映射关系
	private static Map<String, List<String>> pdfboxDefaultSubstitutes = null;
	// 自定义的字体别名映射关系
	public static volatile String pdfboxFontSubstitutesStr = null;

	private static long FONTMAPPERIMPL_LASTRESORTFONT_OFFSET;
	private static long FONTMAPPERIMPL_SUBSTITUTES_OFFSET;

	static {
		try {
			FontMapper instance = FontMappers.instance();
			if (instance instanceof FontMapperImpl){
				logger.info("获取到pdfbox的FontMapperImpl字体关系实例");
				fontMapper = (FontMapperImpl) instance;
			} else {
				logger.warn("FontMappers.instance不是FontMapperImpl类型的，如无法自定义pdfbox保底字体文件");
			}
			FONTMAPPERIMPL_LASTRESORTFONT_OFFSET = UnsafeAccess.fieldOffset(FontMapperImpl.class, "lastResortFont");
			FONTMAPPERIMPL_SUBSTITUTES_OFFSET = UnsafeAccess.fieldOffset(FontMapperImpl.class, "substitutes");
		} catch (Throwable ex) {
			logger.error("当前类初始化异常信息：", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	private static TrueTypeFont getLastResortFontUnsafe(FontMapperImpl fontMapper){
		return (TrueTypeFont) UnsafeAccess.UNSAFE.getObjectVolatile(fontMapper, FONTMAPPERIMPL_LASTRESORTFONT_OFFSET);
	}

	private static void setLastResortFontUnsafe(FontMapperImpl fontMapper, TrueTypeFont lastResortFont){
		UnsafeAccess.UNSAFE.putObjectVolatile(fontMapper, FONTMAPPERIMPL_LASTRESORTFONT_OFFSET, lastResortFont);
	}

	private static Map<String, List<String>> getSubstitutes(FontMapperImpl fontMapper){
		return (Map<String, List<String>>) UnsafeAccess.UNSAFE.getObjectVolatile(fontMapper, FONTMAPPERIMPL_SUBSTITUTES_OFFSET);
	}

	// 校验指定的字体文件
	public static boolean checkFont(String pbLastResortFont){
		try {
			if (StringUtils.isBlank(pbLastResortFont)) {
				return false;
			}
			if (QysFontMapperImpl.PDFBOX.equalsIgnoreCase(pbLastResortFont) || FileUtils.isValidFile(new File(pbLastResortFont))) {
				return true;
			}
			return false;
		} catch (Throwable e) {
			logger.info("校验pdfbox字体文件相关参数出现异常", e);
			return false;
		}

	}

	/**
	 * 设置和覆盖保底字体文件（就算设置异常也不能将错误抛出，可继续使用pdfbox默认的保底字体文件）
	 * @param ttfName
	 * @param toPdfTask
	 */
	public static void setLastResortFontSyn(String ttfName, ToPdfTask toPdfTask) {
		try {
			if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
				try {
					if (setLastResortFont(ttfName)) {
						ttfNameLastResortFont = ttfName;
					}
				} finally {
					lock.unlock();
				}
			}
		} catch (Throwable e) {
			logger.error("设置pdfbox保底字体文件异常，建议引用ttf类型的字体文件！字体文件路径：" + ttfName, e);
		}
	}

	/**
	 * 设置和覆盖保底字体文件
	 * @param ttfName
	 */
	private static boolean setLastResortFont(String ttfName){
		try {
			if (fontMapper == null || StringUtils.isBlank(ttfName)) {
				return false;
			}
			if (pdfboxDefaultLastResortFont == null) {
				pdfboxDefaultLastResortFont = getLastResortFontUnsafe(fontMapper);
			}
			if (PDFBOX.equalsIgnoreCase(ttfName) && pdfboxDefaultLastResortFont != null) {
				setLastResortFontUnsafe(fontMapper, pdfboxDefaultLastResortFont);
				return true;
			}
			try (InputStream resourceAsStream = new FileInputStream(new File(ttfName));
				 RandomAccessReadBuffer randomAccessReadBuffer = new RandomAccessReadBuffer(resourceAsStream);) {
				TTFParser ttfParser = new TTFParser();
				TrueTypeFont lastResortFont = ttfParser.parse(randomAccessReadBuffer);
				setLastResortFontUnsafe(fontMapper, lastResortFont);
				return true;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * 新增一个字体名的一个匹配项（不能替换和覆盖原来加的值）
	 * @param match
	 * @param replace
	 */
	@Deprecated
	public static boolean addSubstitute(String match, String replace, ToPdfTask toPdfTask) {
		if (fontMapper != null){
			fontMapper.addSubstitute(match, replace);
			return true;
		}
		return false;
	}

	/**
	 *
	 * 新增多个字体别名和真实字体名的匹配项（新增或覆盖原来加的值）
	 */
	public static void replaceSubstituteMapSyn(Map<String, List<String>> substituteMap, ToPdfFontTask toPdfFontTask) {
		try {
			if (fontMapper == null){
				return;
			}
			if (lockSubstitutes.tryLock(1000, TimeUnit.MILLISECONDS)) {
				try {
					replaceSubstituteMap(substituteMap, toPdfFontTask);
					pdfboxFontSubstitutesStr = toPdfFontTask == null ? null : toPdfFontTask.getPdfboxFontSubstitutesStr();
					Map<String, List<String>> substitutes = getSubstitutes(fontMapper);
					logger.info("当前pdfbox字体别名与真实字体名称的映射关系："+ (substitutes != null ? substitutes.toString(): null));
				} finally {
					lockSubstitutes.unlock();
				}
			}
		} catch (Throwable e) {
			logger.error("添加pdfbox字体别名与真实字体名称的映射关系异常，传入的集合信息：" + (substituteMap != null ? substituteMap.toString() : null), e);
		}
	}

	/**
	 * 传入映射关系集合，根据key替换value值
	 */
	private static void replaceSubstituteMap(Map<String, List<String>> substituteMap, ToPdfFontTask toPdfFontTask) {
		if (fontMapper == null) {
			return;
		}

		Map<String, List<String>> substitutes = getSubstitutes(fontMapper);

		// 记录pdfbox默认初始化的字体别名和真实字体名的关联关系
		if (pdfboxDefaultSubstitutes == null) {
			if (substitutes != null) {
				pdfboxDefaultSubstitutes = new HashMap<String, List<String>>();
				pdfboxDefaultSubstitutes.putAll(substitutes);
			}
		}
		// 重置默认映射关系
		if (toPdfFontTask != null && PDFBOX.equals(toPdfFontTask.getPdfboxFontSubstitutesStr()) && pdfboxDefaultSubstitutes != null) {
			for (Map.Entry <String, List<String>> entry: pdfboxDefaultSubstitutes.entrySet()) {
				String key = entry.getKey();
				List<String> value = entry.getValue();
				replaceSubstitute(key, value, substitutes, toPdfFontTask);
			}
		} else {
			if (substituteMap != null) {
				// 新增和替换映射关系
				for (Map.Entry<String, List<String>> entry : substituteMap.entrySet()) {
					if (entry != null) {
						String key = entry.getKey();
						List<String> value = entry.getValue();
						replaceSubstitute(key, value, substitutes, toPdfFontTask);
					}
				}
			}
		}
	}

	/**
	 *
	 * 新增一个字体别名和真实字体名的一个匹配项（新增或覆盖原来加的值）
	 * @param match
	 * @param replacements
	 */
	private static void replaceSubstitute(String match, List<String> replacements, Map<String, List<String>> substitutes, ToPdfFontTask toPdfFontTask) {
		if (match == null || substitutes == null) {
			return;
		}

		substitutes.put(match.toLowerCase(Locale.ENGLISH), replacements);
	}

}