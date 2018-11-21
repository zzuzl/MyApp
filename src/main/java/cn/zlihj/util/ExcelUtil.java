package cn.zlihj.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Iterator;

public class ExcelUtil {

	/**
	 * 从文件里面读取excel内容
	 * 
	 */
	public static String[][] readFromFile(File wbFile, int lineNum, int maxRowNum) throws FileNotFoundException, IOException {
		if (wbFile == null) {
			return null;
		}
		return readFromFile(wbFile.getName(), new FileInputStream(wbFile), lineNum, maxRowNum);
	}

	/**
	 * 从文件里面读取excel内容
	 * 
	 */
	public static String[][] readFromFile(MultipartFile wbFile, int lineNum, int maxRowNum) throws FileNotFoundException, IOException {
		if (wbFile == null) {
			return null;
		}
		return readFromFile(wbFile.getOriginalFilename(), wbFile.getInputStream(), lineNum, maxRowNum);
	}

	/**
	 * 从文件里面读取excel内容
	 * 
	 */
	private static String[][] readFromFile(String name, InputStream fileInputStream, int lineNum, int maxRowNum) throws FileNotFoundException, IOException {
		try {
			if (name.toLowerCase().endsWith(".xlsx")) {
				// 默认是用2007读取
				return readFrom2007File(fileInputStream, lineNum, maxRowNum);
			} else {
				return readFrom2003File(fileInputStream, lineNum, maxRowNum);
			}
		} catch (OfficeXmlFileException e) {
			// 修正由于命名错误引起的解析错误
			return readFrom2007File(fileInputStream, lineNum, maxRowNum);
		}
	}

	private static String[][] readFrom2003File(InputStream fileInputStream, int lineNum, int maxRowNum) throws OfficeXmlFileException, IOException {
		try {
			// 默认是用2003读取
			HSSFWorkbook wb = new HSSFWorkbook(fileInputStream);
			return readFromFile(wb.getSheetAt(0), lineNum, maxRowNum);
		} finally {
			fileInputStream.close();
		}
	}

	private static String[][] readFrom2007File(InputStream fileInputStream, int lineNum, int maxRowNum) throws OfficeXmlFileException, IOException {
		// 兼容2007
		try {
			XSSFWorkbook wb = new XSSFWorkbook(fileInputStream);
			return readFromFile(wb.getSheetAt(0), lineNum, maxRowNum);
		} finally {
			fileInputStream.close();
		}
	}

	/**
	 * 从文件里面读取excel内容
	 * 
	 */
	private static String[][] readFromFile(Sheet sheet, int lineNum, int maxRowNum) throws FileNotFoundException, IOException {
		String[][] returns;
		int lastRowNum = sheet.getLastRowNum();
		if (maxRowNum < lastRowNum) {
			throw new RuntimeException("超过最大行数[" + maxRowNum + "]");
		}
		returns = new String[lastRowNum + 1][lineNum];
		int rowNum = 0;
		Object tmp = null;
		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row next = rowIterator.next();
			for (int j = 0; j < lineNum; j++) {
				tmp = null;
				Cell cell = next.getCell(j);
				if (cell != null) {
					switch (cell.getCellType()) {
					case HSSFCell.CELL_TYPE_NUMERIC: // 数字
						tmp = cell.getNumericCellValue();
						break;
					case HSSFCell.CELL_TYPE_STRING: // 字符串
						tmp = cell.getStringCellValue();
						break;
					case HSSFCell.CELL_TYPE_BOOLEAN: // Boolean
						tmp = cell.getBooleanCellValue();
						break;
					case HSSFCell.CELL_TYPE_FORMULA: // 公式
						tmp = cell.getCellFormula();
						break;
					case HSSFCell.CELL_TYPE_BLANK: // 空值
						tmp = " ";
						break;
					case HSSFCell.CELL_TYPE_ERROR: // 故障
						tmp = null;
						break;
					default:
						tmp = cell.getStringCellValue();
						break;
					}
				}
				if (tmp != null) {
					returns[rowNum][j] = tmp.toString();
				}
			}
			rowNum++;
		}
		return returns;
	}
}
