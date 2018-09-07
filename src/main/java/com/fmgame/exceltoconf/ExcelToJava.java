package com.fmgame.exceltoconf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fmgame.exceltoconf.ExcelTo.JavaTemplateConf;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * excel 转换java文件
 * 
 * @author luowei
 * @date 2017年10月9日 下午5:19:47
 */
final class ExcelToJava {
	
	/** 类模板 */
	private static Template template;
	
	static {
		try {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			cfg.setDirectoryForTemplateLoading(new File(Thread.currentThread()
					.getContextClassLoader().getResource("").getPath()));
			
			template = cfg.getTemplate(ConfigPropParser.JAVA_TEMPLATE_FILE_NAME);
		} catch (IOException e) {
			throw new ParserException("生成Java模板出现错误", e);
		}
	}

	/**
	 * 生成java文件
	 * @param naming
	 * @param javaTemplateConf
	 */
	public static void toJava(String naming, JavaTemplateConf javaTemplateConf) {
		// 设置类名
		String className = Utils.NAMING_PREFIX + naming;
		genJavaFile(className, genFreemarkerPropMap(className, javaTemplateConf));
		System.err.println("  生成.java文件完成--->>> " + className + ".java");
	}
	
	/**
	 * 生成freemarker需要的属性参数
	 * @param className
	 * @param javaTemplateConf
	 * @return
	 */
	private static Map<String, Object> genFreemarkerPropMap(String className, JavaTemplateConf javaTemplateConf) {
		Map<String, Object> root = new HashMap<>();

		// 包名
		root.put("package", ConfigPropParser.JAVA_SOURCE_PACKAGE);
		// 设置日期参数
		root.put("createDate", new Date());
		// 设置类名
		root.put("class_name", className);
		// 设置类注释
		root.put("class_inform", javaTemplateConf.class_inform);
		// 设置属性
		root.put("vars", javaTemplateConf.vars);
		
		return root;
	}
	
	/**
	 * 生成java文件
	 * @param className
	 * @param root
	 */
	private static void genJavaFile(String className, Map<String, Object> root) {
		OutputStreamWriter writer;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(ConfigPropParser.JAVA_FILE_OUTPUT_PATH + "\\" + className + ".java"));
			template.process(root, writer);
			writer.flush();
			writer.close();
		} catch (Exception e) {
		}
	}

}
