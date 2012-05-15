/**
 * Copyright 2011 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.multibit.qrcode;

/**
 * Uses code from com.google.zxing.qrcode.QRCodeWriter which is:
 * Copyright 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.multibit.Localiser;
import org.multibit.controller.MultiBitController;
import org.multibit.model.MultiBitModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.uri.BitcoinURI;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

/**
 * Class to generate swatches (QR codes + text of QR code in an image)
 * 
 * @author jim
 * 
 */
public class SwatchGenerator {

    private static final Logger log = LoggerFactory.getLogger(SwatchGenerator.class);

    // small
    private static final int QUIET_ZONE_SIZE = 4;
    private static final int WIDTH_OF_TEXT_BORDER = 2;
    private static final int LEFT_TEXT_INSET = 3;
    private static final int RIGHT_TEXT_INSET = 3;
    private static final int BOTTOM_TEXT_INSET = 4;
    private static final int TOP_TEXT_INSET = 2;
    private static final int GAP_BETWEEN_TEXT_ROWS = 2;
    private static final int GAP_ABOVE_ADDRESS = 4;
    private static int BOUNDING_BOX_PADDING = 2;
    private static int QR_CODE_ELEMENT_MULTIPLE = 2;

    private static Font addressFont;
    private static Font labelFont;
    private static Font amountFont;
    private static Map<Font, FontMetrics> fontToFontMetricsMap;

    private BufferedImage emptyImage;
    private Graphics2D emptyGraphics;
    private QRCode code;

    private MultiBitController controller;

    public SwatchGenerator(MultiBitController controller) {
        this.controller = controller;

        // graphics context - used to work out the width of the swatch
        emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        emptyGraphics = emptyImage.createGraphics();

        code = new QRCode();

        // fonts
        addressFont = new Font("Verdana", Font.PLAIN, 10); // 10 best
        labelFont = new Font("Serif", Font.PLAIN, 17); // 17 looks best
        amountFont = labelFont;

        // cached to save time
        fontToFontMetricsMap = new HashMap<Font, FontMetrics>();
        fontToFontMetricsMap.put(addressFont, emptyGraphics.getFontMetrics(addressFont));
        fontToFontMetricsMap.put(labelFont, emptyGraphics.getFontMetrics(labelFont));
        fontToFontMetricsMap.put(amountFont, emptyGraphics.getFontMetrics(amountFont));

        // make sure fonts are loaded
        JFrame frame = new JFrame();
    }

    public BufferedImage generateQRcode(String address, String amount, String label) {
        return generateQRcode(address, amount, label, 1);
    }

