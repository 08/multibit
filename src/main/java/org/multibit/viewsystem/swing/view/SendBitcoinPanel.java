package org.multibit.viewsystem.swing.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.multibit.controller.MultiBitController;
import org.multibit.model.AddressBookData;
import org.multibit.model.DataProvider;
import org.multibit.model.MultiBitModel;
import org.multibit.viewsystem.View;
import org.multibit.viewsystem.swing.MultiBitFrame;
import org.multibit.viewsystem.swing.action.CopySendAddressAction;
import org.multibit.viewsystem.swing.action.CreateNewSendingAddressAction;
import org.multibit.viewsystem.swing.action.PasteAddressAction;
import org.multibit.viewsystem.swing.action.SendBitcoinConfirmAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendBitcoinPanel extends AbstractTradePanel implements DataProvider, View {

    private static final long serialVersionUID = -2065108865497111662L;

    private final Logger log = LoggerFactory.getLogger(SendBitcoinPanel.class);

    private static final String SEND_BITCOIN_BIG_ICON_FILE = "/images/send-big.jpg";
//    private static final String DRAG_HERE_ICON_FILE = "/images/dragHere.png";

//    private static final int MINIMUM_QRCODE_PANEL_HORIZONTAL_SPACING = 30;
//    private static final int MINIMUM_QRCODE_PANEL_VERTICAL_SPACING = 80;

    private JButton pasteAddressButton;
    private JButton sendButton;

    public SendBitcoinPanel(MultiBitFrame mainFrame, MultiBitController controller) {
        super(mainFrame, controller);
    }

    @Override
    protected boolean isReceiveBitcoin() {
        return false;
    }
   
    @Override
    protected Action getCreateNewAddressAction() {
        return new CreateNewSendingAddressAction(controller, this);
    }
    
    @Override
    protected String getAddressConstant() {
        return MultiBitModel.SEND_ADDRESS;
    }
    
    @Override
    protected String getLabelConstant() {
        return MultiBitModel.SEND_LABEL;
    }
    @Override
    protected String getAmountConstant() {
        return MultiBitModel.SEND_AMOUNT;
    }

    @Override
    protected String getUriImageConstant() {
        return MultiBitModel.SEND_URI_IMAGE;
    }

    /**
     * method for concrete impls to populate the localisation map
     */
    @Override
    protected void populateLocalisationMap() {
        localisationKeyConstantToKeyMap.put(ADDRESSES_TITLE, "sendBitcoinPanel.sendingAddressesTitle");                
        localisationKeyConstantToKeyMap.put(CREATE_NEW_TOOLTIP, "createOrEditAddressAction.createSending.tooltip");       
    }

    protected JPanel createFormPanel() {
        formPanel = new JPanel();
        formPanel.setBorder(new DashedBorder());
        formPanel.setBackground(MultiBitFrame.VERY_LIGHT_BACKGROUND_COLOR);

        JPanel buttonPanel = new JPanel();
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        buttonPanel.setLayout(flowLayout);

        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JPanel filler1 = new JPanel();
        filler1.setOpaque(false);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 5;
        constraints.weighty = 0.10;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(filler1, constraints);

        ImageIcon bigIcon = MultiBitFrame.createImageIcon(SEND_BITCOIN_BIG_ICON_FILE);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0.3;
        constraints.weighty = 0.08;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        formPanel.add(new JLabel(bigIcon), constraints);

        JLabel helpLabel1 = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.helpLabel1.message"));
        helpLabel1.setHorizontalAlignment(JLabel.LEFT);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 3;
        constraints.gridy = 1;
        constraints.weightx = 0.3;
        constraints.weighty = 0.08;
        constraints.gridwidth = 4;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(helpLabel1, constraints);

        JLabel helpLabel2 = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.helpLabel2.message"));
        helpLabel2.setHorizontalAlignment(JLabel.LEFT);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 3;
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.08;
        constraints.gridwidth = 4;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(helpLabel2, constraints);

        JLabel helpLabel3 = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.helpLabel3.message"));
        helpLabel3.setHorizontalAlignment(JLabel.LEFT);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 3;
        constraints.gridy = 3;
        constraints.weightx = 0.3;
        constraints.weighty = 0.08;
        constraints.gridwidth = 4;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(helpLabel3, constraints);

        JPanel filler2 = new JPanel();
        filler2.setOpaque(false);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.weightx = 0.05;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(filler2, constraints);

        JLabel addressLabel = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.addressLabel"));
        addressLabel.setToolTipText(controller.getLocaliser().getString("sendBitcoinPanel.addressLabel.tooltip"));
        addressLabel.setHorizontalAlignment(JLabel.RIGHT);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.weightx = 4.0;
        constraints.weighty = 0.15;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        formPanel.add(addressLabel, constraints);

        JLabel filler4 = new JLabel("");
        filler4.setOpaque(false);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2;
        constraints.gridy = 4;
        constraints.weightx = 1;
        constraints.weighty = 0.5;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(filler4, constraints);

        addressTextField = new JTextField(35);
        addressTextField.setHorizontalAlignment(JTextField.LEFT);
        addressTextField.setMinimumSize(new Dimension(MultiBitFrame.WIDTH_OF_LONG_FIELDS, 24));
        addressTextField.setMaximumSize(new Dimension(MultiBitFrame.WIDTH_OF_LONG_FIELDS, 24));

        addressTextField.addKeyListener(new QRCodeKeyListener());
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 3;
        constraints.gridy = 4;
        constraints.weightx = 0.1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(addressTextField, constraints);

        ImageIcon copyIcon = MultiBitFrame.createImageIcon(MultiBitFrame.COPY_ICON_FILE);
        CopySendAddressAction copyAddressAction = new CopySendAddressAction(controller, this, copyIcon);
        JButton copyAddressButton = new JButton(copyAddressAction);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 6;
        constraints.gridy = 4;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(copyAddressButton, constraints);

        ImageIcon pasteIcon = MultiBitFrame.createImageIcon(MultiBitFrame.PASTE_ICON_FILE);
        PasteAddressAction pasteAddressAction = new PasteAddressAction(controller, this, pasteIcon);
        pasteAddressButton = new JButton(pasteAddressAction);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 7;
        constraints.gridy = 4;
        constraints.weightx = 3;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(pasteAddressButton, constraints);

        JLabel labelLabel = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.labelLabel"));
        labelLabel.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, MultiBitFrame.VERY_LIGHT_BACKGROUND_COLOR));
        labelLabel.setToolTipText(controller.getLocaliser().getString("sendBitcoinPanel.labelLabel.tooltip"));
        labelLabel.setHorizontalAlignment(JLabel.RIGHT);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.weightx = 0.3;
        constraints.weighty = 0.15;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        formPanel.add(labelLabel, constraints);

        JTextField aTextField = new JTextField();
        labelTextArea = new JTextArea("", 2, 20);
        labelTextArea.setBorder(aTextField.getBorder());
        labelTextArea.addKeyListener(new QRCodeKeyListener());

        JScrollPane labelScrollPane = new JScrollPane(labelTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        labelScrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, MultiBitFrame.DARK_BACKGROUND_COLOR));
        labelScrollPane.setOpaque(true);
        labelScrollPane.setBackground(MultiBitFrame.VERY_LIGHT_BACKGROUND_COLOR);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 3;
        constraints.gridy = 5;
        constraints.weightx = 0.15;
        constraints.weighty = 0.40;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(labelScrollPane, constraints);

        JPanel filler5 = new JPanel();
        filler5.setOpaque(false);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 5;
        constraints.gridy = 5;
        constraints.weightx = 1;
        constraints.weighty = 0.4;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(filler5, constraints);

        JLabel amountLabel = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.amountLabel"));
        amountLabel.setToolTipText(controller.getLocaliser().getString("sendBitcoinPanel.amountLabel.tooltip"));
        amountLabel.setHorizontalAlignment(JLabel.RIGHT);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.gridwidth = 1;
        constraints.weightx = 0.3;
        constraints.weighty = 0.30;
        constraints.anchor = GridBagConstraints.LINE_END;
        formPanel.add(amountLabel, constraints);

        amountTextField = new JTextField("", 20);
        amountTextField.setHorizontalAlignment(JTextField.RIGHT);
        amountTextField.setMinimumSize(new Dimension(MultiBitFrame.WIDTH_OF_AMOUNT_FIELD, 24));
        amountTextField.setMaximumSize(new Dimension(MultiBitFrame.WIDTH_OF_AMOUNT_FIELD, 24));
        amountTextField.addKeyListener(new QRCodeKeyListener());

        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 3;
        constraints.gridy = 6;
        constraints.weightx = 0.1;
        constraints.weighty = 0.5;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(amountTextField, constraints);

        JLabel amountUnitLabel = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.amountUnitLabel"));
        amountUnitLabel.setToolTipText(controller.getLocaliser().getString("sendBitcoinPanel.amountUnitLabel.tooltip"));
        constraints.gridx = 4;
        constraints.gridy = 6;
        constraints.weightx = 2.0;
        constraints.weighty = 0.30;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(amountUnitLabel, constraints);

        SendBitcoinConfirmAction sendBitcoinConfirmAction = new SendBitcoinConfirmAction(controller, this);
        sendButton = new JButton(sendBitcoinConfirmAction);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 6;
        constraints.gridy = 6;
        constraints.weightx = 10;
        constraints.weighty = 0.4;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(sendButton, constraints);

        return formPanel;
    }

