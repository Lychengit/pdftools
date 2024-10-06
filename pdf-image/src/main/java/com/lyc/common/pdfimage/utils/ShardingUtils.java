package com.lyc.common.pdfimage.utils;

/**
 * @author : 		刘勇成
 * @description :
 * @date : 		2021/9/30 17:23
 */
public class ShardingUtils {

    // hashmod
    public static int calculationHashMod(String value, int shardingCount) {
        if (shardingCount > 0) {
            value = value != null ? value : "";
            return hash(value) % shardingCount;
        }
        return 0;
    }

    public static int hash(String str){
        return Math.abs(str.hashCode());
    }

    // 对指定数据做hash取最后6位，位数不够的补充0
    public static String getHash6Str(String str){
        int hashCode = hash(str);
        return hashCode < 1000000 ? String.format("%06d", hashCode): String.valueOf(hashCode % 1000000);
    }

    // 截取字符串的后6位
    public static String subLast6Str(String str){
        return str.substring(str.length() - 6);
    }


}
