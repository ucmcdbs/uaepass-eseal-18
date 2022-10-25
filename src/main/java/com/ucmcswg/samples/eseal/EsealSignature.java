package com.ucmcswg.samples.eseal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Base64;

import javax.xml.bind.JAXBElement;

import org.springframework.core.io.ClassPathResource;

import com.safelayer.tws.AppearanceType;
import com.safelayer.tws.BackgroundType;
import com.safelayer.tws.ForegroundType;
import com.safelayer.tws.ImageType;
import com.safelayer.tws.ImageType.ImageSize;
import com.safelayer.tws.ObjectFactory;
import com.safelayer.tws.PdfAttributesType;
import com.safelayer.tws.PdfAttributesType.Params;
import com.safelayer.tws.PdfSignatureInfoType;
import com.safelayer.tws.PositionType;
import com.safelayer.tws.RectType;
import com.safelayer.tws.SignatureInfosType;
import com.safelayer.tws.SignatureInfosType.SignatureInfo;
import com.safelayer.tws.TextType;

public class EsealSignature {

    private final EsealClientProperties esealClientProperties;

    private final ObjectFactory objectFactory;

    private String base64Image = new String("");

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;

        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);

                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
    }

    public EsealSignature(EsealClientProperties esealClientProperties) {
        this.objectFactory = new ObjectFactory();
        this.esealClientProperties = esealClientProperties;
    }

    public JAXBElement<PdfSignatureInfoType> getPdfSignature() {
        Params params = new Params();
        params.setReason("Author");

        PdfAttributesType pdfAttributes = new PdfAttributesType();
        pdfAttributes.setValidationMethod(esealClientProperties.getSignature().get("validation-method"));
        pdfAttributes.setSignaturePosition(esealClientProperties.getSignature().get("position"));
        pdfAttributes.setParams(params);

        RectType rect = new RectType();
        rect.setX0(new BigInteger("50"));
        rect.setX1(new BigInteger("340"));
        rect.setY0(new BigInteger("50"));
        rect.setY1(new BigInteger("450"));

        TextType.Properties textProperties = new TextType.Properties();
        textProperties.setColor("0 0 0");
        textProperties.setFontSize(new BigInteger("10"));
        PositionType position = new PositionType();
        position.setX(new BigInteger("12"));
        position.setY(new BigInteger("12"));

        SignatureInfo signatureInfoSubject = new SignatureInfo();
        signatureInfoSubject.setId("Subject");
        signatureInfoSubject.setTitle(" ");

        SignatureInfo signatureInfoDate = new SignatureInfo();
        signatureInfoDate.setId("Date");
        signatureInfoDate.setProperties("timezone.local");
        signatureInfoDate.setTitle(" ");

        SignatureInfosType signatureInfos = new SignatureInfosType();
        signatureInfos.getSignatureInfo().add(signatureInfoSubject);
        signatureInfos.getSignatureInfo().add(signatureInfoDate);

        TextType text = new TextType();
        text.setProperties(textProperties);
        text.setPosition(position);
        text.setSignatureInfos(signatureInfos);

        try {
            InputStream is = new ClassPathResource("/assets/background.jpg").getInputStream();
            byte[] backgroundBytes = readAllBytes(is);
            base64Image = Base64.getEncoder().encodeToString(backgroundBytes);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        ImageSize backgroundImageSize = new ImageSize();
        backgroundImageSize.setHeight(new BigInteger("100"));
        backgroundImageSize.setWidth(new BigInteger("200"));

        PositionType backgroundPosition = new PositionType();
        backgroundPosition.setX(new BigInteger("55"));
        backgroundPosition.setY(new BigInteger("300"));

        ImageType backgroundImage = new ImageType();
        backgroundImage.setData(base64Image);
        backgroundImage.setEncodeType("base64");
        backgroundImage.setImageSize(backgroundImageSize);
        backgroundImage.setPosition(backgroundPosition);

        BackgroundType background = new BackgroundType();
        background.setImage(backgroundImage);

        try {
            InputStream is = new ClassPathResource("/assets/foreground.jpg").getInputStream();
            byte[] foregroundBytes = readAllBytes(is);
            base64Image = Base64.getEncoder().encodeToString(foregroundBytes);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        ImageSize foregroundImageSize = new ImageSize();
        foregroundImageSize.setHeight(new BigInteger("1"));
        foregroundImageSize.setWidth(new BigInteger("1"));

        PositionType foregroundPosition = new PositionType();
        foregroundPosition.setX(new BigInteger("12"));
        foregroundPosition.setY(new BigInteger("12"));

        ImageType foregroundImage = new ImageType();
        foregroundImage.setData(base64Image);
        foregroundImage.setEncodeType("base64");
        foregroundImage.setImageSize(foregroundImageSize);
        foregroundImage.setPosition(foregroundPosition);

        ForegroundType foreground = new ForegroundType();
        foreground.setImage(foregroundImage);
        foreground.setText(text);

        AppearanceType appearance = new AppearanceType();
        appearance.setRect(rect);
        appearance.setBackground(background);
        appearance.setForeground(foreground);

        PdfSignatureInfoType pdfSignatureInfoType = new PdfSignatureInfoType();
        pdfSignatureInfoType.setPdfAttributes(pdfAttributes);
        pdfSignatureInfoType.setAppearance(appearance);

        return this.objectFactory.createPdfSignatureInfo(pdfSignatureInfoType);
    }
}
