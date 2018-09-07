package com.fmgame.exceltoconf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import com.fmgame.exceltoconf.ExcelTo.JsonDataConf;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * 生成json格式数据文件。文件格式
 * [
 * {},
 * {}
 * ]
 * 
 * @author luowei
 * @date 2017年10月10日 上午11:51:07
 */
final class ExcelToJson {
	
	/** 类模板 */
	private static Template template;
	
	static {
		try {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			cfg.setDirectoryForTemplateLoading(new File(Thread.currentThread()
					.getContextClassLoader().getResource("").getPath()));
			
			template = cfg.getTemplate(ConfigPropParser.JSON_TEMPLATE_FILE_NAME);
		} catch (IOException e) {
			throw new ParserException("生成Java模板出现错误", e);
		}
	}

	/**
	 * 生成json文件
	 * @param naming
	 * @param jsonDataConf
	 */
	public static void toJson(String naming, JsonDataConf jsonDataConf) {
		// 设置文件名
		String fileName = Utils.NAMING_PREFIX + naming;
		genJsonFile(fileName, genFreemarkerPropMap(fileName, jsonDataConf));
		System.err.println("  生成.json文件完成--->>> " + fileName + ".json");
	}
	
	/**
	 * 生成freemarker需要的属性参数
	 * @param className
	 * @param javaTemplateConf
	 * @return
	 */
	private static Map<String, Object> genFreemarkerPropMap(String fileName, JsonDataConf jsonDataConf) {
		Map<String, Object> root = new HashMap<>();

		// 设置数据项
		root.put("jsons", jsonDataConf.jsons);
		
		return root;
	}
	
	/**
	 * 生成java文件
	 * @param fileName
	 * @param root
	 */
	private static void genJsonFile(String fileName, Map<String, Object> root) {
		OutputStreamWriter writer;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(ConfigPropParser.JSON_FILE_OUTPUT_PATH + "\\" + fileName + ".json"));
			template.process(root, writer);
			writer.flush();
			writer.close();
		} catch (Exception e) {
		}
	}

}
