package com.yondu.service;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by aomine on 3/13/17.
 */
public class ReportService {

    public void exportXls()
            throws JRException, IOException {

        String fileDest = RUSH_HOME + "//" + "transactions.xls";
        File file = new File(fileDest);
        OutputStream outputStream = new FileOutputStream(file);

        String jasperPath         = getClass().getResource("/app/jrxml/test.jasper").getPath();
        Map map = new HashMap();
        JasperPrint jasperPrint   = JasperFillManager.fillReport(jasperPath, map, new JREmptyDataSource());
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.create.custom.palette", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.one.page.per.sheet", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.remove.empty.space.between.rows", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.remove.empty.space.between.columns", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.white.page.background", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.detect.cell.type", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.size.fix.enabled", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.ignore.graphics", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.collapse.row.span", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.ignore.cell.border", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.ignore.cell.background", "false");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.max.rows.per.sheet", "0");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.wrap.text", "true");
        jasperPrint.setProperty("net.sf.jasperreports.export.xls.use.timezone", "false");

        JRXlsExporter exporter = new JRXlsExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
        configuration.setOnePagePerSheet(true);
        configuration.setDetectCellType(true);
        configuration.setCollapseRowSpan(false);
        exporter.setConfiguration(configuration);
        exporter.exportReport();
        outputStream.close();
    }

}
