package com.mxny.ss.mvc.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mxny.ss.component.CustomThreadPoolExecutorCache;
import com.mxny.ss.domain.ExportParam;
import com.mxny.ss.domain.TableHeader;
import com.mxny.ss.exception.AppException;
import com.mxny.ss.metadata.ValueProvider;
import com.mxny.ss.util.BeanConver;
import com.mxny.ss.util.OkHttpUtils;
import com.mxny.ss.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

/**
 * 通用导出工具
 * Created by asiamaster on 2017/6/15 0015.
 */
@Component
@DependsOn("initConfig")
public class ExportUtils {

    public final static Logger log = LoggerFactory.getLogger(ExportUtils.class);

    //每次去后台获取条数
    private final static int FETCH_COUNT = 20000;

    private static final String HEADER_HIDDEN = "hidden";
    private static final String HEADER_PROVIDER = "provider";
    private static final String HEADER_FIELD = "field";
    private static final String HEADER_TYPE = "type";
    private static final String HEADER_FORMAT = "format";
    private static final String HEADER_EXPORT = "export";
    private static final String HEADER_TITLE = "title";

    /**
     * 表单参数
     */
    private final static String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    /**
     * JSON参数，以HTTP Body方式请求
     */
    private final static String CONTENT_TYPE_JSON = "application/json";

    //    多线程执行器
    @Resource
    private CustomThreadPoolExecutorCache customThreadPoolExecutor;

    /**
     * 通用beans导出方法
     *
     * @param response
     * @param title        标题/文件名
     * @param tableHeaders 表头，使用List是为了排序
     * @param beans        数据列表,Bean集合
     * @param providerMeta provider的metadata, key为字段名， value为provider的beanId，如果不需要转义，则为null
     */
    public void exportBeans(HttpServletResponse response, String title, List<TableHeader> tableHeaders, List beans, Map providerMeta) throws Exception {
        List<Map> datas = new ArrayList<>(beans.size());
        for (Object bean : beans) {
            Map map = BeanConver.transformObjectToMap(bean);
            datas.add(map);
        }
        exportMaps(response, title, tableHeaders, datas, providerMeta);
    }

