package com.example.fuelticket.service;

import com.example.fuelticket.entity.Ticket;
import com.example.fuelticket.entity.Station;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
public class PdfGenerationService {

    @Value("${app.pdf.directory:./tickets}")
    private String pdfDirectory;

    public String generateTicketPdf(Ticket ticket) throws IOException, WriterException {
        // Normaliser le chemin du répertoire (convertir en chemin absolu si relatif)
        Path baseDir = Paths.get(pdfDirectory);
        if (!baseDir.isAbsolute()) {
            // Si le chemin est relatif, le rendre absolu par rapport au répertoire de travail
            baseDir = Paths.get(System.getProperty("user.dir"), pdfDirectory).normalize();
        }
        
        // Créer le répertoire s'il n'existe pas
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
            log.info("Répertoire PDF créé: {}", baseDir.toAbsolutePath());
        }

        // Générer le nom du fichier PDF
        String fileName = "ticket_" + ticket.getNumeroTicket() + ".pdf";
        Path filePath = baseDir.resolve(fileName).normalize();
        
        log.info("Génération du PDF: {}", filePath.toAbsolutePath());

        // Couleurs modernes
        DeviceRgb primaryColor = new DeviceRgb(102, 126, 234); // Violet/bleu
        DeviceRgb secondaryColor = new DeviceRgb(118, 75, 162); // Violet foncé
        DeviceRgb accentColor = new DeviceRgb(76, 81, 191); // Bleu foncé
        DeviceRgb lightGray = new DeviceRgb(248, 249, 250);
        DeviceRgb darkGray = new DeviceRgb(73, 80, 87);

        // Créer le PDF
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // En-tête avec le nom de l'application
            Div headerDiv = new Div();
            headerDiv.setBackgroundColor(primaryColor);
            headerDiv.setPadding(20);
            headerDiv.setMarginBottom(20);
            
            Paragraph appName = new Paragraph("Taadjii Ko Lafia")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(ColorConstants.WHITE)
                    .setMargin(0);
            headerDiv.add(appName);
            
            Paragraph appSubtitle = new Paragraph("Système de Gestion de Tickets de Carburant")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setFontColor(new DeviceRgb(255, 255, 255))
                    .setMarginTop(5)
                    .setMarginBottom(0);
            headerDiv.add(appSubtitle);
            
            document.add(headerDiv);

            // Titre du ticket
            Paragraph title = new Paragraph("TICKET DE CARBURANT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(accentColor)
                    .setMarginBottom(15);
            document.add(title);

            // Informations de la station dans une carte stylée
            Station station = ticket.getStation();
            Div stationCard = new Div();
            stationCard.setBackgroundColor(lightGray);
            stationCard.setBorder(new SolidBorder(primaryColor, 2));
            stationCard.setPadding(15);
            stationCard.setMarginBottom(20);
            
            Paragraph stationLabel = new Paragraph("STATION")
                    .setFontSize(11)
                    .setFontColor(darkGray)
                    .setMarginBottom(5)
                    .setMarginTop(0);
            stationCard.add(stationLabel);
            
            Paragraph stationInfo = new Paragraph(station.getNom())
                    .setFontSize(16)
                    .setBold()
                    .setFontColor(accentColor)
                    .setMarginBottom(5)
                    .setMarginTop(0);
            stationCard.add(stationInfo);
            
            if (station.getLocalisation() != null && !station.getLocalisation().isEmpty()) {
                Paragraph location = new Paragraph("📍 " + station.getLocalisation())
                        .setFontSize(11)
                        .setFontColor(darkGray)
                        .setMarginTop(0)
                        .setMarginBottom(0);
                stationCard.add(location);
            }
            
            document.add(stationCard);

            // Tableau des détails du ticket stylé
            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);
            
