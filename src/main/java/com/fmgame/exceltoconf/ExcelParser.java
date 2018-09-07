package com.fmgame.exceltoconf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.alibaba.fastjson.JSONObject;
import com.fmgame.exceltoconf.ExcelTo.ExcelTemplateConf;
import com.fmgame.exceltoconf.ExcelTo.JavaVarConf;
import com.fmgame.exceltoconf.ExcelTo.JavaTemplateConf;
import com.fmgame.exceltoconf.ExcelTo.JsonDataConf;

/**
 * excel解析
 * 
 * @author luowei
 * @date 2017年10月9日 下午7:12:06
 */
final class ExcelParser {

	/** 全局: excel sheet命名.不允许存在重复。sheet命令关系到.java文件和.json文件命名. */
	private static final Map<String, String> NAMING_TO_PATH = new HashMap<>();

	/** poi 公式类型处理器 */
	private static FormulaEvaluator evaluator;

	/**
	 * 解析excel文件
	 */
	public static ExcelTemplateConf parse(File excelFile) {
		final ExcelTemplateConf conf = new ExcelTemplateConf();
		// 解析excel
		parseExcelFile(conf, excelFile);
		
		return conf;
	}
	
	/**
	 * 解析excel文件，生成excelconf对象
	 * 
	 * @param conf 
	 * @param excelFile
	 */
	private static void parseExcelFile(ExcelTemplateConf conf, File excelFile) {
		Workbook workbook = parseWorkBook(excelFile);
		// 总表格数
		int totalSheet = workbook.getNumberOfSheets();
		for (int i = 0; i < totalSheet; i++) {
			Sheet sheet = workbook.getSheetAt(i);
			// 表名
			String name = sheet.getSheetName();
			// 非法命名忽略
			if(!name.contains("|")) 
				throw new ParserException("sheet: "+ name +" , 非规则命名，忽略处理!所属文件：" + excelFile.getAbsolutePath());
			
			// 数据的总行数小于数据头行数
			int lastRowNum = sheet.getLastRowNum() + 1;
			if (lastRowNum < ConfigPropParser.FLAG_HEAD_LEN)
				throw new ParserException("数据表行数错误。数据头行数必须为:" + ConfigPropParser.FLAG_HEAD_LEN + ",当前数据行数：" + lastRowNum);
			
			// 是否已定义此命名
			if (NAMING_TO_PATH.containsKey(name))
				throw new ParserException("存在重复的sheet命名.原命名" + NAMING_TO_PATH.get(name) + ",重复命名：" + excelFile.getPath() + "-" + name);
			
			try {
				// 添加到全局队列中
				NAMING_TO_PATH.put(name, excelFile.getPath() + "-" + name + "-" + excelFile.getName());
				// 表英文名
				String nameEN = Utils.toUpperFristChar(name.substring(name.indexOf("|") + 1, name.length()));
				// 解析java模板
				JavaTemplateConf javaTemplateConf = parseToJavaTemplateConf(sheet);
				javaTemplateConf.class_inform = name + ".所属文件:" + excelFile.getName();
				// 解析json数据
				JsonDataConf jsonDataConf = parseToJsonDataConf(sheet);
				
				conf.namings.add(nameEN);
				conf.java_template_collections.put(nameEN, javaTemplateConf);
				conf.json_data_collections.put(nameEN, jsonDataConf);
			} catch (Exception e) {
				throw new ParserException("sheet: "+ name +" , 非规则命名，忽略处理!所属文件：" + excelFile.getAbsolutePath(), e);
			}
		}
	}

