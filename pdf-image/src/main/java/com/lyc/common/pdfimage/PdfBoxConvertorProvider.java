package com.lyc.common.pdfimage;

import com.lyc.common.pdfimage.exception.ConvertorException;
import com.lyc.common.pdfimage.exception.PdfConvertException;
import com.lyc.common.pdfimage.exception.RendererNotValidException;
import com.lyc.common.pdfimage.formatCheck.handler.AbstractCheckHandler;
import com.lyc.common.pdfimage.formatCheck.handler.exception.CheckException;
import com.lyc.common.pdfimage.formatCheck.handler.impl.PbRenderBandsComponentsExHandler;
import com.lyc.common.pdfimage.formatCheck.handler.impl.PbRenderExceptionHandlerDefault;
import com.lyc.common.pdfimage.formatCheck.handler.impl.PbRenderInvalidBlockExHandler;
import com.lyc.common.pdfimage.formatCheck.model.PbRenderExceptionRequest;
import com.lyc.common.pdfimage.model.CachePdfboxRenderer;
import com.lyc.common.pdfimage.model.CachePdfboxRendererGroup;
import com.lyc.common.pdfimage.model.EnumConvertModel;
import com.lyc.common.pdfimage.model.HashedWheelTimerFactory;
import com.lyc.common.pdfimage.model.ImageSubsamplingSelector;
import com.lyc.common.pdfimage.model.MonitorResponse;
import com.lyc.common.pdfimage.model.PbCOSNameMapCleanWorker;
import com.lyc.common.pdfimage.model.PbConvertParamConfig;
import com.lyc.common.pdfimage.model.PdfRendererTask;
import com.lyc.common.pdfimage.model.PdfboxConvertMessage;
import com.lyc.common.pdfimage.model.PdfboxConvertRequest;
import com.lyc.common.pdfimage.model.PdfboxGroupLimit;
import com.lyc.common.pdfimage.model.PdfboxGroupWorker;
import com.lyc.common.pdfimage.model.PdfboxLimitWorker;
import com.lyc.common.pdfimage.model.PdfboxLoadBalanceFactory;
import com.lyc.common.pdfimage.model.PdfboxRendererExpireRequest;
import com.lyc.common.pdfimage.model.PdfboxResourceCache;
import com.lyc.common.pdfimage.model.ReentrantLockEntry;
import com.lyc.common.pdfimage.model.SingletonExecutor;
import com.lyc.common.pdfimage.model.SingletonExecutorFactory;
import com.lyc.common.pdfimage.model.WeakHashLockEntry;
import com.lyc.common.pdfimage.service.PdfRendererCallable;
import com.lyc.common.pdfimage.service.PdfboxAbstractLoadBalance;
import com.lyc.common.pdfimage.utils.FileUtils;
import com.lyc.common.pdfimage.utils.ImageScaleScalableUtil;
import com.lyc.common.pdfimage.utils.MonitorPDDocument;
import com.lyc.common.pdfimage.utils.unsafe.PdfBoxUnsafeUtils;
import com.lyc.common.pdfimage.utils.weakmap.MyConcurrentReferenceHashMap;
import io.netty.util.HashedWheelTimer;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.ResourceCache;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * pdfbox转换（加入线程池做削峰处理）相关业务层接口
 */
public class PdfBoxConvertorProvider {

	protected static Logger logger = LoggerFactory.getLogger(PdfBoxConvertorProvider.class);

	/**
	 * pdfbox转换动态获取清晰度相关的
	 */
	public static float STANDARD_SCALE = 2.0f;
	public static int BASE_SIZE_MIN = 1000 * 1000;
	public static int BASE_SIZE_MAX = 2700 * 2700;
	public static String START_SCALE = "2";
	public static String DECREASE_VALUE = "0.025";
	public static int SCALE_NUM = 80;
	public static final ImageScaleScalableUtil.ImageScaleModel IMAGE_SCALE_MODEL =
			new ImageScaleScalableUtil.ImageScaleModel(STANDARD_SCALE, BASE_SIZE_MIN, BASE_SIZE_MAX, new ImageScaleScalableUtil.ImageScaleSelector(START_SCALE, DECREASE_VALUE, SCALE_NUM, BASE_SIZE_MAX));
	public static final ImageSubsamplingSelector IMAGE_SUBSAMPLING_SELECTOR = new ImageSubsamplingSelector();

	// 当前锁用于保证一个预览id的源文件只有一个线程下载
	public static final WeakHashLockEntry<String> LOCK_ROOT_FILE = new WeakHashLockEntry<>();
	// 当前锁用于保证一个预览id只加载一个转换实例
	public static final WeakHashLockEntry<String> LOCK = new WeakHashLockEntry<>();
	// 缓存pdfbox转换实例组
	public static final MyConcurrentReferenceHashMap<String, CachePdfboxRendererGroup> TASK_MAP = new MyConcurrentReferenceHashMap<>();
	public static final ConcurrentHashMap<CachePdfboxRenderer, CachePdfboxRendererGroup> RENDERER_MAP = new ConcurrentHashMap<>();

