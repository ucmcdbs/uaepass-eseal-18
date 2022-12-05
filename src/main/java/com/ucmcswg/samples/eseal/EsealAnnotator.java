package com.ucmcswg.samples.eseal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import com.itextpdf.text.pdf.PdfBorderDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

@Service
public class EsealAnnotator {

    public byte[] annotate(byte[] data) throws DocumentException, IOException {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        PdfReader reader = new PdfReader(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, outputStream, '\0', true);

        int currentPage = reader.getNumberOfPages();

        PdfAppearance canvas = PdfAppearance.createAppearance(stamper.getWriter(), 100, 30);
        Rectangle pageSize = reader.getPageSizeWithRotation(currentPage);
        Rectangle position = new Rectangle(
            pageSize.getRight() - 150,
            pageSize.getTop() - 30,
            pageSize.getRight() - 50,
            pageSize.getTop() - 10,
            0
        );

        PdfAnnotation annotation = PdfAnnotation.createFreeText(stamper.getWriter(), position, "GBM", canvas);
        annotation.put(PdfName.F, new PdfNumber(PdfAnnotation.FLAGS_READONLY));

        PdfBorderDictionary borderDictionary = new PdfBorderDictionary(0, PdfBorderDictionary.STYLE_SOLID);

        annotation.setBorderStyle(borderDictionary);
        stamper.addAnnotation(annotation, currentPage);

        stamper.close();
        reader.close();

        return outputStream.toByteArray();
    }

}
