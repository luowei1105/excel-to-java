package com.fmgame.exceltoconf;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * excel转换为配置文件
 * 读取excel文件并转换为.json和.java文件
 * 
 * @author luowei
 * @date 2017年10月9日 下午3:31:58
 */
public class ExcelTo {
	
	/**
	 * excel模板 配置
	 * 
	 * @author luowei
	 * @date 2017年10月9日 下午8:54:12
	 */
	static class ExcelTemplateConf {
		
		/** sheet名 */
		public Set<String> namings = new LinkedHashSet<>();
		
		/** java格式模板对应列集合.key: 表名. value: java模板 */
		public Map<String, JavaTemplateConf> java_template_collections = new LinkedHashMap<>();
		
		/** java格式模板对应列集合.key: 表名. value: json数据 */
		public Map<String, JsonDataConf> json_data_collections = new LinkedHashMap<>();
	}
	
	/**
	 * java格式模板
	 * 
	 * @author luowei
	 * @date 2017年10月9日 下午8:44:07
	 */
	static class JavaTemplateConf {
		
		/** 类定义信息 */
		public String class_inform;
		/** java格式模板对应属性集合 */
		public List<JavaVarConf> vars = new LinkedList<>();
	}
	
	/**
	 * java格式模板对应属性(必须声明public class和get/set方法，否则freemarker不能识别)
	 * 
	 * @author luowei
	 * @date 2017年10月9日 下午8:46:50
	 */
	public static class JavaVarConf {
		
		/** 变量描述 */
		public String variable_desc;
		/** 变量命名 */
		public String variable_naming;
		/** 变量类型 */
		public String variable_type;

		public String getVariable_desc() {
			return variable_desc;
		}

		public void setVariable_desc(String variable_desc) {
			this.variable_desc = variable_desc;
		}

		public String getVariable_naming() {
			return variable_naming;
		}

		public void setVariable_naming(String variable_naming) {
			this.variable_naming = variable_naming;
		}

		public String getVariable_type() {
			return variable_type;
		}

		public void setVariable_type(String variable_type) {
			this.variable_type = variable_type;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("JavaAttrConf [variable_desc=");
			builder.append(variable_desc);
			builder.append(", variable_naming=");
			builder.append(variable_naming);
			builder.append(", variable_type=");
			builder.append(variable_type);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
	/**
	 * json数据配置
	 * 
	 * @author luowei
	 * @date 2017年10月9日 下午8:39:09
	 */
	static class JsonDataConf {
		
		/** json数据列表 */
		public List<String> jsons = new LinkedList<>();
	}

	public static void main(String[] args) {
		// 清除文件
		Utils.cleanFile(ConfigPropParser.JAVA_FILE_OUTPUT_PATH);
		Utils.cleanFile(ConfigPropParser.JSON_FILE_OUTPUT_PATH);
		// 读取excel文件
		List<File> files = ExcelFileReader.readExcelFiles();
		// 解析excel
		files.forEach(excelFile -> {
			System.out.println("开始解析Excel文件===>>> " + excelFile.getName());
			// 解析excel
			ExcelTemplateConf excelConf = ExcelParser.parse(excelFile);
			for (String naming : excelConf.namings) {
				JavaTemplateConf templateConf = excelConf.java_template_collections.get(naming);
				// 如果没有任何变量则不生成文件
				if (templateConf.vars.isEmpty()) {
					continue;
				}
				
				// 转换.java文件
				ExcelToJava.toJava(naming, templateConf);
				// 转换.json文件
				ExcelToJson.toJson(naming, excelConf.json_data_collections.get(naming));
			}
		});
	}
	
}
