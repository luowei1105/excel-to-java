package com.fmgame.exceltoconf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 属性解析
 * 
 * @author luowei
 * @date 2017年10月9日 下午3:35:09
 */
final class ConfigPropParser {

	/**  Excel源文件的地址 */
	public static String EXCEL_SOURCE_FILE_PATH = Thread.currentThread().getContextClassLoader().getResource("").getPath();
	/** java源文件包定义 */
	public static String JAVA_SOURCE_PACKAGE = "com.fmgame.exceldata";
	/** java模板文件名 */
	public static String JAVA_TEMPLATE_FILE_NAME = "java_template.ftl";
	/** java文件输出路径 */
	public static String JAVA_FILE_OUTPUT_PATH = Thread.currentThread().getContextClassLoader().getResource("").getPath();
	/** jSON模板文件名 */
	public static String JSON_TEMPLATE_FILE_NAME = "json_template.ftl";
	/** json文件输出路径 */
	public static String JSON_FILE_OUTPUT_PATH = Thread.currentThread().getContextClassLoader().getResource("").getPath();
	
	/** 标记：变量描述行 */
	public static int FLAG_VARIABLE_DESC_ROW = 0;
	/** 标记：客户端或者服务器字段行 */
	public static int FLAG_CS_ROW = 1;
	/** 标记：变量命名行 */
	public static int FLAG_VARIABLE_NAMING_ROW = 2;
	/** 标记：变量类型行 */
	public static int FLAG_VARIABLE_TYPE_ROW = 3;
	/** 标记：头标识长度 */
	public static int FLAG_HEAD_LEN = 4;

	static {
		Properties prop = loadProperties();
		
		// 设置参数
		EXCEL_SOURCE_FILE_PATH = prop.getProperty("EXCEL_SOURCE_FILE_PATH");
		JAVA_SOURCE_PACKAGE = prop.getProperty("JAVA_SOURCE_PACKAGE");
		
		JAVA_TEMPLATE_FILE_NAME = prop.getProperty("JAVA_TEMPLATE_FILE_NAME");
		JAVA_FILE_OUTPUT_PATH = prop.getProperty("JAVA_FILE_OUTPUT_PATH");
		
		JSON_TEMPLATE_FILE_NAME = prop.getProperty("JSON_TEMPLATE_FILE_NAME");
		JSON_FILE_OUTPUT_PATH = prop.getProperty("JSON_FILE_OUTPUT_PATH");
		
		FLAG_HEAD_LEN = Integer.valueOf(prop.getProperty("FLAG_HEAD_LEN"));
	}
	
	/**
	 * 加载属性
	 * @return
	 */
	private static Properties loadProperties() {
		Properties prop = new Properties();
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
			prop.load(in);
			in.close();
		} catch (IOException e) {
			throw new ParserException("解析configuration.properties配置文件出错" + e);
		} 
		return prop;
	}

}
