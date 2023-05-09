package com.dhl.fin.api.common.util.excel;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.StyleSet;
import com.dhl.fin.api.common.annotation.ExcelTitle;
import com.dhl.fin.api.common.domain.BasicDomain;
import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.ExcelCellAlignment;
import com.dhl.fin.api.common.enums.ExcelDataType;
import com.dhl.fin.api.common.enums.LogStatus;
import com.dhl.fin.api.common.exception.BusinessException;
import com.dhl.fin.api.common.exception.excel.ExcelFieldNotFoundException;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.DateUtil;
import com.dhl.fin.api.common.util.*;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by CuiJianbo on 2020.03.22.
 */
@Data
public class ExcelUtil {

    private List<ExcelSheet> excelSheets = new LinkedList<>();
    private String fileName;
    private Boolean isTrailDate = false;
    public static Integer dataCount = 0;

    public static ExcelBuilder builder() {
        return new ExcelBuilder();
    }

    private static List readData(ExcelReader reader, Class beanType) {
        try {
            List dataList = new LinkedList();
            if (ObjectUtil.notNull(beanType)) {
                if (beanType.equals(Map.class)) {
                    return reader.readAll();
                } else {
                    List<Map<String, Object>> datas = reader.readAll();
                    Map<String, Map<String, String>> titleKeyMap = titleKeyMap(beanType);
                    for (Map<String, Object> data : datas) {
                        Object o = beanType.newInstance();
                        for (Map.Entry<String, Object> entity : data.entrySet()) {
                            Map d = titleKeyMap.get(entity.getKey());
                            if (ObjectUtil.notNull(d)) {
                                String fieldName = MapUtil.getString(d, "field");

                                String defaultValue;
                                if (fieldName.endsWith("Str")) {
                                    Field field = ReflectUtil.getField(o.getClass(), fieldName);
                                    defaultValue = field.getDeclaredAnnotation(ExcelTitle.class).defaultV();
                                    ReflectUtil.setFieldValue(o, fieldName, entity.getValue());
                                    fieldName = fieldName.substring(0, fieldName.length() - 3);
                                } else {
                                    Field field = ReflectUtil.getField(o.getClass(), fieldName);
                                    defaultValue = field.getDeclaredAnnotation(ExcelTitle.class).defaultV();
                                }


                                Field field = ReflectUtil.getField(o.getClass(), fieldName);
                                Class fieldType = field.getType();
                                Object fieldValue;
                                Object entityValue = entity.getValue();
                                if (ObjectUtil.isNull(entityValue)
                                        || StringUtil.isEmpty(entityValue.toString())) {
                                    if (StringUtil.isEmpty(defaultValue)) {
                                        continue;
                                    } else {
                                        entityValue = defaultValue;
                                    }
                                }
                                if (fieldType.equals(Integer.class)) {
                                    fieldValue = Integer.valueOf(entityValue.toString());
                                    ReflectUtil.setFieldValue(o, fieldName, fieldValue);
                                } else if (fieldType.equals(Long.class)) {
                                    fieldValue = Long.valueOf(entityValue.toString());
                                    ReflectUtil.setFieldValue(o, fieldName, fieldValue);
                                } else if (fieldType.equals(Double.class)) {
                                    fieldValue = Double.valueOf(entityValue.toString());
                                    ReflectUtil.setFieldValue(o, fieldName, fieldValue);
                                } else if (fieldType.equals(Date.class) || fieldType.equals(Timestamp.class)) {
                                    DateUtil.parseDateField(o, entityValue.toString(), field);
                                } else {
                                    ReflectUtil.setFieldValue(o, fieldName, entityValue);
                                }
                            }
                        }
                        dataList.add(o);
                    }
                    return dataList;
                }
            } else {
                return reader.readAll();
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Map<String, Map<String, String>> titleKeyMap(Class beanType) {
        Map map = MapUtil.builder().build();
        Arrays.stream(ReflectUtil.getFields(beanType)).
                filter(p -> ObjectUtil.notNull(p.getDeclaredAnnotation(ExcelTitle.class)))
                .map(p -> {
                            ExcelTitle excelTitle = p.getDeclaredAnnotation(ExcelTitle.class);
                            String titleName = excelTitle.name();
                            String titleCode = excelTitle.code();
                            String code = StringUtil.isEmpty(titleCode) ? StringUtil.toUnderlineCase(p.getName()) : titleCode;
                            return MapUtil.builder()
                                    .add("titleName", titleName)
                                    .add("info", MapUtil.builder()
                                            .add("field", p.getName())
                                            .add("titleCode", code)
                                            .build())
                                    .build();
                        }
                ).forEach(p -> {
            map.put(p.get("titleName"), p.get("info"));
        });
        return map;
    }

    /**
     * 从本地excel取数据，返回List<Map>数据类型
     *
     * @param filePath
     * @return List<Map>
     */
    public static List getExcelFromLocal(String filePath) {
        return getExcelFromLocal(filePath, Map.class);
    }

    public static List getExcelFromLocal(File file) {
        return getExcelFromLocal(file, Map.class);
    }

    /**
     * 从本地excel获取数据，返回List<beanType>数据类型
     *
     * @param filePath
     * @param beanType 数据class
     * @return List<beanType>
     */
    public static List getExcelFromLocal(String filePath, Class beanType) {
        ExcelReader reader = cn.hutool.poi.excel.ExcelUtil.getReader(FilesUtil.getFile(filePath));
        List data = readData(reader, beanType);
        reader.close();
        return data;
    }


    public static List getExcelFromLocal(File file, Class beanType) {
        ExcelReader reader = cn.hutool.poi.excel.ExcelUtil.getReader(file);
        List data = readData(reader, beanType);
        reader.close();
        return data;
    }

    public static List getExcelFromStream(InputStream fileSteam) {
        ExcelReader reader = cn.hutool.poi.excel.ExcelUtil.getReader(fileSteam);
        List data = readData(reader, null);
        reader.close();
        return data;
    }


    /**
     * 从本地excel文件的某几个sheet里获取数据，返回Map<sheetName,List<beanType>>数据类型
     *
     * @param filePath
     * @param beanType
     * @param sheetNames
     * @return Map<sheetName, List < beanType>>
     */
    public static Map<String, List> getExcelFromLocal(String filePath, Class beanType, String... sheetNames) {
        MapBuilder mapBuilder = MapUtil.builder();
        for (String sheetName : sheetNames) {
            if (StringUtil.isNotEmpty(sheetName.trim())) {
                ExcelReader reader = cn.hutool.poi.excel.ExcelUtil.getReader(FilesUtil.getFile(filePath), sheetName.trim());
                List data = readData(reader, beanType);
                mapBuilder.add(sheetName, data);
                reader.close();
            }
        }
        return mapBuilder.build();
    }

    /**
     * 从request获取excel数据，返回List<Map>数据类型
     *
     * @param request
     * @return List<Map>
     */
    public static List getExcelFromRequest(MultipartHttpServletRequest request) throws ExcelFieldNotFoundException, IOException {
        return getExcelFromRequest(request, Map.class);
    }


    /***
     *从request获取excel数据，返回List<beanType>数据类型
     * @param request
     * @param beanType
     * @return List<beanType>
     */
    public static List getExcelFromRequest(MultipartHttpServletRequest request, Class beanType) throws ExcelFieldNotFoundException, IOException {
        Set<Map.Entry<String, MultipartFile>> fileSet = request.getFileMap().entrySet();
        if (fileSet.size() > 0) {
            for (Map.Entry<String, MultipartFile> fileEntry : fileSet) {
                MultipartFile file = fileEntry.getValue();
                InputStream in = file.getInputStream();
                ExcelReader reader = cn.hutool.poi.excel.ExcelUtil.getReader(in);
                checkExcelField(reader, beanType);
                return readData(reader, beanType);
            }
        }
        return null;
    }


    /**
     * 从本地excel文件的某几个sheet里获取数据，返回Map<sheetName,List<beanType>>数据类型
     *
     * @param request
     * @param beanType
     * @param sheetNames
     * @return Map<sheetName, List < beanType>>
     */
    public static <T> Map<String, List<T>> getExcelFromRequest(MultipartHttpServletRequest request, Class<T> beanType, String... sheetNames) throws IOException {
        Set<Map.Entry<String, MultipartFile>> fileSet = request.getFileMap().entrySet();
        if (fileSet.size() > 0) {
            for (Map.Entry<String, MultipartFile> fileEntry : fileSet) {
                MultipartFile file = fileEntry.getValue();
                MapBuilder mapBuilder = MapUtil.builder();
                for (String sheetName : sheetNames) {
                    if (StringUtil.isNotEmpty(sheetName.trim())) {
                        InputStream in = file.getInputStream();
                        try {
                            ExcelReader reader = cn.hutool.poi.excel.ExcelUtil.getReader(in, sheetName.trim());
                            mapBuilder.add(sheetName, readData(reader, beanType));
                        } catch (Exception e) {

                        }
                    }
                }

                return mapBuilder.build();
            }
        }
        return null;
    }


    /**
     * 写excel到本地
     * 返回文件路径
     *
     * @param
     */
    public String writeToLocal() throws Exception {
        return writeToLocal(null);
    }

    /**
     * 写excel到本地
     * 返回文件路径
     *
     * @param localDir
     */
    public String writeToLocal(String localDir) throws Exception {
        String filePath = "";
        try {
            if (StringUtil.isNotEmpty(fileName)) {
                String path = SpringContextUtil.getPropertiesValue("custom.uploadPath");

                if (StringUtil.isEmpty(path) && StringUtil.isEmpty(localDir)) {
                    return null;
                }

                String os = System.getProperties().getProperty("os.name");
                localDir = StringUtil.isEmpty(localDir) ? path : localDir;
                if ((os.contains("Windows") && localDir.trim().charAt(1) != ':') || (os.contains("Linux") && localDir.trim().charAt(0) != '/')) {
                    localDir = path + File.separator + localDir;
                }

                SimpleDateFormat longSdf = new SimpleDateFormat("MMddHHmmss");
                String currentDate = longSdf.format(DateUtil.getSysTime());
                fileName = isTrailDate ? fileName + "_" + currentDate : fileName;
                filePath = localDir + File.separator + fileName + ".xlsx";
                FileUtil.del(new File(filePath));
                ExcelWriter excelWriter = cn.hutool.poi.excel.ExcelUtil.getBigWriter(filePath);
                buildWriter(excelWriter);
                excelWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ExcelUtil.dataCount = 0;
            throw new BusinessException("", ActionEnum.EXPORT, LogStatus.FAILED, "导出失败：" + e.getCause().getMessage());
        }
        return filePath;
    }


    /**
     * 本地文件写回responose
     *
     * @param filePath
     * @param response
     */
    public static void writeLocalToClient(String filePath, HttpServletResponse response) throws IOException {
        int index1 = filePath.lastIndexOf("\\");
        int index2 = filePath.lastIndexOf("/");
        int index = index1 > index2 ? index1 : index2;
        String fileName = filePath.substring(index + 1, filePath.length());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
        File targetFile = FileUtils.getFile(filePath);
        FileUtils.copyFile(targetFile, response.getOutputStream());
    }

    /**
     * 本地文件写回responose
     *
     * @param response
     * @param file
     * @param downLoadFileName 不带后缀的文件名称
     */
    public static void writeExcelFileToClient(HttpServletResponse response, File file, String downLoadFileName) throws IOException {
        OutputStream output = response.getOutputStream();
        response.reset();
        downLoadFileName = StringUtil.toUtf8String(downLoadFileName);
        response.setHeader("Content-disposition", "attachment; filename=" + downLoadFileName + ".xls");
        response.setContentType("application/msexcel");
        FileUtils.copyFile(file, output);
    }

    /**
     * 写excel到客户端
     *
     * @param response
     */
    public void writeToClient(HttpServletResponse response) throws Exception {
        try {

            SimpleDateFormat longSdf = new SimpleDateFormat("MMddHHmmss");
            String currentDate = longSdf.format(DateUtil.getSysTime());
            String name = StringUtil.isEmpty(fileName) ? "file.xlsx" : fileName;
            name = isTrailDate ? name.split("\\.")[0] + "_" + currentDate + ".xlsx" : name;
            String[] ss = name.split("\\.");
            name = ss[0] + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(name, "UTF-8"));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            ExcelWriter excelWriter = cn.hutool.poi.excel.ExcelUtil.getBigWriter();
            buildWriter(excelWriter);
            excelWriter.flush(response.getOutputStream(), true);
            excelWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            ExcelUtil.dataCount = 0;
            throw new BusinessException("", ActionEnum.EXPORT, LogStatus.FAILED, "导出失败");
        }
    }


    private ExcelWriter buildWriter(ExcelWriter excelWriter) {

        RedisService redisService = SpringContextUtil.getBean(RedisService.class);
        if (CollectorUtil.isEmpty(excelSheets)) {
            return null;
        }


        int index = 0;
        for (ExcelSheet excelSheet : excelSheets) {

            List<ExcelTitleBean> titleSorted = excelSheet.getTitles()
                    .stream()
                    .filter(ExcelTitleBean::isAvailable)
                    .sorted(Comparator.comparingInt(ExcelTitleBean::getSort))
                    .collect(Collectors.toList());

            List<String> titles = titleSorted.stream()
                    .map(p -> p.getKey() + "->" + p.getName())
                    .filter(StringUtil::isNotEmpty)
                    .collect(Collectors.toList());

            String sheetName = excelSheet.getSheetName();
            String headName = excelSheet.getHeadName();
            sheetName = StringUtil.isEmpty(sheetName) ? ("sheet" + (index + 1)) : sheetName;
            excelWriter.setSheet(index++);
            excelWriter.renameSheet(sheetName);
            List<String> titleKeys = new LinkedList<>();
            List<String> titleNames = new LinkedList<>();
            List<String> titleCodes = new LinkedList<>();

            Font font = excelWriter.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 11);
            font.setFontName("宋体");

            Font contentFont = excelWriter.createFont();
            contentFont.setFontHeightInPoints((short) 10);
            contentFont.setFontName("宋体");

            CellStyle defaultStyle = excelWriter.getCellStyle();
            defaultStyle.setFont(font);
            defaultStyle.setBorderLeft(BorderStyle.NONE);
            defaultStyle.setBorderBottom(BorderStyle.NONE);
            defaultStyle.setBorderRight(BorderStyle.NONE);
            defaultStyle.setBorderTop(BorderStyle.NONE);
            defaultStyle.setAlignment(HorizontalAlignment.CENTER);


            CellStyle[] cellTypes = new CellStyle[titles.size()];


            List datas = excelSheet.getRowList();
            for (int i = 0; i < titleSorted.size(); i++) {
                ExcelTitleBean title = titleSorted.get(i);
                String key = title.getKey();
                String code = title.getCode();
                String name = title.getName();
                String dicCode = title.getDicCode();

                titleKeys.add(key);
                titleCodes.add(StringUtil.isNotEmpty(code) ? code : "");
                titleNames.add(StringUtil.isNotEmpty(name) ? name : "");
                excelWriter.addHeaderAlias(Character.toString(((char) (97 + i))), name);

                Map<String, String> dictionaryMap = redisService.getDictionary(dicCode);
                if (StringUtil.isNotEmpty(dicCode) && ObjectUtil.notNull(dictionaryMap)) {
                    String[] dicValues = dictionaryMap.entrySet().stream().map(p -> p.getValue()).toArray(String[]::new);
                    setHSSFValidation(excelWriter, dicValues, 1, datas.size() == 0 ? 200 : datas.size(), i, i);
                }
            }
            long titleKeyNum = titleKeys.stream().filter(StringUtil::isNotEmpty).count();

            if (StringUtil.isNotEmpty(headName)) {
                excelWriter.merge((int) (titleKeyNum - 1), headName);
            }

            long titleNameNum = titleNames.stream().filter(StringUtil::isNotEmpty).count();
            if (titleNameNum > 0) {
                excelWriter.writeRow(titleNames);
            }

            long titleCodesNum = titleCodes.stream().filter(StringUtil::isNotEmpty).count();
            if (titleCodesNum > 0) {
                excelWriter.writeRow(titleCodes);
            }

            for (int i = 0; i < titles.size(); i++) {
                ExcelTitleBean title = titleSorted.get(i);
                Integer width = title.getWidth();
                if (ObjectUtil.notNull(width) && width > 0) {
                    excelWriter.setColumnWidth(i, width);
                }
            }


            int titleNum = 1;
            if (StringUtil.isNotEmpty(excelSheet.getHeadName())) {
                titleNum++;
            }

            if (titleCodesNum > 0) {
                titleNum++;
            }


            for (int i = 0; i < cellTypes.length; i++) {

                ExcelTitleBean titleBean = titleSorted.get(i);
                Integer width = titleBean.getWidth();

                if (ObjectUtil.notNull(width) && width > 0) {
                    excelWriter.setColumnWidth(i, width);
                }

                cellTypes[i] = excelWriter.createCellStyle(i, titleNum);
                cellTypes[i].setFont(contentFont);
                cellTypes[i].setAlignment(HorizontalAlignment.CENTER);

                if (ObjectUtil.notNull(titleBean.getAlignment())) {
                    if (titleBean.getAlignment().equals(ExcelCellAlignment.LEFT)) {
                        cellTypes[i].setAlignment(HorizontalAlignment.LEFT);
                    } else if (titleBean.getAlignment().equals(ExcelCellAlignment.RIGHT)) {
                        cellTypes[i].setAlignment(HorizontalAlignment.RIGHT);
                    }
                }
            }


            CommonDBService commonDBService = excelSheet.getCommonDBService();
            int pageSize = excelSheet.getPageSize();
            String sql = excelSheet.getSql();
            List dataList = null;
            int totalRowSize = 0;


            if (ObjectUtil.notNull(commonDBService) && StringUtil.isNotEmpty(sql)) {
                int l = 1;
                dataList = commonDBService.pageQuery(sql, null, l, pageSize);
                totalRowSize += dataList.size();
                int startY = titleNum;

                while (CollectorUtil.isNoTEmpty(dataList) && dataList.size() == pageSize) {
                    try {
                        commonDBService.dealRow(dataList);
                        writeRowList(excelWriter, excelSheet, dataList, titleKeys, cellTypes, startY);
                        dataList = null;
                        System.gc();

                        l++;
                        dataList = commonDBService.pageQuery(sql, null, l, pageSize);
                        System.out.println("load data size " + dataList.size());
                        dataCount = dataCount - pageSize < 0 ? 0 : dataCount - pageSize;

                        startY += pageSize;

                    } catch (Exception e) {
                        dataList = null;
                        dataCount = dataCount - pageSize < 0 ? 0 : dataCount - pageSize;
                        System.gc();
                        throw e;
                    }

                }

                if (CollectorUtil.isNoTEmpty(dataList)) {
                    int dataSize = dataList.size();
                    try {
                        commonDBService.dealRow(dataList);
                        writeRowList(excelWriter, excelSheet, dataList, titleKeys, cellTypes, startY);
                        dataList = null;
                        System.gc();
                        dataCount = dataCount - dataSize < 0 ? 0 : dataCount - dataSize;
                    } catch (Exception e) {
                        dataList = null;
                        System.gc();
                        dataCount = dataCount - dataSize < 0 ? 0 : dataCount - dataSize;
                        throw e;
                    }
                }
            } else {
                writeRowList(excelWriter, excelSheet, excelSheet.getRowList(), titleKeys, cellTypes, titleNum);
            }

        }


        return excelWriter;


    }

    private void writeRowList(ExcelWriter excelWriter, ExcelSheet excelSheet, List<Object> dataList, List<String> keys, CellStyle[] cellTypes, int startY) {
        if (CollectorUtil.isEmpty(dataList)) {
            return;
        }
        for (int y = 0; y < dataList.size(); y++) {
            Object row = dataList.get(y);

            MapBuilder item = MapUtil.builder();
            if (row instanceof Map) {
                Map<String, ExcelTitleBean> titleMap = new HashMap();
                excelSheet.getTitles().forEach(p -> titleMap.put(p.getKey(), p));
                Map rowData = (Map) row;
                for (int x = 0; x < keys.size(); x++) {
                    String key = keys.get(x);
                    if (StringUtil.isEmpty(key)) continue;
                    Object value = MapUtil.getObject(rowData, key);

                    ExcelDataType excelDataType = titleMap.get(key).getDataType();
                    if (ObjectUtil.notNull(excelDataType) && ObjectUtil.notNull(value)) {
                        String valueStr = value.toString().trim();
                        if (excelDataType.equals(ExcelDataType.INTEGER)) {
                            try {
                                value = Integer.valueOf(valueStr);
                            } catch (NumberFormatException e) {
                            }
                        } else if (excelDataType.equals(ExcelDataType.DOUBLE)) {
                            try {
                                value = Double.valueOf(valueStr);
                            } catch (NumberFormatException e) {
                            }
                        } else if (excelDataType.equals(ExcelDataType.LONG)) {
                            try {
                                value = Long.valueOf(valueStr);
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                    item.add(key, ObjectUtil.isNull(value) ? "" : value);
                }
            } else {
                for (int x = 0; x < keys.size(); x++) {

                    String key = keys.get(x);

                    if (StringUtil.isEmpty(key)) continue;
                    Object value = null;
                    String[] ss = key.split("\\.");
                    if (ss.length == 2) {
                        Object joinDomain = ReflectUtil.getFieldValue(row, StringUtil.toCamelCase(ss[0]));
                        if (joinDomain instanceof BasicDomain) {
                            value = getFieldValue(row, ss[1]);
                        }
                    } else if (ss.length == 1) {
                        value = getFieldValue(row, key);
                    }

                    item.add(key, ObjectUtil.isNull(value) ? "" : value);
                }
            }
            Map rowMap = item.build();

            for (int x = 0; x < keys.size(); x++) {
                Cell cell = excelWriter.getOrCreateCell(x, y + startY);
                cell.setCellStyle(cellTypes[x]);
                Object cellValue = rowMap.get(keys.get(x));
                if (cellValue instanceof Integer) {
                    cell.setCellValue((Integer) cellValue);
                } else if (cellValue instanceof Double || cellValue instanceof BigDecimal) {
                    cell.setCellValue(Double.valueOf(cellValue.toString()));
                } else if (cellValue instanceof Long) {
                    cell.setCellValue((Long) cellValue);
                } else if (cellValue instanceof String) {
                    cell.setCellValue(cellValue.toString());
                }
            }

        }
    }

    private Object getFieldValue(Object row, String code) {
        String f = StringUtil.toCamelCase(code);
        Field field = ReflectUtil.getField(row.getClass(), f);

        if (ObjectUtil.isNull(field)) {
            return null;
        }

        ExcelTitle excelTitle = ReflectUtil.getField(row.getClass(), f).getDeclaredAnnotation(ExcelTitle.class);

        if (f.endsWith("Str")) {
            f = f.substring(0, f.length() - 3);
        }
        Object value = ReflectUtil.getFieldValue(row, f);
        if (value instanceof Date) {
            if (ObjectUtil.notNull(excelTitle)) {
                String format = excelTitle.dateFormat();
                if (StringUtil.isNotEmpty(format)) {
                    return new SimpleDateFormat(format).format(value);
                }
            }
        }

        return value;

    }


    public static void setHSSFValidation(ExcelWriter writer,
                                         String[] values, int firstRow, int lastRow, int firstCol,
                                         int lastCol) {

        Sheet sheet = writer.getSheet();
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);

        StyleSet styleSet = writer.getStyleSet();
        CellStyle cellStyle = styleSet.getCellStyle();
        cellStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("text"));
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        DataValidation dataValidation = helper.createValidation(constraint, addressList);
        writer.addValidationData(dataValidation);
    }

    private static void checkExcelField(ExcelReader reader, Class beanType) throws ExcelFieldNotFoundException {
        if (ObjectUtil.notNull(beanType) && beanType.equals(Map.class)) {
            return;
        }
        List<Object> datas = reader.readRow(0);
        if (CollectionUtil.isNotEmpty(datas)) {
            String fields = Arrays.stream(beanType.getDeclaredFields())
                    .filter(p -> p.getDeclaredAnnotation(ExcelTitle.class) != null)
                    .map(p -> (p.getDeclaredAnnotation(ExcelTitle.class)).name())
                    .collect(Collectors.joining(",")
                    );
            for (Object field : datas) {
                if (!fields.contains(field.toString())) {
                    throw new ExcelFieldNotFoundException();
                }
            }
        }
    }


}



