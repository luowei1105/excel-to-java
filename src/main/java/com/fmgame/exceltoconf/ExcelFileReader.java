package com.fmgame.exceltoconf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * excel文件读取
 * 
 * @author luowei
 * @date 2017年10月10日 上午11:57:20
 */
final class ExcelFileReader {
	
	/**
	 * 读取excel文件
	 * 
	 * @return
	 */
	public static List<File> readExcelFiles() {
		File rootFile = new File(ConfigPropParser.EXCEL_SOURCE_FILE_PATH);
		if (!rootFile.isDirectory())
			throw new ParserException("Excel源文件的地址必须为一个目录." + rootFile.getAbsolutePath());
		
		// 声明文件列表
		List<File> allFiles = new ArrayList<>();
		// 读取文件到列表中
		readFileToList(allFiles, rootFile);
		// 过滤excel文件
		return allFiles.parallelStream()
			.filter(subFile -> subFile.getName().toLowerCase().matches("^.+(\\.xls|\\.xlsx|\\.xlsm)$") && !subFile.getName().toLowerCase().contains("~$"))
			.collect(Collectors.toList());
	}
	
	/**
	 * 读取文件及所有子文件到列表中
	 * 
	 * @param allFiles
	 * @param readFile
	 */
	private static void readFileToList(List<File> allFiles, File readFile) {
		if (allFiles == null || readFile == null)
			return;
		File[] files = readFile.listFiles();
		if (files == null)
			return;
		
		for (File subFile : files) {
			if (subFile.isDirectory()) {
				readFileToList(allFiles, subFile);
			} else {
				allFiles.add(subFile);
			}
		}
	}
	
}
