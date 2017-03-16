package com.yondu.service;

import com.yondu.App;
import com.yondu.model.Customer;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aomine on 3/13/17.
 */
public class ReportService {


    public void exportXls() {

        try {
            File file = new File("/home/aomine/test.xls");
            InputStream is = this.getClass().getResourceAsStream("/app/jrxml/test.jrxml");

            JasperReport jasperPath = JasperCompileManager.compileReport(is);


            List<Customer> customers = new ArrayList<>();
            Customer c1 = new Customer();
            c1.setName("erin");
            Customer c2 = new Customer();
            c2.setName("c2");
            customers.add(c1);
            customers.add(c2);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, new HashMap<>(), new JRBeanCollectionDataSource(customers));
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
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            configuration.setOnePagePerSheet(true);
            configuration.setDetectCellType(true);
            configuration.setCollapseRowSpan(false);
            exporter.setConfiguration(configuration);
            exporter.exportReport();

        } catch (JRException e) {
            e.printStackTrace();
        }

    }


}
