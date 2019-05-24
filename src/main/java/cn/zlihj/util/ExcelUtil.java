package cn.zlihj.util;

import cn.zlihj.enums.WorkType;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelUtil {
	private static final String[] HEADERS = {
			"姓名",
			"类型",
			"性别",
			"学校",
			"专业",
			"手机号",
			"QQ",
			"微信",
			"广讯通",
			"邮箱",
			"工作位置",
			"来源",
			"部门/项目"
	};

	private static final int[] WIDTHS = {
			1000,
			1500,
			1000,
			2000,
			3000,
			2000,
			2000,
			2000,
			2000,
			2000,
			3000,
			1000,
			3000
	};

	/**
	 * 从文件里面读取excel内容
	 * 
	 */
	public static List<String[]> readFromFile(File wbFile, int lineNum, int maxRowNum) throws FileNotFoundException, IOException {
		if (wbFile == null) {
			return null;
		}
		return readFromFile(wbFile.getName(), new FileInputStream(wbFile), lineNum, maxRowNum);
	}

	/**
	 * 从文件里面读取excel内容
	 * 
	 */
	public static List<String[]> readFromFile(MultipartFile wbFile, int lineNum, int maxRowNum) throws FileNotFoundException, IOException {
		if (wbFile == null) {
			return null;
		}
		return readFromFile(wbFile.getOriginalFilename(), wbFile.getInputStream(), lineNum, maxRowNum);
	}

	/**
	 * 从文件里面读取excel内容
	 * 
	 */
	private static List<String[]> readFromFile(String name, InputStream fileInputStream, int lineNum, int maxRowNum) throws FileNotFoundException, IOException {
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

	private static List<String[]> readFrom2003File(InputStream fileInputStream, int lineNum, int maxRowNum) throws OfficeXmlFileException, IOException {
		try {
			// 默认是用2003读取
			HSSFWorkbook wb = new HSSFWorkbook(fileInputStream);
			return readFromFile(wb.getSheetAt(0), lineNum, maxRowNum);
		} finally {
			fileInputStream.close();
		}
	}

	private static List<String[]> readFrom2007File(InputStream fileInputStream, int lineNum, int maxRowNum) throws OfficeXmlFileException, IOException {
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
	private static List<String[]> readFromFile(Sheet sheet, int lineNum, int maxRowNum) throws FileNotFoundException, IOException {
		List<String[]> returns = new ArrayList<>();
		int lastRowNum = sheet.getLastRowNum();
		if (maxRowNum < lastRowNum) {
			throw new RuntimeException("超过最大行数[" + maxRowNum + "]");
		}
		int rowNum = 0;
		Object tmp = null;
		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row next = rowIterator.next();
			Cell first = next.getCell(0);
			if (first == null || !StringUtils.hasText(((XSSFCell) first).getRawValue())) {
				break;
			}

			String[] arr = new String[lineNum];
			for (int j = 0; j < lineNum; j++) {
				tmp = null;
				Cell cell = next.getCell(j);
				if (cell != null) {
					switch (cell.getCellType()) {
					case HSSFCell.CELL_TYPE_NUMERIC: // 数字
						NumberFormat numberFormat = NumberFormat.getInstance();
						numberFormat.setGroupingUsed(false);
						tmp = numberFormat.format(cell.getNumericCellValue());
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
					arr[j] = tmp.toString();
				}
			}
			returns.add(arr);
			rowNum++;
		}
		return returns;
	}

	public static String downloadTemplate(HttpServletResponse response, String outputName, String[] arr) {
		OutputStream os = null;
		try {
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			outputName = new String(outputName.getBytes("utf-8"), "iso8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=" + outputName);
			XSSFWorkbook workbook = genTemplate(arr);
			os = response.getOutputStream();
			workbook.write(os);
		} catch (Exception e) {
			throw new RuntimeException("downloadTemplate error", e);
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					throw new RuntimeException("downloadTemplate 关闭outputStream error", e);
				}
		}
		return null;
	}

	private static XSSFWorkbook genTemplate(String[] arr) {
		// 创建工作薄
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("导入人员信息");

		// 表头
		XSSFRow row = sheet.createRow(0);
		for (int i=0;i<HEADERS.length;i++) {
			XSSFCell cell = row.createCell(i);
			XSSFCellStyle style = workbook.createCellStyle();
			style.setAlignment(HorizontalAlignment.CENTER);
			style.setWrapText(false);
			XSSFFont font = workbook.createFont();
			font.setFontHeight(16);
			font.setBold(true);
			style.setFont(font);
			cell.setCellStyle(style);
			cell.setCellValue(HEADERS[i]);

			sheet.setColumnWidth(i, WIDTHS[i] * 4);
		}

		// 下拉选择
		XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);

		// 类型验证
		WorkType[] values = WorkType.values();
		String[] types = new String[values.length];
		for (int i=0;i<values.length;i++) {
			types[i] = values[i].getText();
		}
		sheet.addValidationData(createValidation(dvHelper, 1, types));

		// 性别验证
		sheet.addValidationData(createValidation(dvHelper, 2, new String[] { "男", "女" }));

		// 来源验证
		sheet.addValidationData(createValidation(dvHelper, 11, new String[] { "总部", "项目簇" }));

		// 具体验证
		sheet.addValidationData(createValidation(dvHelper, 12, arr));

		return workbook;
	}

	private static XSSFDataValidation createValidation(XSSFDataValidationHelper dvHelper, int col, String[] arr) {
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
				.createExplicitListConstraint(arr);

		CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, col, col);
		XSSFDataValidation validation = (XSSFDataValidation) dvHelper
				.createValidation(dvConstraint, addressList);
		return validation;
	}
}