	/**
	 * 解析excel工作薄
	 * @param excelFile
	 * @return
	 */
	private static Workbook parseWorkBook(File excelFile) {
		Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(new FileInputStream(excelFile));
			evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		} catch (EncryptedDocumentException | InvalidFormatException | IOException e) {
			throw new ParserException("workbook解析错误.excel文件：" + excelFile.getAbsolutePath(), e);
		}
		return workbook;
	}
	
	/**
	 * 是否服务器端标识
	 * @param row																			
	 * @param cellIndex
	 * @return
	 */
	private static boolean isServerFlag(Row row, int cellIndex) {
		String serverOrClient = row.getCell(cellIndex).getStringCellValue().toLowerCase();
		// 只能包含2个字符
		if (serverOrClient.length() > 2)
			return false;
		
		// 存在一个字符时必须包含s
		if (serverOrClient.length() == 1 && !serverOrClient.contains("s"))
			return false;
		
		// 存在2个字符时必须包含cs
		if (serverOrClient.length() == 2 && !serverOrClient.contains("cs"))
			return false;
		
		return true;
	}

	/**
	 * 解析为java模板配置
	 * @param sheet
	 * @return
	 * @throws Exception
	 */
	private static JavaTemplateConf parseToJavaTemplateConf(Sheet sheet) throws Exception {
		Row desc_row = sheet.getRow(ConfigPropParser.FLAG_VARIABLE_DESC_ROW);
		Row cs_row = sheet.getRow(ConfigPropParser.FLAG_CS_ROW);
		Row naming_row = sheet.getRow(ConfigPropParser.FLAG_VARIABLE_NAMING_ROW);
		Row type_row = sheet.getRow(ConfigPropParser.FLAG_VARIABLE_TYPE_ROW);
		
		// 数据总列数
		JavaTemplateConf templateConf = new JavaTemplateConf();
		int totalColumn = sheet.getRow(ConfigPropParser.FLAG_CS_ROW).getLastCellNum();
		for (int i = 0; i < totalColumn; i++) {
			// 判断是否服务器端标识
			if (!isServerFlag(cs_row, i))
				continue;
			
			JavaVarConf columnConf = new JavaVarConf();
			columnConf.variable_naming = Utils.toLowerCaseFristChar(naming_row.getCell(i).getStringCellValue());
			columnConf.variable_desc = ExcelCellParser.caseToVariableDesc(desc_row.getCell(i).getStringCellValue(), type_row.getCell(i).getStringCellValue());
			columnConf.variable_type = ExcelCellParser.caseToVariableType(type_row.getCell(i).getStringCellValue());
			
			templateConf.vars.add(columnConf);
		}
		
		return templateConf;
	}

	/**
	 * 解析为json数据配置
	 * @param sheet
	 * @return
	 */
	private static JsonDataConf parseToJsonDataConf(Sheet sheet) throws Exception {
		Row cs_row = sheet.getRow(ConfigPropParser.FLAG_CS_ROW);
		Row naming_row = sheet.getRow(ConfigPropParser.FLAG_VARIABLE_NAMING_ROW);
		Row type_row = sheet.getRow(ConfigPropParser.FLAG_VARIABLE_TYPE_ROW);
		
		// 数据总列数
		JsonDataConf jsonDataConf = new JsonDataConf();
		int totalColumn = sheet.getRow(ConfigPropParser.FLAG_CS_ROW).getLastCellNum();
		
		for (int rowNum = ConfigPropParser.FLAG_HEAD_LEN; rowNum < sheet.getLastRowNum(); rowNum++) {
			Row data_row = sheet.getRow(rowNum);
			if (data_row == null)
				continue;
			
			JSONObject jsonObj = new JSONObject(new LinkedHashMap<>()); 
			for (int columnNum = 0; columnNum < totalColumn; columnNum++) {
				Cell data_cell = data_row.getCell(columnNum);
				// 如果第一列为#END，则直接返回
				if (columnNum == 0 && (data_cell != null && data_cell.getCellTypeEnum() == CellType.STRING 
							&& data_cell.getStringCellValue().toUpperCase().equalsIgnoreCase("#END")))
					break;
				
				// 判断是否服务器端标识
				if (!isServerFlag(cs_row, columnNum))
					continue;
		
				Object jsonValue = ExcelCellParser.parse(sheet, data_cell, type_row.getCell(columnNum).getStringCellValue(), evaluator);
				jsonObj.put(Utils.toLowerCaseFristChar(naming_row.getCell(columnNum).getStringCellValue()), jsonValue);
			}
			if (!jsonObj.isEmpty()) {
				jsonDataConf.jsons.add(jsonObj.toJSONString());
			}
		}
		
		return jsonDataConf;
	}


}
