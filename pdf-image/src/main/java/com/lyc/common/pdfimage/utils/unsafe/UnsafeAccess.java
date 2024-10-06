package com.lyc.common.pdfimage.utils.unsafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author : 		刘勇成
 * @date : 		2022/11/26 18:55
 */
public class UnsafeAccess {

	protected static final Logger logger = LoggerFactory.getLogger(UnsafeAccess.class);

	public static final boolean SUPPORTS_GET_AND_SET_REF;
	public static final boolean SUPPORTS_GET_AND_ADD_LONG;
	public static final Unsafe UNSAFE;

	static
	{
		try {
			UNSAFE = getUnsafe();
			SUPPORTS_GET_AND_SET_REF = hasGetAndSetSupport();
			SUPPORTS_GET_AND_ADD_LONG = hasGetAndAddLongSupport();
		} catch (Throwable ex) {
			logger.error("当前类初始化异常信息：", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	private static Unsafe getUnsafe()
	{
		Unsafe instance;
		try
		{
			final Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			instance = (Unsafe) field.get(null);
		}
		catch (Exception ignored)
		{
			try
			{
				Constructor<Unsafe> c = Unsafe.class.getDeclaredConstructor();
				c.setAccessible(true);
				instance = c.newInstance();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		return instance;
	}

	private static boolean hasGetAndSetSupport() {
		try {
			Unsafe.class.getMethod("getAndSetObject", Object.class, Long.TYPE, Object.class);
			return true;
		}
		catch (Exception ignored) {
		}
		return false;
	}

	private static boolean hasGetAndAddLongSupport() {
		try {
			Unsafe.class.getMethod("getAndAddLong", Object.class, Long.TYPE, Long.TYPE);
			return true;
		}
		catch (Exception ignored) {
		}
		return false;
	}

	public static long fieldOffset(Class clz, String fieldName) throws RuntimeException {
		try {
			return UNSAFE.objectFieldOffset(clz.getDeclaredField(fieldName));
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
}
