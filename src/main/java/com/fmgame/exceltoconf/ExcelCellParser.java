package com.fmgame.exceltoconf;

import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import com.alibaba.fastjson.JSON;

/**
 * excel 列解析
 * 
 * @author luowei
 * @date 2017年10月10日 下午5:50:00
 */
class ExcelCellParser {

	/**
	 * 转换变量描述
	 * @param desc
	 * @param dataType
	 * @return
	 */
	public static String caseToVariableDesc(String desc, String dataType) {
		if (dataType.toLowerCase().contains("json")){
			return desc + "(数据格式:JSONObject)";
		}  else if (dataType.toLowerCase().contains("[]")) {
			return desc + "(数据格式:JSONArray)";
		}
		return desc;
	}

	/**
	 * 转换变量类型
	 * @param dataType
	 * @return
	 */
	public static String caseToVariableType(String dataType) {
		if (dataType.toLowerCase().contains("byte")) {
			return "byte";
		} else if (dataType.toLowerCase().contains("short")) {
			return "short";
		} else if (dataType.toLowerCase().contains("integer") || dataType.toLowerCase().contains("int")) {
			return "int";
		} else if (dataType.toLowerCase().contains("float")) {
			return "float";
		} else if (dataType.toLowerCase().contains("double")) {
			return "double";
		} else if (dataType.toLowerCase().contains("boolean") || dataType.toLowerCase().contains("bool")) {
			return "boolean";
		} else if (dataType.toLowerCase().contains("long")) {
			return "long";
		} else if (dataType.toLowerCase().contains("json") || dataType.toLowerCase().contains("[]")){
			return "String";
		} else {
			return "String";
		}
	}

	/**
	 * 解析excel 单元列
	 * 
	 * @param sheet
	 * @param cell
	 * @param dataType
	 * @param evaluator
	 * @return
	 * @throws Exception
	 */
	public static Object parse(Sheet sheet, Cell cell, String dataType, FormulaEvaluator evaluator) throws Exception {
		// 设置默认值
		if (cell == null || cell.getCellTypeEnum() == CellType.BLANK)
			cell = setDefaultValue(sheet, dataType);

		// 处理数据
		switch (cell.getCellTypeEnum()) {
			case NUMERIC: // 数字类型
				// 去除科学计数法
				return formatSciNot(dataType, cell.getNumericCellValue());
			case STRING: // 字符串类型
				String value = cell.getStringCellValue();
				// 验证数据配置是否正确
				return checkDataFormat(dataType, value);
			case BOOLEAN: // boolean类型
				if (!dataType.toLowerCase().equals("boolean") && !dataType.toLowerCase().equals("bool"))
					throw new Exception("数据类型不应当是Boolean类型");
				return cell.getBooleanCellValue();
			case FORMULA: // 公式类型
				return checkDataForFormula(cell, dataType, evaluator);
			default:
				throw new Exception("未知数据类型！");
		}
	}

	/**
	 * 设置默认值
	 * @param sheet
	 * @param colume
	 */
	public static Cell setDefaultValue(Sheet sheet, String dateType) {
		// 如果单元为空 在最大单元格创建默认值，不改变原单元格内容 16383 TODO 这个在03的Excel中可能有问题
		Cell cell = sheet.createRow(16383).createCell(16383);
		if (dateType.equalsIgnoreCase("byte") || dateType.equalsIgnoreCase("short") 
				|| dateType.equalsIgnoreCase("int") || dateType.equalsIgnoreCase("long")) {
			cell.setCellValue(0);
		} else if (dateType.equalsIgnoreCase("float") || dateType.equalsIgnoreCase("double")) {
			cell.setCellValue(0.0);
		} else if (dateType.equalsIgnoreCase("boolean")) {
			cell.setCellValue(false);
		} else {
			cell.setCellValue("");
		}

		return cell;
	}

	/**
	 * 去除科学计数法
	 * @param value
	 * @return
	 * @throws Exception 
	 */
	private static Object formatSciNot(String dataType, double value) throws Exception {
		BigDecimal bd = new BigDecimal(value);
		return caseTo(dataType, bd.toString());
	}

	/**
	 * 验证配置数据格式
	 * @param dataType
	 * @param strVal
	 * @return
	 * @throws Exception
	 */
	private static String checkDataFormat(String dataType, String strVal) throws Exception {
		if (dataType.equalsIgnoreCase("json")) {
			return checkJsonParse(strVal);
		} else if (dataType.contains("[]")) { // 数组类型
			return checkArrayParse(dataType, strVal);
		} else {
			if (strVal.length() == 0 //
					|| "null".equals(strVal) //
					|| "NULL".equals(strVal)) {
				return "";
			}
			return strVal;
		}
	}

