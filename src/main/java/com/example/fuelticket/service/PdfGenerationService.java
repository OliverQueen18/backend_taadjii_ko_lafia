package com.example.fuelticket.service;

import com.example.fuelticket.entity.Ticket;
import com.example.fuelticket.entity.Station;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@Slf4j
public class PdfGenerationService {

    @Value("${app.pdf.directory:./tickets}")
    private String pdfDirectory;

    public String generateTicketPdf(Ticket ticket) throws IOException, WriterException {
        // Créer le répertoire s'il n'existe pas
        Path dir = Paths.get(pdfDirectory);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // Générer le nom du fichier PDF
        String fileName = "ticket_" + ticket.getNumeroTicket() + ".pdf";
        String filePath = pdfDirectory + File.separator + fileName;

        // Créer le PDF
        try (FileOutputStream fos = new FileOutputStream(filePath);
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Titre
            Paragraph title = new Paragraph("TICKET DE CARBURANT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold();
            document.add(title);

            document.add(new Paragraph("\n"));

            // Informations de la station
            Station station = ticket.getStation();
            Paragraph stationInfo = new Paragraph("STATION: " + station.getNom())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold();
            document.add(stationInfo);

            Paragraph location = new Paragraph("Localisation: " + station.getLocalisation())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12);
            document.add(location);

            document.add(new Paragraph("\n"));

            // Tableau des détails du ticket
            Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            
            table.addCell("Numéro de Ticket:");
            table.addCell(ticket.getNumeroTicket());
            
            table.addCell("Numéro d'Ordre:");
            table.addCell(ticket.getNumeroOrdre());
            
            table.addCell("Date d'Approvisionnement:");
            table.addCell(ticket.getDateApprovisionnement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            table.addCell("Type de Carburant:");
            table.addCell(ticket.getTypeCarburant());
            
            table.addCell("Quantité (Litres):");
            table.addCell(ticket.getQuantite().toString());
            
            table.addCell("Montant Payé:");
            table.addCell(ticket.getMontantPaye() + " FCFA");
            
            table.addCell("Statut:");
            table.addCell(ticket.getStatut().toString());
            
            table.addCell("Date de Création:");
            table.addCell(ticket.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            document.add(table);

            document.add(new Paragraph("\n"));

            // Informations du citoyen
            Paragraph citizenInfo = new Paragraph("INFORMATIONS DU CITOYEN")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold();
            document.add(citizenInfo);

            Table citizenTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            
            citizenTable.addCell("Nom:");
            citizenTable.addCell(ticket.getNomCitoyen());
            
            citizenTable.addCell("Prénom:");
            citizenTable.addCell(ticket.getPrenomCitoyen());
            
            citizenTable.addCell("Email:");
            citizenTable.addCell(ticket.getEmailCitoyen());
            
            if (ticket.getTelephoneCitoyen() != null && !ticket.getTelephoneCitoyen().isEmpty()) {
                citizenTable.addCell("Téléphone:");
                citizenTable.addCell(ticket.getTelephoneCitoyen());
            }

            document.add(citizenTable);

            document.add(new Paragraph("\n"));

            // Générer et ajouter le QR code
            if (ticket.getQrCodeData() != null) {
                try {
                    BufferedImage qrCodeImage = generateQRCodeImage(ticket.getQrCodeData(), 200, 200);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(qrCodeImage, "PNG", baos);
                    byte[] imageBytes = baos.toByteArray();
                    
                    Image qrCode = new Image(com.itextpdf.io.image.ImageDataFactory.create(imageBytes));
                    qrCode.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                    document.add(qrCode);
                } catch (Exception e) {
                    log.error("Erreur lors de la génération du QR code: {}", e.getMessage());
                }
            }

            // Message de validation
            Paragraph validationMessage = new Paragraph("Ce ticket est valide pour l'approvisionnement en carburant à la date indiquée.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setItalic();
            document.add(validationMessage);

        }

        log.info("PDF généré avec succès: {}", filePath);
        return filePath;
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