    /**
     * 通用maps导出方法
     *
     * @param response
     * @param title        标题/文件名
     * @param tableHeaders 表头，使用List是为了排序
     * @param datas        数据列表,Map集合
     * @param providerMeta provider的metadata, key为字段名， value为provider的beanId，如果不需要转义，则为null
     */
    public void exportMaps(HttpServletResponse response, String title, List<TableHeader> tableHeaders, List<Map> datas, Map providerMeta) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(FETCH_COUNT);                     // 创建工作簿对象
        Sheet sheet = workbook.createSheet(title);
        //构建表头
        CellStyle columnTopStyle = getHeaderColumnStyle(workbook);  //获取列头样式对象
        Row headerRow = sheet.createRow(0);
        for (int j = 0; j < tableHeaders.size(); j++) {
            //列头信息
            TableHeader tableHeader = tableHeaders.get(j);
            Cell cell = headerRow.createCell(j, CellType.STRING);               //创建列头对应个数的单元格
            RichTextString text = new XSSFRichTextString(tableHeader.getTitle().replaceAll("\\n", "").trim());
            cell.setCellValue(text);                                 //设置列头单元格的值
            cell.setCellStyle(columnTopStyle);                       //设置列头单元格样式
        }
        /**
         * 全局缓存数据列格式， key为format
         */
        Map<String, CellStyle> DATA_COLUMN_STYLE = new HashMap<String, CellStyle>();
        //构建数据
        //用于缓存providerBean
        Map<String, ValueProvider> providerBuffer = new HashMap<>();
        //迭代数据
        for (int i = 0; i < datas.size(); i++) {
            Map rowDataMap = datas.get(i);
            Row dataRow = sheet.createRow(i + 1);
            //迭代列头
            for (int j = 0; j < tableHeaders.size(); j++) {
                TableHeader tableHeader = tableHeaders.get(j);
                Object value = rowDataMap.get(tableHeader.getField());
                String format = tableHeader.getFormat() == null ? getDefaultFormat(value) : tableHeader.getFormat();
                //获取列头样式对象
                CellStyle dataColumnStyle = getDataColumnStyle(workbook, format, DATA_COLUMN_STYLE);
                if (providerMeta != null && providerMeta.containsKey(tableHeader.getField())) {
                    ValueProvider valueProvider = null;
                    //value是provider的beanId
                    String providerBeanId = (String) providerMeta.get(tableHeader.getField());
                    if (providerBuffer.containsKey(providerBeanId)) {
                        valueProvider = providerBuffer.get(providerBeanId);
                    } else {
                        valueProvider = SpringUtil.getBean(providerBeanId, ValueProvider.class);
                        providerBuffer.put(providerBeanId, valueProvider);
                    }
                    setCellValue(dataRow, j, valueProvider.getDisplayText(value, null, null), dataColumnStyle, tableHeader.getTitle());
                } else {
                    setCellValue(dataRow, j, value, dataColumnStyle, tableHeader.getTitle());
                }
            }
        }
        //执行导出
        write(title, workbook, response);
    }

    /**
     * 根据ExportParam导出数据<br></>
     * 用于ExportController的导出datagrid表格
     */
    public void export(HttpServletRequest request, HttpServletResponse response, ExportParam exportParam) {
        try {
            SXSSFWorkbook workbook = new SXSSFWorkbook(FETCH_COUNT);// 创建工作簿对象
            String title = exportParam.getTitle();
            SXSSFSheet sheet = workbook.createSheet(StringUtils.isBlank(title) ? "sheet1" : title);
//            解决高版本poi autoSizeColumn方法异常的情况
            sheet.trackAllColumnsForAutoSizing();
            //构建表头
            buildHeader(exportParam, workbook, sheet);
            //构建数据
            buildData(exportParam, workbook, sheet, request);
            //执行导出
            write(exportParam.getTitle(), workbook, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建数据列
     *
     * @param exportParam
     * @param workbook
     * @param sheet
     * @param request
     */
    private void buildData(ExportParam exportParam, SXSSFWorkbook workbook, Sheet sheet, HttpServletRequest request) {
        String url = exportParam.getUrl();
        if (!url.startsWith("http")) {
            url = url.startsWith("/") ? url : "/" + url;
            String basePath = SpringUtil.getProperty("project.serverPath", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
            url = basePath + url;
        }
        //这里获取到的是nginx转换后的(IP)地址和端口号，如果是跳板机这种，有可能会禁止访问，后续改为从配置读取
//        String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();

        //先获取总数
        int total = 0;
        try {
            total = getCount(url, exportParam.getContentType(), exportParam.getQueryParams(), request);
        } catch (Exception e) {
            log.error(String.format("构建导出数据异常, url:'%s', 参数:(%s)", url, JSON.toJSONString(exportParam.getQueryParams())), e);
            throw e;
        }
        //查询次数
        int queryCount = total % FETCH_COUNT == 0 ? total / FETCH_COUNT : total / FETCH_COUNT + 1;
        //如果只查一次，就不启线程了，直接在主线程查
        if (queryCount == 1) {
            JSONArray rowDatas = new ExportDataThread(0, exportParam.getQueryParams(), url, exportParam.getContentType(), request).queryThreadData();
            buildSingleData(workbook, 0, exportParam.getColumns(), rowDatas, sheet);
        } else {
            //分别进行取数
            List<Future<JSONArray>> futures = new ArrayList<>(queryCount);
            for (int current = 0; current < queryCount; current++) {
                Map<String, String> queryParams = new HashMap<>();
                //注意这里直接使用exportParam.getQueryParams()会产生多线程并发缺陷，所有深putAll进行半深拷贝
                //然而putAll也不完全是深拷贝，但它的性能优于字节拷贝，它只能深拷贝基本类型，不过这里也只有基本类型
                queryParams.putAll(exportParam.getQueryParams());
                CompletionService<JSONArray> completionService = new ExecutorCompletionService<JSONArray>(customThreadPoolExecutor.getExecutor());
                Future<JSONArray> future = completionService.submit(new ExportDataThread(current, queryParams, url, exportParam.getContentType(), request));
//                Future<JSONArray> future = executor.submit(new ExportDataThread(current, queryParams, url, exportParam.getContentType(), request));
                futures.add(future);
            }
            int current = 0;
            try {
                for (Future<JSONArray> future : futures) {
                    JSONArray rowDatas = future.get();
                    if(rowDatas == null){
                        continue;
                    }
                    buildSingleData(workbook, current++, exportParam.getColumns(), rowDatas, sheet);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据数据类型获取默认的格式
     * @param value
     * @return
     */
    private String getDefaultFormat(Object value){
        if (value instanceof Integer || value instanceof Short || value instanceof Long) {
            return "0";
        } else if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            return "0.00";
        } else if (value instanceof Date){
            return "m/d/yy h:mm";
        } else{
            return "@";
        }
    }

    /**
     * 构建单次数据
     * @param current
     * @param columns
     * @param rowDatas
     * @param sheet
     */
    private void buildSingleData(SXSSFWorkbook workbook, int current, List<List<Map<String, Object>>> columns, JSONArray rowDatas, Sheet sheet) {
        Integer headerRowCount = columns.size();
        //直接取最后一行的列头信息
        List<Map<String, Object>> headers = columns.get(columns.size() - 1);
        int headerSize = headers.size();
        int rowDataSize = rowDatas.size();
        /**
         * 全局缓存数据列格式， key为format
         */
        Map<String, CellStyle> DATA_COLUMN_STYLE = new HashMap<String, CellStyle>();
        //迭代数据
        for (int i = 0; i < rowDataSize; i++) {
            JSONObject rowDataMap = (JSONObject) rowDatas.get(i);
            Row row = sheet.createRow(current * FETCH_COUNT + i + headerRowCount);
            int cellIndex = 0;
            //迭代列头
            for (int j = 0; j < headerSize; j++) {
                Map<String, Object> headerMap = headers.get(j);
                Boolean export = (Boolean)headerMap.get(HEADER_EXPORT);
                if(export != null && !export){
                    continue;
                }
                //隐藏的列不导出
                if (headerMap.get(HEADER_HIDDEN) != null && headerMap.get(HEADER_HIDDEN).equals(true) && (export == null || !export)) {
                    continue;
                }
                String field = (String) headerMap.get(HEADER_FIELD);
                Object value = null;
                int fieldIndex = field.indexOf(".");
                //如果没有提供者，则由导出处理obj.key这种field
                if (StringUtils.isBlank((String) headerMap.get(HEADER_PROVIDER)) && fieldIndex >= 0) {
                    String field1 = field.substring(0, fieldIndex);
                    String field2 = field.substring(fieldIndex + 1);
                    JSONObject obj = rowDataMap.getJSONObject(field1);
                    if (obj != null) {
                        value = obj.get(field2);
                    }
                } else {
                    value = rowDataMap.get(field);
                }
                //强制单元格类型，目前只支持number和string
                String type = (String) headerMap.get(HEADER_TYPE);
                //number类型的格式，参见org.apache.poi.ss.usermodel.BuiltinFormats,默认为0
                String format = headerMap.getOrDefault(HEADER_FORMAT, getDefaultFormat(value)).toString();
                //判断是否有值提供者需要转义(此功能已经在datagrid的查询中封装，这里不需要处理了)
//                if(headerMap.containsKey("HEADER_PROVIDER")){
//                    value = valueProviderUtils.setDisplayText(headerMap.get("HEADER_PROVIDER").toString(), value, null);
//                }
                CellStyle dataColumnStyle = getDataColumnStyle(workbook, format, DATA_COLUMN_STYLE);
                setCellValue(row, cellIndex, value, dataColumnStyle, type);
                cellIndex++;
            }
        }
    }

    /**
     * 设置单元格的值，主要是处理单元格类型
     * @param row
     * @param cellIndex
     * @param value
     * @param dataColumnStyle
     * @param type
     */
    private void setCellValue(Row row, int cellIndex,  Object value, CellStyle dataColumnStyle, String type) {
        CellType cellType = value instanceof Number ? CellType.NUMERIC : CellType.STRING;
        if(StringUtils.isNotBlank(type) && type.equals("number")){
            cellType = CellType.NUMERIC;
        }
        Cell cell = row.createCell(cellIndex, cellType);
        cell.setCellStyle(dataColumnStyle);
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        //优先使用type
        if(StringUtils.isNotBlank(type)){
            if(type.equals("number")){
                cell.setCellValue(new Double(value.toString()));
            }else if(type.equals("string")){
                cell.setCellValue(new XSSFRichTextString(value.toString()));
            }else{
                cell.setCellValue(value.toString());
            }
            return;
        }
        //没有type则根据值类型确定格式
        if (value instanceof Integer) {
            cell.setCellValue(((Integer) value).doubleValue());
        } else if (value instanceof Long) {
            cell.setCellValue(((Long) value).doubleValue());
        } else if (value instanceof Short) {
            cell.setCellValue(((Short) value).doubleValue());
        } else if (value instanceof Float) {
            cell.setCellValue(((Float) value).doubleValue());
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else {
//            RichTextString text = new XSSFRichTextString(value.toString());
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 导出一部分数据
     */
    private class ExportDataThread implements Callable {
        int current;
        Map<String, String> queryParams;
        String exportUrl;
        String contentType;
        HttpServletRequest request;

        ExportDataThread(int current, Map<String, String> queryParams, String exportUrl, String contentType, HttpServletRequest request) {
            this.current = current;
            this.queryParams = queryParams;
            this.exportUrl = exportUrl;
            this.request = request;
            this.contentType = contentType;
        }

        @Override
        public JSONArray call() {
            return queryThreadData();
        }

        //构建每个线程导出的数据
        private JSONArray queryThreadData() {
            queryParams.put("page", String.valueOf(current + 1));
            queryParams.put("rows", String.valueOf(FETCH_COUNT));
            String json = syncExecute(exportUrl, contentType, queryParams, "POST", request);
            //简单判断是JSONArray的话，就是不分页的list查询，总数直接取JSONArray
            if (json.trim().startsWith("[") && json.trim().endsWith("]")) {
                return JSON.parseArray(json);
            } else {
                return (JSONArray) JSON.parseObject(json).get("rows");
            }
        }
    }

    /**
     * 获取当前需要导出的总数
     *
     * @param url
     * @param queryParams
     * @param contentType
     * @param request     为了数据权限
     * @return
     */
    private int getCount(String url, String contentType, Map<String, String> queryParams, HttpServletRequest request) {
        queryParams.put("page", "1");
        queryParams.put("rows", "1");
        String json = syncExecute(url, contentType, queryParams, "POST", request);
        if(json == null){
            String exMsg = String.format("远程访问异常, url:%s, contentType:%s, queryParams:%s", url, contentType, queryParams);
            log.error(exMsg);
            throw new AppException(exMsg);
        }
        //简单判断是JSONArray的话，就是不分页的list查询，总数直接取JSONArray的长度
        if (json.trim().startsWith("[") && json.trim().endsWith("]")) {
            return JSON.parseArray(json).size();
        } else {
            try {
                return (int) JSON.parseObject(json).get("total");
            } catch (Exception e) {
                log.error("getCount远程访问失败，结果:" + json);
                throw e;
            }
        }
    }

    /**
     * 构建表头列, 只支持colspan，不支持rowspan，因为rowspan无法确定是向上还是向下合并,判断的因素太多，暂不支持
     *
     * @param exportParam
     * @param workbook
     * @param sheet
     */
    private void buildHeader(ExportParam exportParam, SXSSFWorkbook workbook, Sheet sheet) {
        CellStyle columnTopStyle = getHeaderColumnStyle(workbook);//获取列头样式对象
        //渲染复合表头列
        for (int i = 0; i < exportParam.getColumns().size(); i++) {
            //每行的列信息
            List<Map<String, Object>> rowColumns = exportParam.getColumns().get(i);
            Row row = sheet.createRow(i);
            int colspanAdd = 0;
            int index = 0;
            //迭代生成每一列，如果有hidden或者title为null的，则跳过
            Iterator<Map<String, Object>> it = rowColumns.iterator();
            //列号
            int columnIndex = 0;
            while (it.hasNext()) {

//            for (int j = 0; j < rowColumns.size(); j++) {
                //列头信息
                Map<String, Object> columnMap = it.next();
                Boolean export = (Boolean)columnMap.get(HEADER_EXPORT);
                if(export != null && !export){
                    continue;
                }
                //隐藏的列不导出
                if (columnMap.get(HEADER_HIDDEN) != null && columnMap.get(HEADER_HIDDEN).equals(true) && (export == null || !export)) {
                    it.remove();
                    continue;
                }
                if (columnMap.get(HEADER_TITLE) == null) {
                    it.remove();
                    continue;
                }
                String headerTitle = columnMap.get(HEADER_TITLE).toString().replaceAll("\\n", "").trim();
                //最后一行的列头，适应宽度
                if (i == exportParam.getColumns().size() - 1) {
                    sheet.setColumnWidth(columnIndex, headerTitle.getBytes().length * 2 * 256);
//                    sheet.autoSizeColumn(j, true);
                }
                Cell cell = row.createCell(index + colspanAdd, CellType.STRING);               //创建列头对应个数的单元格
                RichTextString text = new XSSFRichTextString(headerTitle);
                cell.setCellValue(text);                                 //设置列头单元格的值
                cell.setCellStyle(columnTopStyle);                       //设置列头单元格样式
                if (columnMap.get("colspan") != null) {
                    Integer colspan = Integer.class.isAssignableFrom(columnMap.get("colspan").getClass()) ? (Integer) columnMap.get("colspan") : Integer.parseInt(columnMap.get("colspan").toString());
                    if (colspan > 1) {
                        Cell tempCell = row.createCell(index + colspanAdd + colspan - 1, CellType.STRING);               //创建合并最后一列的列头，保证最后一列有右边框
                        tempCell.setCellStyle(columnTopStyle);
                        sheet.addMergedRegion(new CellRangeAddress(i, i, index + colspanAdd, index + colspanAdd + colspan - 1));
                        colspanAdd = colspanAdd + colspan - 1;
                    }
                }
                columnIndex++;
                index++;
            }
        }
    }

    /**
     * 同步调用远程方法
     *
     * @param url
     * @param queryParams
     * @param httpMethod
     * @param contentType
     * @param request     为了数据权限
     * @return
     */
    private String syncExecute(String url, String contentType, Map<String, String> queryParams, String httpMethod, HttpServletRequest request) {
        try {
            Map<String, String> headersMap = new HashMap<>();
//            headersMap.put("Content-Type", "application/json;charset=utf-8");
//	            传入权限的SessionConstants.SESSION_ID(值为SessionId，需要注意和权限系统同步，毕竟框架不依赖权限系统)
            if (request != null) {
                Enumeration<String> enumeration = request.getHeaderNames();
                while (enumeration.hasMoreElements()) {
                    String key = enumeration.nextElement();
                    if ("Accept-Encoding".equalsIgnoreCase(key.trim())) {
                        continue;
                        //解决nginx不用完整路径，用header中的Host的问题，可能导致导出器使用错误的host路径，这时取消Host Header
                    } else if ("Host".equalsIgnoreCase(key.trim())) {
                        continue;
                    }
                    headersMap.put(key, request.getHeader(key));
                }
            }
            if ("POST".equalsIgnoreCase(httpMethod)) {
                JSONObject paramJo = (JSONObject) JSONObject.toJSON(queryParams);
                if (StringUtils.isBlank(contentType) || contentType.indexOf(CONTENT_TYPE_FORM) >= 0) {
                    //构建查询参数，主要是为了处理metadata信息以及其它类型的值转为String
                    Map<String, String> param = buildMetadataParams(paramJo);
//                  MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8")
                    return OkHttpUtils.postFormParameters(url, param, headersMap, null);
                } else if (contentType.indexOf(CONTENT_TYPE_JSON) >= 0) {
                    return OkHttpUtils.postBodyString(url, paramJo.toJSONString(), headersMap, null);
                } else {
                    log.error(String.format("不支持的contentType[%s]", contentType));
                    return null;
                }
            } else { //GET方式, 不支持metadata转换
                return OkHttpUtils.get(url, queryParams, null, null);
            }
        } catch (Exception e) {
            log.error(String.format("远程调用[" + url + "]发生异常,message:[%s]", e.getMessage()), e);
            return null;
        }
    }

    /**
     * 构建查询参数，主要是为了处理metadata信息以及其它类型的值转为String
     * @param paramJo
     * @return
     */
    private Map<String, String> buildMetadataParams(JSONObject paramJo){
        Map<String, String> param = new HashMap<>();
        if (paramJo != null && !paramJo.isEmpty()) {
            for (Map.Entry<String, Object> entry : paramJo.entrySet()) {
                if (entry.getValue() instanceof JSONObject) {
                    JSONObject valueJo = (JSONObject) entry.getValue();
                    //解决spring mvc参数注入@ModelAttribute Domain domain时，metadata作为Map类型的注入问题
                    for (Map.Entry<String, Object> tmpEntry : valueJo.entrySet()) {
                        if (tmpEntry.getValue() == null) {
                            continue;
                        }
                        param.put(entry.getKey() + "[" + tmpEntry.getKey() + "]", tmpEntry.getValue().toString());
                    }
                } else {
                    //避免为空(null)的值(value)在okhttp调用时报错，这里就不传入了
                    if (entry.getValue() == null) {
                        continue;
                    }
                    param.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }
        return param;
    }

    /**
     * 执行导出
     *
     * @param title
     * @param workbook
     * @param response
     */
    private void write(String title, SXSSFWorkbook workbook, HttpServletResponse response) {
        if (workbook != null) {
            try {
                String fileNameDownload = title + ".xlsx";

                response.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(fileNameDownload, "utf-8") + "\"");
                //this.getResponse().setContentType("application/x-xls;charset=UTF-8");
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                workbook.write(response.getOutputStream());
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 列头单元格样式
     * @param workbook
     * @return
     */
    private CellStyle getHeaderColumnStyle(SXSSFWorkbook workbook) {
        // 设置字体
        Font font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short) 11);
        //字体加粗
        font.setBold(true);
        //字体颜色
        font.setColor(IndexedColors.ROYAL_BLUE.index);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        CellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(IndexedColors.BLACK.index);
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(IndexedColors.BLACK.index);
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(IndexedColors.BLACK.index);
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(IndexedColors.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置前景色样式
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //设置前景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 列数据信息单元格样式
     * @param workbook
     * @param format
     * @return
     */
    private CellStyle getDataColumnStyle(SXSSFWorkbook workbook, String format, Map<String, CellStyle> DATA_COLUMN_STYLE) {
        if(DATA_COLUMN_STYLE.containsKey(format)){
            return DATA_COLUMN_STYLE.get(format);
        }
        // 设置字体
        Font font = workbook.createFont();
        //设置字体大小
        font.setFontHeightInPoints((short) 10);
        //字体加粗
        //font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        //设置字体名字
        font.setFontName("Courier New");
        //设置样式;
        CellStyle style = workbook.createCellStyle();
        //设置底边框;
        style.setBorderBottom(BorderStyle.THIN);
        //设置底边框颜色;
        style.setBottomBorderColor(IndexedColors.BLACK.index);
        //设置左边框;
        style.setBorderLeft(BorderStyle.THIN);
        //设置左边框颜色;
        style.setLeftBorderColor(IndexedColors.BLACK.index);
        //设置右边框;
        style.setBorderRight(BorderStyle.THIN);
        //设置右边框颜色;
        style.setRightBorderColor(IndexedColors.BLACK.index);
        //设置顶边框;
        style.setBorderTop(BorderStyle.THIN);
        //设置顶边框颜色;
        style.setTopBorderColor(IndexedColors.BLACK.index);
        //在样式用应用设置的字体;
        style.setFont(font);
        //设置自动换行;
        style.setWrapText(false);
        //设置水平对齐的样式为居中对齐;
        style.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直对齐的样式为居中对齐;
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(HSSFDataFormat.getBuiltinFormat(format));
        DATA_COLUMN_STYLE.put(format, style);
        return style;
    }

}