            // En-tête du tableau
            Cell headerCell1 = new Cell().add(new Paragraph("INFORMATION")
                    .setBold()
                    .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(primaryColor)
                    .setPadding(10);
            Cell headerCell2 = new Cell().add(new Paragraph("VALEUR")
                    .setBold()
                    .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(primaryColor)
                    .setPadding(10);
            table.addHeaderCell(headerCell1);
            table.addHeaderCell(headerCell2);
            
            // Lignes du tableau avec style alterné
            addStyledRow(table, "Numéro de Ticket", ticket.getNumeroTicket(), lightGray, false);
            addStyledRow(table, "Numéro d'Ordre", ticket.getNumeroOrdre(), new DeviceRgb(255, 255, 255), false);
            addStyledRow(table, "Date d'Approvisionnement", 
                    ticket.getDateApprovisionnement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
                    lightGray, false);
            addStyledRow(table, "Type de Carburant", ticket.getTypeCarburant(), new DeviceRgb(255, 255, 255), false);
            addStyledRow(table, "Quantité", ticket.getQuantite() + " Litres", lightGray, false);
            
            // Montant avec style spécial
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.FRANCE);
            String montantFormate = formatter.format(ticket.getMontantPaye() != null ? ticket.getMontantPaye() : 0);
            addStyledRow(table, "Montant Payé", montantFormate + " FCFA", new DeviceRgb(255, 255, 255), true);
            
            addStyledRow(table, "Statut", ticket.getStatut().toString(), lightGray, false);

            document.add(table);

            // Informations du citoyen dans une carte stylée
            Div citizenCard = new Div();
            citizenCard.setBackgroundColor(lightGray);
            citizenCard.setBorder(new SolidBorder(secondaryColor, 2));
            citizenCard.setPadding(15);
            citizenCard.setMarginBottom(20);
            
            Paragraph citizenTitle = new Paragraph("INFORMATIONS DU CITOYEN")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(secondaryColor)
                    .setMarginBottom(10)
                    .setMarginTop(0);
            citizenCard.add(citizenTitle);

            Table citizenTable = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                    .useAllAvailableWidth();
            
            addCitizenRow(citizenTable, "Prénom", ticket.getPrenomCitoyen());
            addCitizenRow(citizenTable, "Nom", ticket.getNomCitoyen());
            addCitizenRow(citizenTable, "Email", ticket.getEmailCitoyen());
            if (ticket.getTelephoneCitoyen() != null && !ticket.getTelephoneCitoyen().isEmpty()) {
                addCitizenRow(citizenTable, "Téléphone", ticket.getTelephoneCitoyen());
            }

            citizenCard.add(citizenTable);
            document.add(citizenCard);

            // QR Code centré dans une carte
            if (ticket.getQrCodeData() != null) {
                try {
                    Div qrCard = new Div();
                    qrCard.setBackgroundColor(ColorConstants.WHITE);
                    qrCard.setBorder(new SolidBorder(primaryColor, 2));
                    qrCard.setPadding(20);
                    qrCard.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    qrCard.setMarginBottom(20);
                    
                    Paragraph qrTitle = new Paragraph("CODE QR")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(12)
                            .setBold()
                            .setFontColor(primaryColor)
                            .setMarginBottom(10)
                            .setMarginTop(0);
                    qrCard.add(qrTitle);
                    
                    BufferedImage qrCodeImage = generateQRCodeImage(ticket.getQrCodeData(), 250, 250);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(qrCodeImage, "PNG", baos);
                    byte[] imageBytes = baos.toByteArray();
                    
                    Image qrCode = new Image(com.itextpdf.io.image.ImageDataFactory.create(imageBytes));
                    qrCode.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    qrCode.setWidth(200);
                    qrCode.setHeight(200);
                    qrCard.add(qrCode);
                    
                    document.add(qrCard);
                } catch (Exception e) {
                    log.error("Erreur lors de la génération du QR code: {}", e.getMessage());
                }
            }

            // Message de validation stylé
            Div footerDiv = new Div();
            footerDiv.setBackgroundColor(lightGray);
            footerDiv.setBorder(new SolidBorder(primaryColor, 1));
            footerDiv.setPadding(15);
            footerDiv.setMarginTop(20);
            
            Paragraph validationMessage = new Paragraph("✓ Ce ticket est valide pour l'approvisionnement en carburant à la date indiquée.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(darkGray)
                    .setItalic()
                    .setMargin(0);
            footerDiv.add(validationMessage);
            
            document.add(footerDiv);

        }

        // Retourner le chemin normalisé (utiliser des slashes pour compatibilité)
        String normalizedPath = filePath.toAbsolutePath().toString().replace("\\", "/");
        log.info("PDF généré avec succès: {}", normalizedPath);
        return normalizedPath;
    }

    private void addStyledRow(Table table, String label, String value, DeviceRgb backgroundColor, boolean highlight) {
        DeviceRgb darkGray = new DeviceRgb(73, 80, 87);
        DeviceRgb greenColor = new DeviceRgb(76, 175, 80);
        
        Paragraph labelPara = new Paragraph(label)
                .setFontSize(11)
                .setFontColor(darkGray);
        
        Paragraph valuePara = new Paragraph(value)
                .setFontSize(11)
                .setFontColor(highlight ? greenColor : darkGray);
        if (highlight) {
            valuePara.setBold();
        }
        
        Cell labelCell = new Cell().add(labelPara)
                .setBackgroundColor(backgroundColor)
                .setPadding(10)
                .setBorder(new SolidBorder(ColorConstants.WHITE, 0.5f));
        
        Cell valueCell = new Cell().add(valuePara)
                .setBackgroundColor(backgroundColor)
                .setPadding(10)
                .setBorder(new SolidBorder(ColorConstants.WHITE, 0.5f));
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addCitizenRow(Table table, String label, String value) {
        DeviceRgb darkGray = new DeviceRgb(73, 80, 87);
        DeviceRgb lightGray = new DeviceRgb(248, 249, 250);
        DeviceRgb accentColor = new DeviceRgb(76, 81, 191);
        
        Cell labelCell = new Cell().add(new Paragraph(label + ":")
                .setFontSize(10)
                .setFontColor(darkGray))
                .setBackgroundColor(ColorConstants.WHITE)
                .setPadding(8)
                .setBorder(new SolidBorder(lightGray, 0.5f));
        
        Paragraph valuePara = new Paragraph(value != null ? value : "N/A")
                .setFontSize(10)
                .setBold()
                .setFontColor(accentColor);
        
        Cell valueCell = new Cell().add(valuePara)
                .setBackgroundColor(ColorConstants.WHITE)
                .setPadding(8)
                .setBorder(new SolidBorder(lightGray, 0.5f));
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    public String generateQRCodeData(Ticket ticket) {
        // Générer les données du QR code contenant les informations essentielles du ticket
        StringBuilder qrData = new StringBuilder();
        qrData.append("TICKET:").append(ticket.getNumeroTicket()).append(";");
        qrData.append("STATION:").append(ticket.getStation().getNom()).append(";");
        qrData.append("DATE:").append(ticket.getDateApprovisionnement().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append(";");
        qrData.append("TYPE:").append(ticket.getTypeCarburant()).append(";");
        qrData.append("QTE:").append(ticket.getQuantite()).append(";");
        qrData.append("MONTANT:").append(ticket.getMontantPaye()).append(";");
        qrData.append("STATUT:").append(ticket.getStatut().toString());
        
        return qrData.toString();
    }
}