	/**
	 * 验证JSON 字符串
	 * @param strVal
	 * @return
	 */
	private static String checkJsonParse(String strVal) throws Exception {
		// 验证是否包含JSON关键字符
		if (strVal == null || "".equals(strVal))
			throw new Exception("JSON类型数据不允许为空，必须有默认值！");

		// 转换特殊数据 : [0x111232,0x1112312]
		strVal = convert0xData(strVal);
		try {
			JSON.parse(strVal);
		} catch (Exception e) {
			throw new Exception("错误的JSON格式=" + strVal);
		}
		return strVal;
	}

	/**
	 * 将[[0x10001010, 0x10002010],[0x10002010]] 这种数据变为实际的数值,只支持10位的!! 防止json验证问题，以及数据读取时的问题
	 * @param str
	 * @return
	 */
	private static String convert0xData(String str) {
		if (!str.contains("0x"))
			return str;

		String tmp[] = str.split(",");
		for (String s : tmp) {
			// 获得 0x1000101
			String s1 = s.substring(s.indexOf("0x"), (s.indexOf("0x") + 10));
			// 将0x1000101 解析成数字后 再转成字符串准备替换
			String s2 = String.valueOf(Integer.decode(s1));
			// 将解析后的数字替换0x10000101
			str = str.replaceAll(s1, s2);
		}

		return str;
	}

	/**
	 * 数组数据验证 value,value2,value3
	 * @param strVal
	 */
	private static String checkArrayParse(String dataType, String strVal) throws Exception {
		if (strVal == null || strVal.isEmpty() || strVal.equals(""))
			throw new Exception("未设置数据！至少请设置一个默认值");

		// 按照不同类型转一遍 看看是否有错误
		String[] tmp = strVal.split(",");
		// 没数据
		if (tmp.length < 1)
			return strVal;

		for (String s : tmp) {
			caseTo(dataType, s);
		}
		
		return strVal;
		
	}

	/**
	 * 基础类型转型检测
	 * @param dataType
	 * @param value
	 */
	private static Object caseTo(String dataType, String value) throws Exception {
		if (dataType.toLowerCase().contains("byte")) {
			return Byte.parseByte(value);
		} else if (dataType.toLowerCase().contains("short")) {
			return Short.parseShort(value);
		} else if (dataType.toLowerCase().contains("integer") || dataType.toLowerCase().contains("int")) {
			return Integer.parseInt(value);
		} else if (dataType.toLowerCase().contains("float")) {
			return Float.parseFloat(value);
		} else if (dataType.toLowerCase().contains("double")) {
			return Double.parseDouble(value);
		} else if (dataType.toLowerCase().contains("boolean") || dataType.toLowerCase().contains("bool")) {
			return Boolean.parseBoolean(value);
		} else if (dataType.toLowerCase().contains("long")) {
			return Long.parseLong(value);
		} else {
			return String.valueOf(value);
		}
	}

	/**
	 * 处理公式型数据
	 * @param map
	 * @param type
	 * @param cell
	 * @param evaluator 
	 * @throws Exception
	 */
	private static Object checkDataForFormula(Cell cell, String dataType, FormulaEvaluator evaluator) throws Exception {
		String value = "";
		// 公式数据 有可能是字符串的 也可能是数字的，所以先尝试用字符串的方法取
		try {
			// 用字符串的方法取，即使取不到抛异常也不用关心，下面还会用数字的方法取
			value = cell.getStringCellValue();
		} catch (IllegalStateException e) {
		}
		
		// 如果取到，包含0x的处理
		if (value.indexOf("0x") != -1) {
			return Integer.decode(value);
		} else if (dataType.equalsIgnoreCase("json")) { // 如果是字符串 要验证下是不是JSON格式
			return checkJsonParse(value);
		} else if (!"".equals(value)) { // 如果取到不为空 返回数据
			return value;
		} else { // 最后用一个高大尚的方法取 原本Poi有一个类专门处理公式类型... 为了不影响之前的代码，先写在这吧
			CellValue cellValue = evaluator.evaluate(cell);
			switch (cellValue.getCellTypeEnum()) {
				case BOOLEAN:
					return cellValue.getBooleanValue();
				case NUMERIC:
					// 去除科学计数法
					return formatSciNot(dataType, cellValue.getNumberValue());
				default:
					return cellValue.getStringValue();
			}
		}

	}

}