    /**
     * generate a QR code
     * 
     * @param address
     *            Bitcoin address to show
     * @param amount
     *            amount of BTC to show - text
     * @param label
     *            label for swatch
     * @return
     */
    public BufferedImage generateQRcode(String address, String amount, String label, int scaleFactor) {
        String bitcoinURI = "";
        try {
            Address decodeAddress = null;
            if (address != null && !"".equals(address) && controller.getMultiBitService() != null
                    && controller.getMultiBitService().getNetworkParameters() != null) {
                decodeAddress = new Address(controller.getMultiBitService().getNetworkParameters(), address);
            }
            if (amount != null && !"".equals(amount)) {
                bitcoinURI = BitcoinURI.convertToBitcoinURI(decodeAddress, Utils.toNanoCoins(amount), label, null);
            } else {
                bitcoinURI = BitcoinURI.convertToBitcoinURI(decodeAddress, null, label, null);
            }
            controller.getModel().setActiveWalletPreference(MultiBitModel.SEND_PERFORM_PASTE_NOW, "false");
        } catch (IllegalArgumentException e) {
            //log.warn("The address '" + address + "' could not be converted to a bitcoin address. (IAE)");
            return null;
        } catch (AddressFormatException e) {
            //log.warn("The address '" + address + "' could not be converted to a bitcoin address. (AFE)");
            return null;
        }

        // get a byte matrix for the data
        ByteMatrix matrix;
        try {
            matrix = encode(bitcoinURI);
        } catch (com.google.zxing.WriterException e) {
            // exit the method
            return null;
        } catch (IllegalArgumentException e) {
            // exit the method
            return null;
        }

        // generate an image from the byte matrix
        int matrixWidth = matrix.getWidth();
        int matrixHeight = matrix.getHeight();
        int swatchWidth = matrixWidth * scaleFactor;
        int swatchHeight = matrixHeight * scaleFactor;

        // create buffered image to draw to
        BufferedImage image = new BufferedImage(swatchWidth, swatchHeight, BufferedImage.TYPE_INT_RGB);

        // iterate through the matrix and draw the pixels to the image
        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                byte imageValue = matrix.get(x, y);
                for (int scaleX = 0; scaleX < scaleFactor; scaleX++) {
                    for (int scaleY = 0; scaleY < scaleFactor; scaleY++)  {
                        image.setRGB(x * scaleFactor + scaleX, y * scaleFactor + scaleY, imageValue);                       
                    }
                }
            }
        }

        return image;
    }

    public BufferedImage generateSwatch(String address, String amount, String label) {
        return generateSwatch(address, amount, label, 1);
    }

    /**
     * generate a Swatch
     * 
     * @param address
     *            Bitcoin address to show
     * @param amount
     *            amount of BTC to show - text
     * @param label
     *            label for swatch
     * @return
     */
    public BufferedImage generateSwatch(String address, String amount, String label, int scaleFactor) {
        Font addressFont = new Font("Verdana", Font.PLAIN, (int)(10 * scaleFactor));
        Font labelFont = new Font("Serif", Font.PLAIN, (int)(17 * scaleFactor));
        Font amountFont = labelFont;
        
        fontToFontMetricsMap.clear();
        fontToFontMetricsMap.put(addressFont, emptyGraphics.getFontMetrics(addressFont));
        fontToFontMetricsMap.put(labelFont, emptyGraphics.getFontMetrics(labelFont));
        fontToFontMetricsMap.put(amountFont, emptyGraphics.getFontMetrics(amountFont));


        String bitcoinURI = "";
        try {
            Address decodeAddress = null;
            if (address != null && !"".equals(address) && controller.getMultiBitService() != null
                    && controller.getMultiBitService().getNetworkParameters() != null) {
                decodeAddress = new Address(controller.getMultiBitService().getNetworkParameters(), address);
            }
            if (amount != null && !"".equals(amount)) {
                bitcoinURI = BitcoinURI.convertToBitcoinURI(decodeAddress, Utils.toNanoCoins(amount), label, null);
            } else {
                bitcoinURI = BitcoinURI.convertToBitcoinURI(decodeAddress, null, label, null);
            }
            controller.getModel().setActiveWalletPreference(MultiBitModel.SEND_PERFORM_PASTE_NOW, "false");
        } catch (IllegalArgumentException e) {
            //log.warn("The address '" + address + "' could not be converted to a bitcoin address.");
        } catch (AddressFormatException e) {
            //log.warn("The address '" + address + "' could not be converted to a bitcoin address.");
        }

        // get a byte matrix for the data
        ByteMatrix matrix;
        try {
            matrix = encode(bitcoinURI);
        } catch (com.google.zxing.WriterException e) {
            // exit the method
            return null;
        } catch (IllegalArgumentException e) {
            // exit the method
            return null;
        }

        boolean addAmount;
        if (amount == null || "".equals(amount)) {
            addAmount = false;
        } else {
            addAmount = true;
        }

        if (label == null) {
            label = "";
        }

        // generate an image from the byte matrix
        int matrixWidth = matrix.getWidth();
        int matrixHeight = matrix.getHeight();

        int addressAdvance = getAdvance(emptyGraphics, address, addressFont);
        int amountAdvance = 0;
        if (addAmount) {
            amountAdvance = getAdvance(emptyGraphics,
                    amount + " " + controller.getLocaliser().getString("sendBitcoinPanel.amountUnitLabel"), amountFont);
        }
        // convert backslash-rs to backslash-ns
        label = label.replaceAll("\r\n", "\n");
        label = label.replaceAll("\n\r", "\n");
        String[] labelLines = label.split("[\\n\\r]");

        int maxLabelAdvance = 0;

        if (labelLines != null) {
            for (int i = 0; i < labelLines.length; i++) {
                int labelAdvance = getAdvance(emptyGraphics, labelLines[i], labelFont);
                if (labelAdvance > maxLabelAdvance) {
                    maxLabelAdvance = labelAdvance;
                }
            }
        }

        int widestTextAdvance = (int) Math.max(Math.max(addressAdvance, amountAdvance), maxLabelAdvance);
        int swatchWidth = widestTextAdvance + scaleFactor * ( matrixWidth + LEFT_TEXT_INSET + RIGHT_TEXT_INSET + WIDTH_OF_TEXT_BORDER * 2
                + QUIET_ZONE_SIZE);

        // work out the height of the swatch
        int minimumBoxHeight = scaleFactor * (TOP_TEXT_INSET + BOTTOM_TEXT_INSET + 2 * (QUIET_ZONE_SIZE + WIDTH_OF_TEXT_BORDER)
                + GAP_ABOVE_ADDRESS) + addressFont.getSize();
        if (addAmount) {
            minimumBoxHeight = minimumBoxHeight + scaleFactor* GAP_BETWEEN_TEXT_ROWS + amountFont.getSize();
        }

        if (labelLines != null) {
            minimumBoxHeight = minimumBoxHeight +  labelLines.length * labelFont.getSize() + GAP_BETWEEN_TEXT_ROWS * scaleFactor * (labelLines.length - 1);
        }

        int swatchHeight;
        if (minimumBoxHeight > matrixHeight * scaleFactor) {
            swatchHeight = minimumBoxHeight;
        } else {
            swatchHeight = matrixHeight * scaleFactor;
        }

        // create buffered image to draw to
        BufferedImage image = new BufferedImage(swatchWidth, swatchHeight, BufferedImage.TYPE_INT_RGB);

        // iterate through the matrix and draw the pixels to the image
        int qrCodeVerticalOffset = 0;
        if (swatchHeight > matrixHeight * scaleFactor) {
            qrCodeVerticalOffset = (int) ((swatchHeight - matrixHeight * scaleFactor) * 0.5);
        }
        int matrixHorizontalOffset = 0;
        if (!ComponentOrientation.getOrientation(controller.getLocaliser().getLocale()).isLeftToRight()) {
            matrixHorizontalOffset = swatchWidth - matrixWidth * scaleFactor;
        }

        int textBoxHorizontalOffset = scaleFactor * matrixWidth;
        if (!ComponentOrientation.getOrientation(controller.getLocaliser().getLocale()).isLeftToRight()) {
            textBoxHorizontalOffset = scaleFactor * QUIET_ZONE_SIZE;
        }

        for (int y = 0; y < matrixHeight; y++) {
            for (int x = 0; x < matrixWidth; x++) {
                byte imageValue = matrix.get(x, y);
                //image.setRGB(x + matrixHorizontalOffset, y + qrCodeVerticalOffset, imageValue);
                for (int scaleX = 0; scaleX < scaleFactor; scaleX++) {
                    for (int scaleY = 0; scaleY < scaleFactor; scaleY++)  {
                        image.setRGB(x * scaleFactor + scaleX  + matrixHorizontalOffset, y * scaleFactor + scaleY  + qrCodeVerticalOffset, imageValue);                       
                        //image.setRGB(x * scaleFactor + scaleX, y * scaleFactor + scaleY, imageValue);                       
                    }
                }
            }
        }

        // fill in the rest of the image as white
        for (int y = 0; y < swatchHeight; y++) {
            if (matrixHorizontalOffset == 0) {
                for (int x = matrixWidth * scaleFactor; x < swatchWidth; x++) {
                    image.setRGB(x, y, 0xFFFFFF);
                }
            } else {
                for (int x = 0; x < swatchWidth - matrixWidth * scaleFactor; x++) {
                    image.setRGB(x, y, 0xFFFFFF);
                }
            }
        }
        if (swatchHeight > matrixHeight * scaleFactor) {
            for (int y = 0; y < qrCodeVerticalOffset; y++) {
                for (int x = 0; x < swatchWidth; x++) {
                    image.setRGB(x, y, 0xFFFFFF);
                }
            }

            for (int y = scaleFactor * matrixHeight + qrCodeVerticalOffset; y < swatchHeight; y++) {
                for (int x = 0; x < swatchWidth; x++) {
                    image.setRGB(x, y, 0xFFFFFF);
                }
            }
        }

        // draw the text box
        for (int y = QUIET_ZONE_SIZE * scaleFactor; y < swatchHeight - QUIET_ZONE_SIZE * scaleFactor; y++) {
            for (int loopX = 0; loopX < WIDTH_OF_TEXT_BORDER * scaleFactor; loopX++) {
                // left hand side
                image.setRGB(textBoxHorizontalOffset + loopX, y, 0x000000);

                // right hand side
                if (ComponentOrientation.getOrientation(controller.getLocaliser().getLocale()).isLeftToRight()) {
                    image.setRGB(swatchWidth - QUIET_ZONE_SIZE * scaleFactor - loopX - 1, y, 0x000000);
                } else {
                    image.setRGB(swatchWidth - matrixWidth * scaleFactor - loopX - 1, y, 0x000000);
                }
            }
        }

        for (int x = textBoxHorizontalOffset + scaleFactor * (QUIET_ZONE_SIZE - WIDTH_OF_TEXT_BORDER); x < swatchWidth - scaleFactor * (QUIET_ZONE_SIZE
                + matrixWidth) + textBoxHorizontalOffset; x++) {
            for (int loopY = 0; loopY < scaleFactor * WIDTH_OF_TEXT_BORDER; loopY++) {
                // top side
                image.setRGB(x, scaleFactor * QUIET_ZONE_SIZE + loopY, 0x000000);

                // bottom side
                image.setRGB(x, swatchHeight - scaleFactor * QUIET_ZONE_SIZE - loopY - 1, 0x000000);
            }
        }

        Graphics2D g2 = image.createGraphics();

        g2.setColor(Color.black);
        g2.setFont(addressFont);

        // right justified
        g2.drawString(address, swatchWidth - scaleFactor * (QUIET_ZONE_SIZE + WIDTH_OF_TEXT_BORDER + RIGHT_TEXT_INSET
                + matrixWidth) - addressAdvance + textBoxHorizontalOffset, swatchHeight - scaleFactor * (QUIET_ZONE_SIZE + WIDTH_OF_TEXT_BORDER + BOTTOM_TEXT_INSET));

        g2.setFont(labelFont);
        if (labelLines != null) {
            for (int i = 0; i < labelLines.length; i++) {
                if (ComponentOrientation.getOrientation(controller.getLocaliser().getLocale()).isLeftToRight()) {
                    // left justified
                    g2.drawString(labelLines[i], textBoxHorizontalOffset + scaleFactor * (QUIET_ZONE_SIZE + WIDTH_OF_TEXT_BORDER), scaleFactor * (QUIET_ZONE_SIZE
                            + TOP_TEXT_INSET) + labelFont.getSize() + i * (labelFont.getSize() + scaleFactor * GAP_BETWEEN_TEXT_ROWS));
                } else {
                    // right justified
                    int leftEdge = swatchWidth - scaleFactor * (matrixWidth + WIDTH_OF_TEXT_BORDER + RIGHT_TEXT_INSET)
                            - getAdvance(emptyGraphics, labelLines[i], labelFont);
                    g2.drawString(labelLines[i], leftEdge,
                            scaleFactor * (QUIET_ZONE_SIZE + TOP_TEXT_INSET) + labelFont.getSize() + i
                                    * (labelFont.getSize() + scaleFactor * GAP_BETWEEN_TEXT_ROWS));
                }
            }
            if (addAmount) {
                g2.setFont(amountFont);

                // bottom right justified
                if (ComponentOrientation.getOrientation(controller.getLocaliser().getLocale()).isLeftToRight()) {
                    g2.drawString(amount + " " + controller.getLocaliser().getString("sendBitcoinPanel.amountUnitLabel"),
                            swatchWidth - scaleFactor * (QUIET_ZONE_SIZE + WIDTH_OF_TEXT_BORDER + RIGHT_TEXT_INSET) - amountAdvance, swatchHeight
                                    - scaleFactor * (QUIET_ZONE_SIZE + WIDTH_OF_TEXT_BORDER + BOTTOM_TEXT_INSET) - addressFont.getSize()
                                    - scaleFactor * GAP_ABOVE_ADDRESS);
                } else {
                    // bottom right justified, with swatch to right
                    g2.drawString(amount + " " + controller.getLocaliser().getString("sendBitcoinPanel.amountUnitLabel"),
                            swatchWidth - scaleFactor * (matrixWidth + WIDTH_OF_TEXT_BORDER + RIGHT_TEXT_INSET) - amountAdvance, swatchHeight
                                    - scaleFactor * (QUIET_ZONE_SIZE + WIDTH_OF_TEXT_BORDER + BOTTOM_TEXT_INSET) - addressFont.getSize()
                                    - scaleFactor * GAP_ABOVE_ADDRESS);
                }
            }
        } else {
            if (addAmount) {
                g2.setFont(amountFont);
                // left justified, no swatch
                g2.drawString(amount + " " + controller.getLocaliser().getString("sendBitcoinPanel.amountUnitLabel"),
                        textBoxHorizontalOffset + scaleFactor * (QUIET_ZONE_SIZE + WIDTH_OF_TEXT_BORDER), scaleFactor * (QUIET_ZONE_SIZE + TOP_TEXT_INSET)
                                + amountFont.getSize());
            }
        }
        return image;
    }

    private int getAdvance(Graphics graphics, String text, Font font) {
        // get metrics from the graphics
        FontMetrics metrics = fontToFontMetricsMap.get(font);

        // get the advance of my text in this font and render context
        int advance = metrics.stringWidth(text);

        return advance + BOUNDING_BOX_PADDING;
    }

    public static void main(String[] args) {
        MultiBitController controller = new MultiBitController();
        Localiser localiser = new Localiser(new Locale("en"));
        controller.setLocaliser(localiser);
        SwatchGenerator swatchGenerator = new SwatchGenerator(controller);
        String address = "15BGmyMKxGFkejW1oyf2Gwv3NHqeUP7aWh";
        String amount = "0.423232";
        String label = "A longish label xyz";

        BufferedImage swatch = swatchGenerator.generateSwatch(address, amount, label);
        ImageIcon icon = null;
        if (swatch != null) {
            icon = new ImageIcon(swatch);
        }
        JOptionPane.showMessageDialog(null, "", "Swatch Generator 1", JOptionPane.INFORMATION_MESSAGE, icon);

        address = "1HB5XMLmzFVj8ALj6mfBsbifRoD4miY36v";
        amount = "0.5";
        label = "Donate to Wikileaks\nWith a second line";

        swatch = swatchGenerator.generateSwatch(address, amount, label);
        icon = new ImageIcon(swatch);
        JOptionPane.showMessageDialog(null, "", "Swatch Generator 2", JOptionPane.INFORMATION_MESSAGE, icon);

        address = "15BGmyMKxGFkejW1oyf2Gwv3NHqeUP7aWh";
        amount = "0.41";
        label = "";

        controller.getLocaliser().setLocale(new Locale("ar"));
        swatch = swatchGenerator.generateSwatch(address, amount, label);
        icon = new ImageIcon(swatch);
        JOptionPane.showMessageDialog(null, "", "Swatch Generator 3", JOptionPane.INFORMATION_MESSAGE, icon);

        address = "15BGmyMKxGFkejW1oyf2Gwv3NHqeUP7aWh";
        amount = "";
        label = "A longerer label xyzabc - with no amount";

        swatch = swatchGenerator.generateSwatch(address, amount, label);
        icon = new ImageIcon(swatch);
        JOptionPane.showMessageDialog(null, "", "Swatch Generator 4", JOptionPane.INFORMATION_MESSAGE, icon);

        address = "15BGmyMKxGFkejW1oyf2Gwv3NHqeUP7aWh";
        amount = "1.2";
        label = "Shorty\r\non three\rseparate lines";

        swatch = swatchGenerator.generateSwatch(address, amount, label);
        icon = new ImageIcon(swatch);
        JOptionPane.showMessageDialog(null, "", "Swatch Generator 5", JOptionPane.INFORMATION_MESSAGE, icon);

        // write the image to the output stream
        try {
            ImageIO.write(swatch, "png", new File("swatch.png"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This object renders a QR Code as a ByteMatrix 2D array of greyscale
     * values.
     * 
     * @author dswitkin@google.com (Daniel Switkin)
     */
    public ByteMatrix encode(String contents) throws WriterException {

        if (contents == null || contents.length() == 0) {
            throw new IllegalArgumentException("Found empty contents");
        }

        Encoder.encode(contents, ErrorCorrectionLevel.L, null, code);
        return renderResult(code, QR_CODE_ELEMENT_MULTIPLE);
    }

    // Note that the input matrix uses 0 == white, 1 == black, while the output
    // matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    private static ByteMatrix renderResult(QRCode code, int multiple) {
        ByteMatrix input = code.getMatrix();
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = multiple * inputWidth + (QUIET_ZONE_SIZE << 1);
        int qrHeight = multiple * inputHeight + (QUIET_ZONE_SIZE << 1);

        ByteMatrix output = new ByteMatrix(qrWidth, qrHeight);
        byte[][] outputArray = output.getArray();

        // We could be tricky and use the first row in each set of multiple as
        // the temporary storage,
        // instead of allocating this separate array.
        byte[] row = new byte[qrWidth];

        // 1. Write the white lines at the top
        for (int y = 0; y < QUIET_ZONE_SIZE; y++) {
            setRowColor(outputArray[y], (byte) 255);
        }

        // 2. Expand the QR image to the multiple
        byte[][] inputArray = input.getArray();
        for (int y = 0; y < inputHeight; y++) {
            // a. Write the white pixels at the left of each row
            for (int x = 0; x < QUIET_ZONE_SIZE; x++) {
                row[x] = (byte) 255;
            }

            // b. Write the contents of this row of the barcode
            int offset = QUIET_ZONE_SIZE;
            for (int x = 0; x < inputWidth; x++) {
                byte value = (inputArray[y][x] == 1) ? 0 : (byte) 255;
                for (int z = 0; z < multiple; z++) {
                    row[offset + z] = value;
                }
                offset += multiple;
            }

            // c. Write the white pixels at the right of each row
            offset = QUIET_ZONE_SIZE + (inputWidth * multiple);
            for (int x = offset; x < qrWidth; x++) {
                row[x] = (byte) 255;
            }

            // d. Write the completed row multiple times
            offset = QUIET_ZONE_SIZE + (y * multiple);
            for (int z = 0; z < multiple; z++) {
                System.arraycopy(row, 0, outputArray[offset + z], 0, qrWidth);
            }
        }

        // 3. Write the white lines at the bottom
        int offset = QUIET_ZONE_SIZE + (inputHeight * multiple);
        for (int y = offset; y < qrHeight; y++) {
            setRowColor(outputArray[y], (byte) 255);
        }

        return output;
    }

    private static void setRowColor(byte[] row, byte value) {
        for (int x = 0; x < row.length; x++) {
            row[x] = value;
        }
    }
}
