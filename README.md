# excel-to-java
用于excel文件按照一定格式生成为.java代码和.json数据文件，快速转换策划数据为游戏运行代码和数据。

# 简介
* 快速转换1个或者多个excel文件生成.java和.json文件
* excel文件支持配置java变量名，变量类型，
* java文件集成了策划数据加载及读取
* 利用freemarker框架生成java和json文件

# Quick Start
1. 配置excel文件格式及数据
```
例: src\test\resources\W物品.xlsx
```
2. 配置文件生成路径
``` properties
#Windows下路径分隔符须使用“\\\\”

#Excel源文件的地址
EXCEL_SOURCE_FILE_PATH=D:\\excel
#java源文件包定义
JAVA_SOURCE_PACKAGE=com.fmgame.support.gamedata

#java模板文件名
JAVA_TEMPLATE_FILE_NAME=java_template.ftl
#java文件输出路径
JAVA_FILE_OUTPUT_PATH=D:\\excel\\target
#json文件输出路径
JSON_TEMPLATE_FILE_NAME=json_template.ftl
JSON_FILE_OUTPUT_PATH=D:\\excel\\target

# 标记文件头
FLAG_HEAD_LEN = 5
```
3. run ExcelTo.java main
```file
src\main\java\com\fmgame\exceltoconf\ExcelToJava.java
```
```java
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
```