	// 转换相关参数配置
	public static final PbConvertParamConfig CONVERT_PARAM_CONFIG = new PbConvertParamConfig();
	public static final HashedWheelTimerFactory HASHED_WHEEL_TIMER_FACTORY = new HashedWheelTimerFactory("pdfbox转换相关的");

	public static final SingletonExecutorFactory SINGLETON_EXECUTOR_FACTORY = new SingletonExecutorFactory("pdfbox转换线程池");

	// pdf加载异常校验链
	public static final AbstractCheckHandler EXCEPTION_BUILD = new AbstractCheckHandler.Builder<>(new PbRenderExceptionHandlerDefault())
			.perHandler(new PbRenderInvalidBlockExHandler()).perHandler(new PbRenderBandsComponentsExHandler()).build();

	public static final PdfboxGroupWorker WORKER;
	static {
		try {
			WORKER = new PdfboxGroupWorker(60000, "转换实例保底回收器");
			WORKER.start();
		} catch (Throwable ex){
			logger.error("pdfbox转换实例保底回收线程启动异常！", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	// 转换组限制器
	public static final PdfboxGroupLimit LIMIT = new PdfboxGroupLimit();
	public static final PdfboxLimitWorker LIMIT_WORKER;
	static {
		try {
			LIMIT_WORKER = new PdfboxLimitWorker(10000, "Pdfbox占位限制器");
			LIMIT_WORKER.start();
		} catch (Throwable ex){
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static final PbCOSNameMapCleanWorker COSNAMEMAPCLEAN_WORKER;
	static {
		try {
			COSNAMEMAPCLEAN_WORKER = new PbCOSNameMapCleanWorker( 1000* 60 * 60, "TKZX pdfbox的COSName的nameMap属性值内存泄漏监控");
			WORKER.start();
		} catch (Throwable ex){
			logger.error("pdfbox的COSName的nameMap属性值内存泄漏监控线程启动异常！", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	/**
	 * 监控和清理占位集合
	 * @throws Throwable
	 */
	public static void monitorTakings() throws Throwable {
		ConcurrentHashMap<CachePdfboxRendererGroup, String> takingMap = LIMIT.getTakingMap();
		// 遍历监控和及时释放任务
		for (Map.Entry<CachePdfboxRendererGroup, String> entry : takingMap.entrySet()) {
			CachePdfboxRendererGroup group = entry.getKey();
			clearGroupMap(group);
		}
		if (LIMIT.hasWaiters(10)) {
			LIMIT.unblockAllThreads(10);
		}
	}

	/**
	 * 在转换实例做初始化时，将转换实例做负载到合适的转换队列
	 */
	private static String onLBalancePreviewid(CachePdfboxRendererGroup group){
		String ruleModel = String.valueOf(CONVERT_PARAM_CONFIG.getConvertTaskLbRule());
		PdfboxAbstractLoadBalance loadBalance = PdfboxLoadBalanceFactory.buildRule(ruleModel);
		return loadBalance.selectPreviewid(group, SINGLETON_EXECUTOR_FACTORY.getSelects(), CONVERT_PARAM_CONFIG);
	}

	/**
	 * 负载到合适的转换队列，返回队列的序号
	 */
	private static int selectQueueNo(){
		String ruleModel = String.valueOf(CONVERT_PARAM_CONFIG.getConvertLbRule());
		PdfboxAbstractLoadBalance loadBalance = PdfboxLoadBalanceFactory.buildRule(ruleModel);
		return loadBalance.selectQueueNo(SINGLETON_EXECUTOR_FACTORY.getSelects(), CONVERT_PARAM_CONFIG);
	}


	public static Map<String, CachePdfboxRenderer> getChildMap(CachePdfboxRendererGroup group){
		if (group != null) {
			return group.getGroupMap();
		}
		return null;
	}

	public static CachePdfboxRenderer getPdfRenderer(CachePdfboxRendererGroup group, String previewidChild){
		if (group != null) {
			Map<String, CachePdfboxRenderer> pdfRendererChildMap = group.getGroupMap();
			if (pdfRendererChildMap != null) {
				return pdfRendererChildMap.get(previewidChild);
			}
		}
		return null;
	}

	public static CachePdfboxRendererGroup getPdfRendererGroup(String previewidRoot){
		return TASK_MAP.get(previewidRoot);
	}

	// 销毁指定预览id的转换实例组
	public static void clearGroupMap(CachePdfboxRendererGroup group){
		if (isCancelGroupMap(group)) {
			ReentrantReadWriteLock readWriteLock = group.getReadWriteLock();
			ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
			if (writeLock.tryLock()) {
				try {
					if (isCancelGroupMap(group)) {
						try {
							cancelGroupMapSafe(group);
							LIMIT.removeUnblockAllThreads(group, 10);
						} catch (Throwable e) {
							logger.error("Pdfbox清除转换实例组Map异常", e);
						}
					}
				} finally {
					writeLock.unlock();
				}
			}
		}
	}

	private static void cancelGroupMapSafe(CachePdfboxRendererGroup rendererGroup) {
		if (rendererGroup != null) {
			Map<String, CachePdfboxRenderer> groupMap = rendererGroup.getGroupMap();
			if (groupMap != null && groupMap.size() > 0) {
				String previewidRoot = rendererGroup.getPreviewidRoot();
				logger.info("关闭PDDocument Map，预览id：【{}】", previewidRoot);
				Iterator<Map.Entry<String, CachePdfboxRenderer>> it = groupMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, CachePdfboxRenderer> entry = it.next();
					if (entry != null) {
						CachePdfboxRenderer renderer = entry.getValue();
						if (renderer != null) {
							if (cancelRendererSafe(renderer)) {
								String previewidChild = entry.getKey();
								LOCK.remove(previewidChild);
								it.remove();
							}
						}
					}
				}
			}
		}
	}

	// 转换组没有任务，且过期时，将尝试清理转换相关数据
	public static boolean isCancelGroupMap(CachePdfboxRendererGroup group){
		return group.isMapExpireTime() && !group.hasTask();
	}

	/**
	 * 尝试销毁指定预览id的转换实例组
	 */
	public static boolean clearGroup(CachePdfboxRendererGroup group) throws Throwable {
		ReentrantReadWriteLock readWriteLock = group.getReadWriteLock();
		ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
		if (writeLock.tryLock()) {
			try {
				// 如果当前组没有任何任务，就可尝试释放
				if (isCancelGroup(group)) {
					String previewidRoot = group.getPreviewidRoot();
					logger.info("当前转换实例组已过期，关闭转换实例组，预览id：【{}】", previewidRoot);
					try {
						group.cancel();
						closeRendererChildMap(group);
						removeGroup(group);
					} catch (Throwable e) {
						logger.error("Pdfbox清除转换实例组异常", e);
					}
					try {
						File file = group.getFile();
						FileUtils.delete(file, "Pdfbox转换实例过期的");
					} catch (Throwable e) {
						logger.error("Pdfbox删除源文件异常", e);
					}
					return true;
				}
			} finally {
				writeLock.unlock();
			}
		}
		return false;
	}

	private static void removeGroup(CachePdfboxRendererGroup group) {
		String previewidRoot = group.getPreviewidRoot();
		CachePdfboxRendererGroup group1 = TASK_MAP.get(previewidRoot);
		if (group1 != null && group1.equals(group)){
			CachePdfboxRendererGroup removeGroup = TASK_MAP.remove(previewidRoot);
			cancelRendererSafeByGroup(removeGroup);
		}
		LOCK_ROOT_FILE.remove(previewidRoot);
	}

	// 转换组没有任务，且过期时，将尝试清理转换相关数据
	public static boolean isCancelGroup(CachePdfboxRendererGroup group){
		return group.isExpireTime() && !group.hasTask();
	}

	// 销毁指定转换实例组
	public static void closeRendererChildMap(CachePdfboxRendererGroup group){
		if (group != null) {
			Map<String, CachePdfboxRenderer> childMap = group.getGroupMap();
			if (childMap != null) {
				Iterator<Map.Entry<String, CachePdfboxRenderer>> it = childMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, CachePdfboxRenderer> entry = it.next();
					if (entry != null) {
						String previewidChild = entry.getKey();
						CachePdfboxRenderer renderer = entry.getValue();
						// 取消指定转换实例
						cancelRenderer(renderer);
						LOCK.remove(previewidChild);
					}
					it.remove();
				}
			}
		}
	}

	// 取消指定转换实例
	public static void cancelRenderer(CachePdfboxRenderer renderer){
		if (renderer != null) {
			try {
				// 取消转换实例、从转换实例集合移除转换实例，移除lock锁
				renderer.cancel();
			} finally {
				try {
					PDDocument pdDocument = renderer.getPDDocument();
					if (pdDocument != null) {
						pdDocument.close();
					}
				} catch (Throwable e) {
					logger.error("monitorPddocumentExpire关闭pdDocument实例异常", e);
				}
			}
			RENDERER_MAP.remove(renderer);
		}
	}

	private static boolean cancelRendererSafe(CachePdfboxRenderer renderer) {
		if (renderer == null) {
			return true;
		}
		ReentrantReadWriteLock readWriteLock = renderer.getReadWriteLock();
		ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
		if (writeLock.tryLock()) {
			try {
				// 取消指定转换实例
				cancelRenderer(renderer);
				return true;
			} finally {
				writeLock.unlock();
			}
		}
		return false;
	}

	public static void clearNoUseRenderer() throws Throwable {
		for (Map.Entry<CachePdfboxRenderer, CachePdfboxRendererGroup> entry : RENDERER_MAP.entrySet()) {
			if (entry != null) {
				CachePdfboxRenderer renderer = entry.getKey();
				CachePdfboxRendererGroup group = entry.getValue();
				if (group == null || renderer == null) {
					// 取消指定转换实例
					if (cancelRendererSafe(renderer)) {
						continue;
					}
				}
				if (isCancelRenderer(renderer)){
					if (cancelRendererSafe(renderer)) {
						continue;
					}
				}
				if (group != null && isCancelGroup(group)) {
					ReentrantReadWriteLock readWriteLock = group.getReadWriteLock();
					ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();
					if (writeLock.tryLock()) {
						try {
							if (isCancelGroup(group)) {
								// 取消指定转换实例
								if (cancelRendererSafe(renderer)) {
									continue;
								}
							}
						} finally {
							writeLock.unlock();
						}
					}
				}
			}
		}
	}

	public static boolean isCancelRenderer(CachePdfboxRenderer renderer){
		return !isValid(renderer);
	}

	public static float countScale(PDDocument pdDocument, int pageNo){
		if (pdDocument == null){
			throw new RuntimeException("计算scale异常，pdDocument为空");
		}
		PDPage pdPage = pdDocument.getPage(pageNo);
		PDRectangle cropbBox = pdPage.getCropBox();
		float width = cropbBox.getWidth();
		float height = cropbBox.getHeight();
		ImageScaleScalableUtil.ImageScaleModel imageScaleModel = IMAGE_SCALE_MODEL;
		float scale = ImageScaleScalableUtil.scale(width, height, imageScaleModel.getStandardScale(),
				imageScaleModel.getBaseSizeMin(), imageScaleModel.getBaseSizeMax(), imageScaleModel.getScaleSelector());
//		float maxWidthHeight = CONVERT_PARAM_CONFIG.getMaxWidthHeight();
//		float ratio = Math.min(maxWidthHeight / width, maxWidthHeight / height);
//		float scale = Math.min(5, ratio);
		if(scale < 1.0f) {
			logger.info("=== pdf width {} height {} scale {} to width {} height {}====",width, height,scale,width*scale,height*scale);
		}
		return scale;
	}

	// 转换实例组是否还有效
	public static boolean isValidGroup(CachePdfboxRendererGroup group){
		return group != null && !group.isCancelled() && group.isValidFile();
	}

	// 转换实例是否还有效
	public static boolean isValid(CachePdfboxRenderer renderer){
		return renderer != null && !renderer.isCancelled() && isValidDocument(renderer.getPDDocument());
	}

	// 判断 pdDocument是否有效
	private static boolean isValidDocument(PDDocument pdDocument){
		if (pdDocument != null){
			COSDocument document = pdDocument.getDocument();
			if (document != null && !document.isClosed()){
				return true;
			}
		}
		return false;
	}

	// 获取转换模式
	private static EnumConvertModel getConvertModel(CachePdfboxRendererGroup group){

		// 当转换实例数只允许一个时，使用单实例转换模式
		long pdDocumentMaxSize = CONVERT_PARAM_CONFIG.getPdDocumentMaxSize();
		if (pdDocumentMaxSize == 1){
			return EnumConvertModel.OPT_SINGLE;
		}

		// 如果文件大于指定大小，则使用单实例转换模式，因为越大的文件加载时间越长，当加载时长比转换时长多太多，整体转换速率反而会下降。
		Long pdfSize = group.getPdfSize();
		if (pdfSize != null && pdfSize > CONVERT_PARAM_CONFIG.getSingleFileMaxSize()){
			return EnumConvertModel.OPT_SINGLE;
		}

		// 如果文件的页数大于指定页数，则使用单实例转换模式，因为页数越多的文件，pdf obj对象数肯定越多，加载的转换实例就越大。
		Long pdfNum = group.getPdfNum();
		if (pdfNum != null && pdfNum > CONVERT_PARAM_CONFIG.getSingleFileMaxCount()){
			return EnumConvertModel.OPT_SINGLE;
		}

		// 如果pdf obj对象数的预设值大于0，且当前pdf obj对象数大于预设值，使用单实例转换模式。
		Long foreknewObjectSize = group.getForeknewObjectSize();
		if (isSingleFileMaxPdfObj() && foreknewObjectSize != null && foreknewObjectSize > CONVERT_PARAM_CONFIG.getSingleFileMaxPdfObj()){
			return EnumConvertModel.OPT_SINGLE;
		}
		return EnumConvertModel.OPT_MORE;
	}

	// 是否打开了pdf对象数触发单转换实例模式的开关
	private static boolean isSingleFileMaxPdfObj(){
		return CONVERT_PARAM_CONFIG.getSingleFileMaxPdfObj() > 0;
	}

	private static PDDocument loadPDDocumentMonitorGroup(File file, String decryptionPassword, InputStream keyStore, String alias, CachePdfboxRendererGroup group) throws Throwable {
		return MonitorPDDocument.loadPDDocumentGroup(file, decryptionPassword, keyStore, alias, group);
	}

	/**
	 * 注意：pdfbox转换优先推荐使用当前方法。
	 * pdfbox多转换实例转换入口方法
	 * 超时重试负载，作用：
	 * 1、将积压在lock锁队列的任务重新负载均衡，分配到合适的队列里，提升转换实例的利用率。
	 * @param previewidRoot 预览id，用于表示某个文档
	 * @param pageNo 页，某个文档转换的页
	 * @param task 转换实例的回调任务，用于回调获取转换实例，pdfbox的转换实例是PDDocument
	 * @return
	 * @throws Throwable
	 */
	public static BufferedImage loadBalancingToImage(String previewidRoot, int pageNo, PdfRendererTask task) throws Throwable {
		if (StringUtils.isBlank(previewidRoot)){
			throw new RuntimeException("previewidRoot文件标识不能为空");
		}
		if (pageNo < 0){
			throw new RuntimeException("pageNo转换页码数不能小于0");
		}
		if (task == null){
			throw new RuntimeException("task实例不能为空");
		}
		// 下载源文件到本地
		CachePdfboxRendererGroup group = getGroupLockAndContinue(previewidRoot, null, task);
		// 续转换实例组的有效时长：查屏障
		group.continueExpireTime();
		// 加转换读锁记录，用于判断转换实例能否过期
		ReentrantReadWriteLock readWriteLock = group.getReadWriteLock();
		ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
		if (readLock.tryLock(CONVERT_PARAM_CONFIG.getConvertTimeout(), TimeUnit.MILLISECONDS)) {
			try {
				// 占位或阻塞等待
				if (!LIMIT.takingAndBlockThread(group, CONVERT_PARAM_CONFIG.getConvertTpWaitTimeout(), CONVERT_PARAM_CONFIG.getGroupMaxNum())){
					logger.debug("转换实例占位失败，积压的转换文档过多，稍等重试，总占位个数上限【"+ CONVERT_PARAM_CONFIG.getGroupMaxNum()+ "】");
					throw new RuntimeException("转换文档的数量较多，占位失败，请稍后重试");
				}
				EnumConvertModel convertModel = getConvertModel(group);
				return loadBalancing(group, pageNo, convertModel, task);
			} finally {
				readLock.unlock();
			}
		} else {
			throw new RuntimeException("获取转换的资格超时，当前设置的超时时间：【"+ CONVERT_PARAM_CONFIG.getConvertTimeout() + "】ms");
		}

	}

	private static BufferedImage loadBalancing(CachePdfboxRendererGroup group, int pageNo, EnumConvertModel convertModel, PdfRendererTask task) throws Throwable {

		BufferedImage bufferedImage = null;
		String previewidChild = null;
		String previewidRoot = group.getPreviewidRoot();

		int retryTime = CONVERT_PARAM_CONFIG.getWaitTimeoutLBMaxSize();
		for (int i = 1; i <= retryTime; i++) {
			try {
				// 选择转换模式
				if (EnumConvertModel.OPT_MORE.getValue().equals(convertModel.getValue())){
					// 多转换实例模式，选择一个转换实例来转换
					previewidChild = onLBalancePreviewid(group);
				} else {
					// 单实例转换模式，只有一个主转换实例
					previewidChild = previewidRoot;
				}
				// 负载获取一个子转换id
				// 获取转换实例
				CachePdfboxRenderer pdfRenderer = getCachePDFRendererLock(group, previewidChild, task);
				// 基于转换实例绑定的转换队列序号获取相应转换执行器
				SingletonExecutor executor = SINGLETON_EXECUTOR_FACTORY.getSelects().get(pdfRenderer.getQueueNo());
				bufferedImage = toImagePdfboxTP(group, previewidChild, pageNo,
						pdfRenderer, executor);
				break;
			} catch (RendererNotValidException e){
				// 最后一次重试还是不能拿到有效的转换实例，将抛出超出最大重试次数的异常！
				if (i >= retryTime){
					logger.error("pdfbox重新加载转换实例转换的次数达到最大限制，转换previewidChild【 "+previewidChild+ "】, 当前页：【"+ pageNo +"】");
					throw e;
				} else {
					// 转换实例失效的异常，重新加载转换实例，再去转换任务
					logger.error("pdfbox转换实例失效，重新加载转换实例转换，转换previewidChild【 "+previewidChild+ "】, 当前页：【"+ pageNo +"】");
				}
			} catch (Throwable e) { // 其它异常都结束转换任务
				logger.error("转换图片异常，转换previewidChild【 "+previewidChild+ "】, 当前页：【"+ pageNo +"】", e);
				throw e;
			}
		}
		return bufferedImage;
	}

	private static BufferedImage toImagePdfboxTP(CachePdfboxRendererGroup group, String previewidChild, int renderPageNo,
												 CachePdfboxRenderer pdfRenderer, SingletonExecutor executor) throws Throwable {
		// 基于子预览id找出对应的转换队列，提交转换任务
		Future<BufferedImage> submit = null;
		try {
			String previewidRoot = group.getPreviewidRoot();
			HashedWheelTimer hashedWheelTimer = group.getHashedWheelTimer();

			PdfboxConvertMessage message = new PdfboxConvertMessage(previewidRoot, previewidChild, renderPageNo, hashedWheelTimer);
			PdfboxConvertRequest request = new PdfboxConvertRequest(message, CONVERT_PARAM_CONFIG.getConvertTimeout());
			request.setMsg("渲染 draw pdf");

			submit = executor.submit(new Callable<BufferedImage>() {
				@Override
				public BufferedImage call() {

					ReentrantReadWriteLock readWriteLock = pdfRenderer.getReadWriteLock();
					ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
					try {
						if (readLock.tryLock(CONVERT_PARAM_CONFIG.getConvertTimeout(), TimeUnit.MILLISECONDS)) {
							try {
								// 如果当前转换实例失效，则重新去加载转换实例转换
								if (!isValidOrClose(pdfRenderer)){
									throw new RendererNotValidException();
								}

								float scale = countScale(pdfRenderer.getPDDocument(), renderPageNo);
								CompletableFuture<MonitorResponse<PdfboxConvertMessage>> drawFuture = null;

								long t1 = System.currentTimeMillis();
								// 添加转换图片超时监控
								message.setPdfRenderer(pdfRenderer);
								request.continueExpireTime();
								try {
									try {
										drawFuture = request.submitRequest();
										try {
											return pdfRenderer.renderImage(renderPageNo, scale);
										} catch (Throwable e) {
											checkRenderThrowable(e);
											throw e;
										} finally {
											try {
												clearCache(pdfRenderer);
											} catch (Throwable e) {
												logger.warn("===【{"+ previewidRoot+ "}】页码【{"+ renderPageNo+ "}】pdfbox 转换后清除缓存异常=="+ "转换previewidChild【{"+ previewidChild+ "}】", e);
											}
										}
									} finally {
										logger.debug("===【{}】页码【{}】pdfbox 转换,耗时:{}ms==" + "转换previewidChild【{}】", previewidRoot, renderPageNo, System.currentTimeMillis() - t1, previewidChild);
										// 取消转换超时监控任务
										request.cancelMonitor();
										// 等待监控结果
										if (drawFuture != null) {
											MonitorResponse<PdfboxConvertMessage> response = drawFuture.get(20000, TimeUnit.MILLISECONDS);
											if (!response.isSuccess()) {
												throw new PdfConvertException(response.getMessage(), response.getThrowable());
											}
										}
									}
								} catch (PdfConvertException e) {
									throw e;
								} catch (Throwable e) {
									logger.error("转图片异常", e);
									throw new RuntimeException(e.getMessage());
								}
							} finally {
								readLock.unlock();
							}
						} else {
							throw new PdfConvertException("当前图片预览转换等待异常超时！当前设置的超时时间：【"+ CONVERT_PARAM_CONFIG.getConvertTimeout() + "】ms");
						}
					} catch (InterruptedException e) {
						throw new PdfConvertException("转换渲染时等待异常！", e);
					}
				}
			});
		} catch (RejectedExecutionException e) {
			logger.error("转换队列积压任务过多，拒绝后续转换任务", e);
			logExecutorTaskSize();
			throw new ConvertorException(e.getMessage());
		} catch (PdfConvertException e) {
			throw e;
		} catch (Throwable e) {
			logger.error("提交转换任务到线程异常", e);
			throw new ConvertorException(e.getMessage());
		}

		// 获取转换结果
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = submit.get(CONVERT_PARAM_CONFIG.getConvertTpWaitTimeout(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("pdfbox图片转换的线程中断", e);
			throw new ConvertorException(e.getMessage());
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RendererNotValidException) {
//				logger.error("pdfbox转换实例失效", e);
				throw new RendererNotValidException(e.getMessage());

			} else if (e.getCause() instanceof PdfConvertException) {
				throw e.getCause();
			} else {
				logger.error("pdfbox图片转换的线程执行异常", e);
				throw new ConvertorException(e.getMessage());
			}
		} catch (TimeoutException e) {
			logger.error("pdfbox图片转换的任务执行超时", e);
			throw new ConvertorException(e.getMessage());
		}

		return bufferedImage;
	}

	private static void clearCache(CachePdfboxRenderer pdfRenderer){
		// 清除缓存的图片对象。
		PdfBoxUnsafeUtils.setPdfrendererPageUnsafe(pdfRenderer, null);
		// 清除Cache。
		PDDocument pdDocument = pdfRenderer.getPDDocument();
		if (pdDocument != null) {
			ResourceCache resourceCache = pdDocument.getResourceCache();
			if (resourceCache instanceof PdfboxResourceCache) {
				PdfboxResourceCache pdfboxResourceCache = (PdfboxResourceCache) resourceCache;
				if (pdfboxResourceCache.countXObjectSize() > 30000 || pdfRenderer.getRenderCoutNum() % 10 == 0) {
					pdfboxResourceCache.clearXObject();
				}
			}
		}
	}

	// 转换实例是否还有效（注意：只有消费者线程处才能用）
	private static boolean isValidOrClose(CachePdfboxRenderer renderer){
		boolean valid = isValid(renderer);
		// 无效则释放转换实例
		if (!valid){
			cancelRenderer(renderer);
		}
		return valid;
	}

	private static void checkRenderThrowable(Throwable e) throws Throwable {
		try {
			logger.error("渲染图片异常", e);
			PbRenderExceptionRequest pdfCheckRequest = new PbRenderExceptionRequest(e, 0, EXCEPTION_BUILD);
			pdfCheckRequest.submitRequest();
		} catch (Throwable t){
			if (t instanceof CheckException) {
				throw new PdfConvertException(t.getMessage(), t);
			}
			throw t;
		}
	}

	/**
	 * 用于统计pdfbox转换线程池组的队列任务堆积情况
	 */
	public static Map<Integer, Integer> countExecutorTaskSize() {
		List<SingletonExecutor> selects = SINGLETON_EXECUTOR_FACTORY.getSelects();
		HashMap<Integer, Integer> hashMap = new HashMap<>();
		if (selects != null) {
			for (int i = 0; i < selects.size(); i++) {
				SingletonExecutor executor = selects.get(i);
				if (executor != null) {
					int taskSize = executor.taskSize();
					hashMap.put(i, taskSize);
				}
			}
		}
		return hashMap;
	}

	/**
	 * 用于打印pdfbox转换线程池组的队列任务堆积情况
	 */
	public static void logExecutorTaskSize() {
		Map<Integer, Integer> hashMap = countExecutorTaskSize();
		logger.error("pdfbox转换线程池组的队列任务堆积情况:{}", hashMap);
	}

 	private static CachePdfboxRendererGroup getGroupLockAndContinue(String previewidRoot, ReentrantReadWriteLock readWriteLock, PdfRendererTask task) {
		// 下载源文件到本地
		CachePdfboxRendererGroup group = getCachePDFRendererGroupLock(previewidRoot, readWriteLock, task);
		// 续转换实例组的有效时长：查屏障
		group.continueExpireTime();
		return group;
	}

	private static CachePdfboxRendererGroup getCachePDFRendererGroupLock(String previewidRoot, ReentrantReadWriteLock readWriteLock, PdfRendererTask task) {

		// 如果存在缓存的CachePdfboxRendererGroup，且有效，则直接返回
		if (TASK_MAP.containsKey(previewidRoot)) {
			CachePdfboxRendererGroup group = getPdfRendererGroup(previewidRoot);
			if (isValidGroup(group)){
				return group;
			}
		}

		ReentrantLockEntry reentrantLockEntry = LOCK_ROOT_FILE.computeIfAbsent(previewidRoot);
		ReentrantLock reentrantLock = reentrantLockEntry.getReentrantLock();
		// 局部强引用，避免当前方法还在执行就因fullgc导致锁entry被回收
		Map.Entry<String, ReentrantLockEntry> entry = LOCK_ROOT_FILE.getEntry(previewidRoot);
		try {

			if (reentrantLock.tryLock(CONVERT_PARAM_CONFIG.getSingleConvertTimeout(), TimeUnit.MILLISECONDS)) {
				try {
					return getCachePDFRendererGroup(previewidRoot, readWriteLock, task);
				} finally {
					reentrantLock.unlock();
				}
			} else {
				// 可能存在等待超时的任务，但其实文件已经下载好到本地的情况，可尝试获取该文件
				CachePdfboxRendererGroup group = getPdfRendererGroup(previewidRoot);
				if (isValidGroup(group)){
					return group;
				} else {
					// 锁等待超时
					logger.error("pdfbox转换图片前加载pdf文档等待超时，请重试");
					throw new TimeoutException("pdfbox转换图片前加载pdf文档等待超时，请重试");
				}
			}

		} catch (Throwable e) {
			logger.error("本文档的转换文件加载失败，请重试", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	private static CachePdfboxRendererGroup getCachePDFRendererGroup(String previewidRoot, ReentrantReadWriteLock readWriteLock,
																	 PdfRendererTask task) throws Throwable {

		// 如果存在缓存的CachePdfboxRendererGroup，且有效，则直接返回
		if (TASK_MAP.containsKey(previewidRoot)) {
			CachePdfboxRendererGroup group = getPdfRendererGroup(previewidRoot);
			if (isValidGroup(group)){
				return group;
			}
		}

		// 否则，初始化新的CachePdfboxRendererGroup，以及添加pdfbox CachePdfboxRendererGroup到过期监控
		PdfRendererCallable callable = task.getCallable();
		CachePdfboxRendererGroup group = null;
		File file = null;
		try {
			file = (File) callable.initPdfRenderer(task);
			HashedWheelTimer hashedWheelTimer = HASHED_WHEEL_TIMER_FACTORY.selectHashedWheelTimer(previewidRoot);
			group = new CachePdfboxRendererGroup(file, previewidRoot, hashedWheelTimer, readWriteLock);
			if (!group.isValidFile()){
				String filePath = (file != null) ? file.getAbsolutePath() : "";
				throw new RuntimeException("当前pdfbox转换获取的源文件无效，预览id：【"+ previewidRoot +"】，文件所在路径："+ filePath);
			}

			handleRendererGroupParam(group, task);
			PdfboxRendererExpireRequest request = new PdfboxRendererExpireRequest(group, CONVERT_PARAM_CONFIG.getCachePdfrendererTimeout(), Math.min(CONVERT_PARAM_CONFIG.getGroupMapContinueTime(), CONVERT_PARAM_CONFIG.getCachePdfrendererTimeout()));
			group.setRequest(request);
			CachePdfboxRendererGroup rendererGroupOld = TASK_MAP.put(previewidRoot, group);
			Map.Entry<String, CachePdfboxRendererGroup> entry = TASK_MAP.getEntry(previewidRoot);
			group.setGroupEntry(entry);
			if (!group.equals(rendererGroupOld)) {
				cancelRendererSafeByGroup(rendererGroupOld);
			}

			// 添加Pddocument过期监控
			request.submitRequest();
		} catch (Throwable e){
			// 文件添加监控异常，则直接删除
			if (file != null){
				Files.delete(file.toPath());
			}
			throw e;
		}

		return group;
	}

	private static void cancelRendererSafeByGroup(CachePdfboxRendererGroup rendererGroup) {
		if (rendererGroup != null) {
			Map<String, CachePdfboxRenderer> groupMap = rendererGroup.getGroupMap();
			if (groupMap != null) {
				for (Map.Entry<String, CachePdfboxRenderer> entry : groupMap.entrySet()) {
					if (entry != null) {
						CachePdfboxRenderer renderer = entry.getValue();
						if (renderer != null) {
							renderer.cancel();
							cancelRendererSafe(renderer);
						}
					}
				}
			}
		}
	}

	// 处理转换实例组相关参数
	private static void handleRendererGroupParam(CachePdfboxRendererGroup group, PdfRendererTask task) throws Throwable {

		// 处理用户传入的pdf文件大小，没有则自己从临时文件获取，将源文件大小记录到当前转换实例组
		Long pdfSize = task.getPdfSize();
		if (pdfSize == null) {
			File file = group.getFile();
			if (file != null) {
				pdfSize = file.length();
			}
		}
		group.setPdfSize(pdfSize);

		// 处理用户传入的pdf文件总页数，将文件总页数记录到当前转换实例组，
		Long pdfNum = task.getPdfNum();
		group.setPdfNum(pdfNum);

		if (isSingleFileMaxPdfObj()) {
			CachePdfboxRenderer cachePDFRenderer = getCachePDFRendererLock(group, group.getPreviewidRoot(), task);
			if (cachePDFRenderer != null) {
				PDDocument pdDocument = cachePDFRenderer.getPDDocument();
				if (pdDocument != null) {
					COSDocument document = pdDocument.getDocument();
					if (document != null){
						COSDictionary trailer = document.getTrailer();
						long foreknewObjectSize = parseObjectSizeByTrailer(trailer);
						group.setForeknewObjectSize(foreknewObjectSize);
					}
					if (group.getPdfNum() == null) {
						int numberOfPages = pdDocument.getNumberOfPages();
						group.setPdfNum((long) numberOfPages);
					}
				}

			}
		}
	}

	// 没有值会返回-1
	private static long parseObjectSizeByTrailer(COSDictionary trailer) throws IOException {
		return trailer.getLong(COSName.SIZE);
	}

	private static CachePdfboxRenderer getCachePDFRendererLock(CachePdfboxRendererGroup group, String previewidChild, PdfRendererTask task) throws Throwable {

		CachePdfboxRenderer pdfRenderer = getPdfRenderer(group, previewidChild);
		if (isValid(pdfRenderer)) {
			return pdfRenderer;
		}

		ReentrantLockEntry reentrantLockEntry = LOCK.computeIfAbsent(previewidChild);
		ReentrantLock reentrantLock = reentrantLockEntry.getReentrantLock();
		// 局部强引用，避免当前方法还在执行就因fullgc导致锁entry被回收
		Map.Entry<String, ReentrantLockEntry> entry = LOCK.getEntry(previewidChild);
		if (reentrantLock.tryLock(CONVERT_PARAM_CONFIG.getSingleConvertTimeout(), TimeUnit.MILLISECONDS)) {
			try {
				// 获取pdfRenderer，添加Pddocument过期监控
				return getCachePDFRenderer(group, previewidChild, task);
			} finally {
				reentrantLock.unlock();
			}
		} else {

			CachePdfboxRenderer pdfRenderer1 = getPdfRenderer(group, previewidChild);
			if (isValid(pdfRenderer1)) {
				return pdfRenderer1;
			} else {
				// 锁等待超时
				throw new TimeoutException("pdfbox获取转换实例等待超时，请重试");
			}
		}
	}

	private static CachePdfboxRenderer getCachePDFRenderer(CachePdfboxRendererGroup group, String previewidChild, PdfRendererTask task) throws Throwable {

		// 如果存在缓存的CachePDFRenderer，CachePDFRenderer没有取消，则直接返回
		CachePdfboxRenderer pdfRenderer = getPdfRenderer(group, previewidChild);
		if (isValid(pdfRenderer)){
			return pdfRenderer;
		}

		// 否则，初始化新的CachePDFRenderer，以及添加Pddocument过期监控
		PDDocument pdDocument = null;
		try {
			File file = group.getFile();
			pdDocument = loadPDDocumentMonitorGroup(file, "", null, null, group);
			// 负载均衡后获取绑定转换队列的序号
			int queueNo = selectQueueNo();
			pdfRenderer = new CachePdfboxRenderer(pdDocument, previewidChild, queueNo);
			//  开启图像子采样
			pdfRenderer.setSubsamplingAllowed(true);

			putAndMonitor(group, previewidChild, pdfRenderer);
			RENDERER_MAP.put(pdfRenderer, group);
		} catch (Throwable e){
			// 转换实例添加监控异常，则关闭转换实例
			if (pdDocument != null){
				IOUtils.closeQuietly(pdDocument);
			}
			throw e;
		}

		return pdfRenderer;
	}

	private static void putAndMonitor(CachePdfboxRendererGroup group, String previewidChild, CachePdfboxRenderer pdfRenderer) {

		// 获取转换实例集合
		Map<String, CachePdfboxRenderer> childMap = getChildMap(group);
		if (childMap != null) {
			CachePdfboxRenderer rendererOld = childMap.put(previewidChild, pdfRenderer);
			if (rendererOld != null) {
				rendererOld.cancel();
				cancelRendererSafe(rendererOld);
			}
		}
	}
}