//    protected JPanel createQRCodePanel() {
//        qrCodePanel = new JPanel();
//        qrCodePanel.setBackground(MultiBitFrame.VERY_LIGHT_BACKGROUND_COLOR);
//
//        qrCodePanel.setMinimumSize(new Dimension(280, 200));
//        qrCodePanel.setLayout(new GridBagLayout());
//        qrCodeLabel = new JLabel("", MultiBitFrame.createImageIcon(DRAG_HERE_ICON_FILE), JLabel.CENTER);
//        qrCodeLabel.setMinimumSize(new Dimension(QRCODE_WIDTH, QRCODE_HEIGHT));
//        qrCodeLabel.setToolTipText(controller.getLocaliser().getString("sendBitcoinPanel.dragBitcoinLabel.tooltip"));
//
//        qrCodeLabel.setVerticalTextPosition(JLabel.BOTTOM);
//        qrCodeLabel.setHorizontalTextPosition(JLabel.CENTER);
//
//        // copy image support
//        qrCodeLabel.setTransferHandler(new ImageSelection());
//
//        // drag support
//        MouseListener listener = new MouseAdapter() {
//            public void mousePressed(MouseEvent me) {
//                JComponent comp = (JComponent) me.getSource();
//                TransferHandler handler = comp.getTransferHandler();
//                handler.exportAsDrag(comp, me, TransferHandler.COPY);
//            }
//        };
//        qrCodeLabel.addMouseListener(listener);
//
//        GridBagConstraints constraints = new GridBagConstraints();
//
//        JPanel filler1 = new JPanel();
//        filler1.setOpaque(false);
//        constraints.fill = GridBagConstraints.NONE;
//        constraints.gridx = 0;
//        constraints.gridy = 0;
//        constraints.weightx = 0.01;
//        constraints.weighty = 0.2;
//        constraints.gridwidth = 1;
//        constraints.gridheight = 1;
//        constraints.anchor = GridBagConstraints.CENTER;
//        qrCodePanel.add(filler1, constraints);
//
//        constraints.fill = GridBagConstraints.BOTH;
//        constraints.gridx = 1;
//        constraints.gridy = 1;
//        constraints.weightx = 1;
//        constraints.weighty = 0.3;
//        constraints.gridwidth = 1;
//        constraints.anchor = GridBagConstraints.CENTER;
//        qrCodePanel.add(qrCodeLabel, constraints);
//
//        JPanel filler2 = new JPanel();
//        filler2.setOpaque(false);
//        constraints.fill = GridBagConstraints.NONE;
//        constraints.gridx = 2;
//        constraints.gridy = 3;
//        constraints.weightx = 0.01;
//        constraints.weighty = 0.3;
//        constraints.gridwidth = 1;
//        constraints.gridheight = 1;
//        constraints.anchor = GridBagConstraints.LINE_END;
//        qrCodePanel.add(filler2, constraints);
//
//        return qrCodePanel;
//    }

    public void loadForm() {
        // get the current address, label and amount from the model
        String address = controller.getModel().getActiveWalletPreference(MultiBitModel.SEND_ADDRESS);
        String label = controller.getModel().getActiveWalletPreference(MultiBitModel.SEND_LABEL);
        String amount = controller.getModel().getActiveWalletPreference(MultiBitModel.SEND_AMOUNT);

        if (address != null) {
            addressTextField.setText(address);
        }
        if (label != null) {
            labelTextArea.setText(label);
        }
        if (amount != null) {
            amountTextField.setText(amount);
        }
    }

    public void setAddressBookDataByRow(AddressBookData addressBookData) {
        addressTextField.setText(addressBookData.getAddress());
        addressesTableModel.setAddressBookDataByRow(addressBookData, selectedAddressRow, false);
    }

