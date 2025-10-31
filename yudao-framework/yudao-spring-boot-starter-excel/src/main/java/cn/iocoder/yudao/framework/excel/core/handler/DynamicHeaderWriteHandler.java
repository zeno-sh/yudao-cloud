package cn.iocoder.yudao.framework.excel.core.handler;

import cn.idev.excel.write.handler.SheetWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: Jax
 * @Date: Created in 20:21 2025/9/25
 */
public class DynamicHeaderWriteHandler implements SheetWriteHandler {


    private final LinkedHashMap<String, String> headerMap;

    public DynamicHeaderWriteHandler(LinkedHashMap<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    @Override
    public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {

    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        Sheet sheet = writeSheetHolder.getSheet();

        // 设置第一行内容

        // 设置标题行
        Row row2 = sheet.createRow(1);
        row2.setHeight((short) 800);
        Cell cell1 = row2.createCell(0);
        cell1.setCellValue("采购单明细");
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 20); // 调整字体大小
        cellStyle.setFont(font);
        cell1.setCellStyle(cellStyle);
        sheet.addMergedRegionUnsafe(new CellRangeAddress(1, 1, 0, 10));


        // 动态添加 headerMap
        Row row4 = sheet.createRow(2);
        int cellIndex = 0;
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            Cell keyCell = row4.createCell(cellIndex++);
            keyCell.setCellValue(entry.getKey() + entry.getValue());
        }
    }

}
