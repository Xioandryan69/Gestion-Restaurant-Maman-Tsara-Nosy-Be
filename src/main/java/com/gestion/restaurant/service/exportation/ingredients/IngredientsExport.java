package com.gestion.restaurant.service.exportation.ingredients;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import jakarta.servlet.http.HttpServletResponse;
public class IngredientsExport {
    


@Service
public class ExportService {

    // @Autowired
    // private RaceRizeRepository raceRizRepository;

    // @Autowired
    // private HistoriquePrixRizRepository historiquePrixRizRepository;

    // @Autowired
    // private ClientRepository clientRepository;

    // @Autowired
    // private MouvementStockService mouvementStockService;

    // @Autowired
    // private EtatRizRepository etatRizRepository;

    /**
     * Export Excel de l'etat du stock (toutes varietes / tous etats de riz) a une date donnee.
     */
    // /
    //     Cell labelCell = new Cell()
    //             .add(new Paragraph(label))
    //             .setTextAlignment(TextAlignment.LEFT)
    //             .setBorder(null);
    //     table.addCell(labelCell);

    //     Cell valueCell = new Cell()
    //             .add(new Paragraph(value))
    //             .setTextAlignment(TextAlignment.RIGHT)
    //             .setBorder(null);
    //     table.addCell(valueCell);
    // }
}
}