//    class ImageSelection extends TransferHandler implements Transferable {
//        private static final long serialVersionUID = 756395092284264645L;
//
//        private DataFlavor flavors[];
//        private DataFlavor urlFlavor, uriListAsStringFlavor, uriListAsReaderFlavor;
//
//        private Image image;
//
//        public int getSourceActions(JComponent c) {
//            return TransferHandler.COPY;
//        }
//
//        public boolean canImport(JComponent comp, DataFlavor flavor[]) {
//            if (!(comp instanceof JLabel) && !(comp instanceof AbstractButton)) {
//                return false;
//            }
//
//            if (flavors == null) {
//                flavors = new DataFlavor[] { DataFlavor.imageFlavor };
//                try {
//                    urlFlavor = new DataFlavor("application/x-java-url; class=java.net.URL");
//                    uriListAsStringFlavor = new DataFlavor("text/uri-list; class=java.lang.String");
//                    uriListAsReaderFlavor = new DataFlavor("text/uri-list;class=java.io.Reader");
//                    flavors = new DataFlavor[] { DataFlavor.imageFlavor, urlFlavor, uriListAsStringFlavor,
//                            uriListAsReaderFlavor };
//                } catch (ClassNotFoundException cnfe) {
//                    cnfe.printStackTrace();
//                }
//            }
//
//            for (int i = 0, n = flavor.length; i < n; i++) {
//                for (int j = 0, m = flavors.length; j < m; j++) {
//                    if (flavor[i].equals(flavors[j])) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }
//
//        public Transferable createTransferable(JComponent comp) {
//            // Clear
//            image = null;
//
//            if (comp instanceof JLabel) {
//                JLabel label = (JLabel) comp;
//                Icon icon = label.getIcon();
//                if (icon instanceof ImageIcon) {
//                    image = ((ImageIcon) icon).getImage();
//                    return this;
//                }
//            }
//            return null;
//        }
//
//        private boolean processDecodedString(String decodedString, JLabel label, ImageIcon icon) {
//            // decode the string to an AddressBookData
//            BitcoinURI bitcoinURI = new BitcoinURI(controller, decodedString);
//
//            if (bitcoinURI.isParsedOk()) {
//                log.debug("SendBitcoinPanel - ping 1");
//                Address address = bitcoinURI.getAddress();
//                log.debug("SendBitcoinPanel - ping 2");
//                String addressString = address.toString();
//                log.debug("SendBitcoinPanel - ping 3");
//                String amountString = amountTextField.getText();
//                if (bitcoinURI.getAmount() != null) {
//                    amountString = Localiser.bitcoinValueToString4(bitcoinURI.getAmount(), false, false);
//                }
//                log.debug("SendBitcoinPanel - ping 4");
//                String decodedLabel = bitcoinURI.getLabel();
//
//                log.debug("SendBitcoinPanel#imageSelection#importData = addressString = " + addressString + ", amountString = "
//                        + amountString + ", label = " + decodedLabel);
//                log.debug("SendBitcoinPanel - ping 5");
//
//                AddressBookData addressBookData = new AddressBookData(decodedLabel, addressString);
//                log.debug("SendBitcoinPanel - ping 6");
//                // see if the address is already in the address book
//                // see if the current address is on the table and
//                // select it
//                int rowToSelect = addressesTableModel.findRowByAddress(addressBookData.getAddress(), false);
//                if (rowToSelect >= 0) {
//                    addressesTableModel.setAddressBookDataByRow(addressBookData, rowToSelect, false);
//                    addressesTable.getSelectionModel().setSelectionInterval(rowToSelect, rowToSelect);
//                    selectedAddressRow = rowToSelect;
//                } else {
//                    // add a new row to the table
//                    controller.getModel().getActiveWalletWalletInfo().addSendingAddress(addressBookData);
//                    controller.getModel().getActivePerWalletModelData().setDirty(true);
//
//                    // select new row
//                    rowToSelect = addressesTableModel.findRowByAddress(addressBookData.getAddress(), false);
//                    if (rowToSelect >= 0) {
//                        addressesTable.getSelectionModel().setSelectionInterval(rowToSelect, rowToSelect);
//                        selectedAddressRow = rowToSelect;
//                    }
//                }
//                // scroll to visible
//                addressesTable.scrollRectToVisible(addressesTable.getCellRect(rowToSelect, 0, false));
//                addressesTable.invalidate();
//                addressesTable.validate();
//                addressesTable.repaint();
//                mainFrame.invalidate();
//                mainFrame.validate();
//                mainFrame.repaint();
//
//                log.debug("SendBitcoinPanel - ping 7");
//                controller.getModel().setActiveWalletPreference(MultiBitModel.SEND_ADDRESS, addressString);
//                log.debug("SendBitcoinPanel - ping 8");
//                controller.getModel().setActiveWalletPreference(MultiBitModel.SEND_LABEL, decodedLabel);
//                log.debug("SendBitcoinPanel - ping 9");
//
//                controller.getModel().setActiveWalletPreference(MultiBitModel.SEND_AMOUNT, amountString);
//                log.debug("SendBitcoinPanel - ping 10");
//                addressTextField.setText(addressString);
//                log.debug("SendBitcoinPanel - ping 11");
//                amountTextField.setText(amountString);
//                log.debug("SendBitcoinPanel - ping 12");
//                labelTextArea.setText(decodedLabel);
//                log.debug("SendBitcoinPanel - ping 13");
//                mainFrame.updateStatusLabel("");
//                label.setIcon(icon);
//                label.setToolTipText(decodedString);
//                return true;
//            } else {
//                mainFrame.updateStatusLabel(controller.getLocaliser().getString("sendBitcoinPanel.couldNotUnderstandQRcode",
//                        new Object[] { decodedString }));
//                return false;
//            }
//
//        }
//
//        public boolean importData(JComponent comp, Transferable transferable) {
//            if (comp instanceof JLabel) {
//                log.debug("importData - 1");
//
//                JLabel label = (JLabel) comp;
//                image = getDropData(transferable, label);
//                log.debug("importData - 2 - image = " + image);
//
//                if (image != null) {
//                    BufferedImage bufferedImage;
//                    log.debug("importData - 2.1");
//                    if (image.getWidth(qrCodeLabel) + MINIMUM_QRCODE_PANEL_HORIZONTAL_SPACING > qrCodePanel.getWidth()
//                            || image.getHeight(qrCodeLabel) + MINIMUM_QRCODE_PANEL_VERTICAL_SPACING > qrCodePanel.getHeight()) {
//                        // scale image
//                        double qrCodeWidth = (double) qrCodePanel.getWidth();
//                        double qrCodeHeight = (double) qrCodePanel.getHeight();
//                        double xScale = qrCodeWidth
//                                / (double) (image.getWidth(qrCodeLabel) + MINIMUM_QRCODE_PANEL_HORIZONTAL_SPACING);
//                        double yScale = qrCodeHeight
//                                / (double) (image.getHeight(qrCodeLabel) + MINIMUM_QRCODE_PANEL_VERTICAL_SPACING);
//                        double scaleFactor = Math.min(xScale, yScale);
//                        bufferedImage = toBufferedImage(image, (int) (image.getWidth(qrCodeLabel) * scaleFactor),
//                                (int) (image.getHeight(qrCodeLabel) * scaleFactor));
//                    } else {
//                        // no resize
//                        bufferedImage = toBufferedImage(image, -1, -1);
//                    }
//                    log.debug("importData - 2.2");
//                    ImageIcon icon = new ImageIcon(bufferedImage);
//
//                    // decode the QRCode to a String
//                    QRCodeEncoderDecoder qrCodeEncoderDecoder = new QRCodeEncoderDecoder(image.getWidth(qrCodeLabel),
//                            image.getHeight(qrCodeLabel));
//                    log.debug("importData - 2.3");
//
//                    String decodedString = qrCodeEncoderDecoder.decode(toBufferedImage(image, -1, -1));
//                    log.debug("importData - 3 - decodedResult = " + decodedString);
//                    log.info("SendBitcoinPanel#imageSelection#importData = decodedString = {}", decodedString);
//                    return processDecodedString(decodedString, label, icon);
//                }
//            }
//            return false;
//        }
//
//        @SuppressWarnings("rawtypes")
//        private Image getDropData(Transferable transferable, JComponent label) {
//            try {
//                // try to get an image
//                if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
//                    log.debug("image flavor is supported");
//                    Image img = (Image) transferable.getTransferData(DataFlavor.imageFlavor);
//                    if (img != null && img.getWidth(null) != -1) {
//                        return img;
//                    }
//                }
//                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
//                    log.debug("javaFileList is supported");
//                    java.util.List list = (java.util.List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
//                    for (Object aList : list) {
//                        File f = (File) aList;
//                        ImageIcon icon = new ImageIcon(f.getAbsolutePath());
//                        if (icon.getImage() != null) {
//                            return icon.getImage();
//                        }
//                    }
//                }
//
//                if (transferable.isDataFlavorSupported(uriListAsStringFlavor)) {
//                    log.debug("uriListAsStringFlavor is supported");
//                    String uris = (String) transferable.getTransferData(uriListAsStringFlavor);
//
//                    // url-lists are defined by rfc 2483 as crlf-delimited
//                    // TODO iterate over list for all of them
//                    StringTokenizer izer = new StringTokenizer(uris, "\r\n");
//                    if (izer.hasMoreTokens()) {
//                        String uri = izer.nextToken();
//                        log.debug("uri = " + uri);
//                        java.awt.Image image = getURLImage(new URL(uri));
//
//                        if (image != null) {
//                            return image;
//                        }
//
//                        ImageIcon uriIcon = new ImageIcon(uri);
//                        if (uriIcon.getImage() != null) {
//                            return uriIcon.getImage();
//                        }
//                    }
//                }
//
//                if (transferable.isDataFlavorSupported(uriListAsReaderFlavor)) {
//                    log.debug("uriListAsReaderFlavor is supported");
//
//                    BufferedReader read = new BufferedReader(uriListAsReaderFlavor.getReaderForText(transferable));
//                    // Remove 'file://' from file name
//                    String fileName = read.readLine().substring(7).replace("%20", " ");
//                    // Remove 'localhost' from OS X file names
//                    if (fileName.substring(0, 9).equals("localhost")) {
//                        fileName = fileName.substring(9);
//                    }
//                    read.close();
//
//                    java.awt.Image image = getFileImage(new File(fileName));
//
//                    if (image != null) {
//                        return image;
//                    }
//                }
//
//                if (transferable.isDataFlavorSupported(urlFlavor)) {
//                    log.debug("urlFlavor is supported");
//                    URL url = (URL) transferable.getTransferData(urlFlavor);
//                    log.debug("url = " + url);
//                    java.awt.Image image = getURLImage(url);
//
//                    if (image != null) {
//                        return image;
//                    }
//
//                    ImageIcon urlIcon = new ImageIcon(url);
//                    if (urlIcon.getImage() != null) {
//                        return urlIcon.getImage();
//                    }
//                }
//            } catch (IOException ioe) {
//                ioe.printStackTrace();
//            } catch (UnsupportedFlavorException e) {
//
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        private Image getURLImage(URL url) {
//            Image imageToReturn = null;
//
//            try {
//                imageToReturn = ImageIO.read(url);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return imageToReturn;
//        }
//
//        private Image getFileImage(File file) {
//            Image imageToReturn = null;
//
//            try {
//                imageToReturn = ImageIO.read(file);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return imageToReturn;
//        }
//
//        // Transferable
//        public Object getTransferData(DataFlavor flavor) {
//            if (isDataFlavorSupported(flavor)) {
//                if (DataFlavor.imageFlavor.equals(flavor)) {
//                    return image;
//                } else {
//                    if (DataFlavor.javaFileListFlavor.equals(flavor)) {
//                        java.util.List<File> list = new java.util.LinkedList<File>();
//
//                        // write the image to the output stream
//                        File swatchFile = new File("swatch.png");
//                        try {
//                            ImageIO.write(toBufferedImage(image, -1, -1), "png", new File("swatch.png"));
//                        } catch (IOException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                        list.add(swatchFile);
//                        return list;
//
//                    }
//                }
//            }
//            return null;
//        }
//
//        public DataFlavor[] getTransferDataFlavors() {
//            return flavors;
//        }
//
//        public boolean isDataFlavorSupported(DataFlavor flavor) {
//            for (int j = 0, m = flavors.length; j < m; j++) {
//                if (flavor.equals(flavors[j])) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        public BufferedImage toBufferedImage(Image image, int width, int height) {
//            log.debug("SendBitCoinPanel#toBufferedImage - 1");
//            if (image == null) {
//                return null;
//            }
//            if (width == -1) {
//                width = image.getWidth(null);
//            }
//            if (height == -1) {
//                height = image.getHeight(null);
//            }
//            // draw original image to thumbnail image object and
//            // scale it to the new size on-the-fly
//            log.debug("SendBitCoinPanel#toBufferedImage - 2.2, image = " + image + ",width = " + width + ", height = " + height);
//
//            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//            log.debug("SendBitCoinPanel#toBufferedImage - 2.3, bufferedImage = " + bufferedImage);
//
//            Graphics2D g2 = bufferedImage.createGraphics();
//
//            log.debug("SendBitCoinPanel#toBufferedImage - 3");
//            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//            g2.drawImage(image, 0, 0, width, height, null);
//            log.debug("SendBitCoinPanel#toBufferedImage - 4");
//            g2.dispose();
//            return bufferedImage;
//        }
//
//        // This method returns a buffered image with the contents of an image
//        public BufferedImage toBufferedImage2(Image image, int width, int height) {
//            if (width == -1) {
//                width = image.getWidth(null);
//            }
//            if (height == -1) {
//                height = image.getHeight(null);
//            }
//
//            // This code ensures that all the pixels in the image are loaded
//            image = new ImageIcon(image).getImage();
//            log.debug("SendBitCoinPanel#toBufferedImage - 2");
//
//            // Create a buffered image with a format that's compatible with the
//            // screen
//            BufferedImage bimage = null;
//            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//            log.debug("SendBitCoinPanel#toBufferedImage - 2.1");
//            try {
//                // Determine the type of transparency of the new buffered image
//                int transparency = Transparency.OPAQUE;
//
//                // Create the buffered image
//                GraphicsDevice gs = ge.getDefaultScreenDevice();
//                log.debug("SendBitCoinPanel#toBufferedImage - 2.2");
//
//                GraphicsConfiguration gc = gs.getDefaultConfiguration();
//                log.debug("SendBitCoinPanel#toBufferedImage - 2.3, image = " + image + ",width = " + width + ", height = "
//                        + height);
//
//                bimage = gc.createCompatibleImage(width, height, transparency);
//                log.debug("SendBitCoinPanel#toBufferedImage - 2.4");
//
//            } catch (HeadlessException e) {
//                // The system does not have a screen
//            }
//            log.debug("SendBitCoinPanel#toBufferedImage - 3 - bimage = " + bimage);
//
//            if (bimage == null) {
//                // Create a buffered image using the default color model
//                int type = BufferedImage.TYPE_INT_RGB;
//                bimage = new BufferedImage(width, height, type);
//            }
//
//            // Copy image to buffered image
//            Graphics2D g = bimage.createGraphics();
//
//            // Paint the image onto the buffered image
//            g.drawImage(image, 0, 0, width, height, null);
//
//            g.dispose();
//
//            log.debug("SendBitCoinPanel#toBufferedImage - 4 - bimage = " + bimage);
//
//            return bimage;
//        }
//    }

    @Override
    public void displayView() {
        super.displayView();
        updateView();
    }

    @Override
    public void updateView() {
        super.updateView();
        // disable any new changes if another process has changed the wallet
        if (controller.getModel().getActivePerWalletModelData() != null
                && controller.getModel().getActivePerWalletModelData().isFilesHaveBeenChangedByAnotherProcess()) {
            // files have been changed by another process - disallow edits
            mainFrame.setUpdatesStoppedTooltip(addressTextField);
            addressTextField.setEditable(false);
            addressTextField.setEnabled(false);

            if (sendButton != null) {
                sendButton.setEnabled(false);
                mainFrame.setUpdatesStoppedTooltip(sendButton);
            }
            if (pasteAddressButton != null) {
                pasteAddressButton.setEnabled(false);
                mainFrame.setUpdatesStoppedTooltip(pasteAddressButton);
            }
            titleLabel.setText(controller.getLocaliser().getString("sendBitcoinPanel.sendingAddressesTitle.mayBeOutOfDate"));
            mainFrame.setUpdatesStoppedTooltip(titleLabel);                 
        } else {
            addressTextField.setToolTipText(null);
            addressTextField.setEditable(true);
            addressTextField.setEnabled(true);

            if (sendButton != null) {
                sendButton.setEnabled(true);
                sendButton.setToolTipText(controller.getLocaliser().getString("sendBitcoinAction.tooltip"));
            }
            if (pasteAddressButton != null) {
                pasteAddressButton.setEnabled(true);
                pasteAddressButton.setToolTipText(controller.getLocaliser().getString("pasteAddressAction.tooltip"));
            }
            titleLabel.setText(controller.getLocaliser().getString("sendBitcoinPanel.sendingAddressesTitle"));
            titleLabel.setToolTipText(null);
        }
    }
}
