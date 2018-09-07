package com.fmgame.exceltoconf;

import java.io.File;

/**
 * excel生成java工具类
 * @author luowei
 * @date 2017年12月8日 上午11:20:09
 */
public class Utils {

	/**
	 * 命名前缀
	 */
	public static final String NAMING_PREFIX = "GD";
	
	/**
	 * 删除目录下文件
	 * @param filePath
	 */
	public static void cleanFile(String filePath) {
		File f = new File(filePath);
		if(f == null || !f.exists()){
			f.mkdir();
			return;
		}
		
		File[] files = f.listFiles();
		if (files == null)
			return;
		
		for(File file : files){
			file.delete();
		}
	}
	
	
	/**
	 * 首字母大写转换
	 * @param string
	 * @return
	 */
	public static String toUpperFristChar(String s) {  
		if (Character.isUpperCase(s.charAt(0))) {
			return s;
		} else {
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
		}
	}  
	
	/**
	 * 首字母小写转换
	 * @param string
	 * @return
	 */
	public static String toLowerCaseFristChar(String s) {  
		if(Character.isLowerCase(s.charAt(0))) {
			 return s;
		}else {
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
		}
	}  
	
}
