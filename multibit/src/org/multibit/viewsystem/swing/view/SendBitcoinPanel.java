package org.multibit.viewsystem.swing.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.multibit.action.Action;
import org.multibit.action.TextTransfer;
import org.multibit.controller.ActionForward;
import org.multibit.controller.MultiBitController;
import org.multibit.model.AddressBookData;
import org.multibit.model.Data;
import org.multibit.model.DataProvider;
import org.multibit.model.Item;
import org.multibit.model.MultiBitModel;
import org.multibit.qrcode.BitcoinURI;
import org.multibit.qrcode.ImageSelection;
import org.multibit.qrcode.QRCodeEncoderDecoder;
import org.multibit.viewsystem.View;
import org.multibit.viewsystem.swing.MultiBitFrame;
import org.multibit.viewsystem.swing.action.CopyQRCodeImageAction;
import org.multibit.viewsystem.swing.action.CopyQRCodeTextAction;
import org.multibit.viewsystem.swing.action.CreateNewSendingAddressAction;
import org.multibit.viewsystem.swing.action.PasteAddressAction;
import org.multibit.viewsystem.swing.action.SendBitcoinConfirmAction;

public class SendBitcoinPanel extends JPanel implements DataProvider, View {

    private static final long serialVersionUID = -2065108865497111662L;

    private static final String SEND_BITCOIN_BIG_ICON_FILE = "/images/send-big.jpg";

    private MultiBitController controller;

    private JTextField addressTextField;

    private JTextField labelTextField;

    private JTextField amountTextField;

    private JPanel formPanel;
    
    //private JButton copyQRCodeTextButton;

    private AddressBookTableModel addressesTableModel;

    private JTable addressesTable;

    private SelectionListener addressesListener;

    private int selectedAddressRow;

    private JLabel qrCodeLabel;
    private JTextArea qrCodeTextArea;
    private QRCodeEncoderDecoder qrCodeEncoderDecoder;
    private static final int QRCODE_WIDTH = 140;
    private static final int QRCODE_HEIGHT = 140;

    public SendBitcoinPanel(JFrame mainFrame, MultiBitController controller) {
        this.controller = controller;

        initUI();
        loadForm();

        labelTextField.requestFocusInWindow();
    }

    private void initUI() {
        setMinimumSize(new Dimension(550, 220));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.4;
        constraints.weighty = 0.4;
        constraints.anchor = GridBagConstraints.LINE_START;
        add(createFormPanel(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 0.6;
        constraints.weighty = 0.4;
        constraints.anchor = GridBagConstraints.LINE_START;
        add(createQRCodePanel(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        constraints.weighty = 1.2;
        constraints.anchor = GridBagConstraints.LINE_START;
        add(createAddressesPanel(), constraints);
    }

    private JPanel createFormPanel() {
        formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

        JPanel buttonPanel = new JPanel();
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        buttonPanel.setLayout(flowLayout);

        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        JPanel filler1 = new JPanel();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.05;
        constraints.weighty = 0.10;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(filler1, constraints);

        ImageIcon bigIcon = createImageIcon(SEND_BITCOIN_BIG_ICON_FILE);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0.3;
        constraints.weighty = 0.08;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.SOUTHWEST;
        formPanel.add(new JLabel(bigIcon), constraints);

        JLabel helpLabel1 = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.helpLabel1.message"));
        helpLabel1.setHorizontalAlignment(JLabel.LEFT);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.weightx = 0.3;
        constraints.weighty = 0.08;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(helpLabel1, constraints);

        JLabel helpLabel2 = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.helpLabel2.message"));
        helpLabel2.setHorizontalAlignment(JLabel.LEFT);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        constraints.weighty = 0.08;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(helpLabel2, constraints);

        JLabel helpLabel3 = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.helpLabel3.message"));
        helpLabel3.setHorizontalAlignment(JLabel.LEFT);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2;
        constraints.gridy = 3;
        constraints.weightx = 0.3;
        constraints.weighty = 0.08;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(helpLabel3, constraints);

        JPanel filler2 = new JPanel();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.weightx = 0.05;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(filler2, constraints);

        JPanel filler3 = new JPanel();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.weightx = 0.3;
        constraints.weighty = 0.1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(filler3, constraints);

        JLabel addressLabel = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.addressLabel"));
        addressLabel.setToolTipText(controller.getLocaliser().getString("sendBitcoinPanel.addressLabel.tooltip"));
        addressLabel.setHorizontalAlignment(JLabel.RIGHT);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.weightx = 0.3;
        constraints.weighty = 0.15;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        formPanel.add(addressLabel, constraints);

        addressTextField = new JTextField();
        addressTextField.setHorizontalAlignment(JTextField.LEFT);
//        addressTextField.setMinimumSize(new Dimension(80, 18));
 //       addressTextField.setMaximumSize(new Dimension(80, 18));
        addressTextField.addKeyListener(new QRCodeKeyListener());

        constraints.gridx = 2;
        constraints.gridy = 5;
        constraints.weightx = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(addressTextField, constraints);

        PasteAddressAction pasteAddressAction = new PasteAddressAction(controller, this);
        JButton pasteAddressButton = new JButton(pasteAddressAction);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 4;
        constraints.gridy = 5;
        constraints.weightx = 0.1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(pasteAddressButton, constraints);

        JLabel labelLabel = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.labelLabel"));
        labelLabel.setToolTipText(controller.getLocaliser().getString("sendBitcoinPanel.labelLabel.tooltip"));
        labelLabel.setHorizontalAlignment(JLabel.RIGHT);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 6;
        constraints.weightx = 0.3;
        constraints.weighty = 0.1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        formPanel.add(labelLabel, constraints);

        labelTextField = new JTextField("", 35);
        labelTextField.setHorizontalAlignment(JTextField.LEFT);
        labelTextField.addKeyListener(new QRCodeKeyListener());
        constraints.gridx = 2;
        constraints.gridy = 6;
        constraints.weightx = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(labelTextField, constraints);

        JLabel amountLabel = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.amountLabel"));
        amountLabel.setToolTipText(controller.getLocaliser().getString("sendBitcoinPanel.amountLabel.tooltip"));
        amountLabel.setHorizontalAlignment(JLabel.RIGHT);
        constraints.gridx = 1;
        constraints.gridy = 7;
        constraints.gridwidth = 1;
        constraints.weightx = 0.5;
        constraints.weighty = 0.1;
        constraints.anchor = GridBagConstraints.LINE_END;
        formPanel.add(amountLabel, constraints);

        amountTextField = new JTextField("", 20);
        amountTextField.setHorizontalAlignment(JTextField.RIGHT);
        amountTextField.addKeyListener(new QRCodeKeyListener());

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2;
        constraints.gridy = 7;
        constraints.weightx = 0.3;
        constraints.weighty = 0.1;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(amountTextField, constraints);

        JLabel amountUnitLabel = new JLabel(controller.getLocaliser().getString("sendBitcoinPanel.amountUnitLabel"));
        amountUnitLabel.setToolTipText(controller.getLocaliser().getString("sendBitcoinPanel.amountUnitLabel.tooltip"));
        constraints.gridx = 3;
        constraints.gridy = 7;
        constraints.weightx = 0.4;
        constraints.weighty = 0.1;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(amountUnitLabel, constraints);

        SendBitcoinConfirmAction sendBitcoinConfirmAction = new SendBitcoinConfirmAction(controller, this);
        JButton sendButton = new JButton(sendBitcoinConfirmAction);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 4;
        constraints.gridy = 8;
        constraints.weightx = 2.0;
        constraints.weighty = 0.4;
        constraints.anchor = GridBagConstraints.LINE_START;
        formPanel.add(sendButton, constraints);

        return formPanel;
    }

    private JPanel createQRCodePanel() {
        JPanel qrCodePanel = new JPanel();
        qrCodePanel.setMinimumSize(new Dimension(240, 200));
        qrCodePanel.setLayout(new GridBagLayout());
        qrCodeLabel = new JLabel("", null, JLabel.CENTER);
        qrCodeLabel.setVerticalTextPosition(JLabel.BOTTOM);
        qrCodeLabel.setHorizontalTextPosition(JLabel.CENTER);

        // copy image support
        qrCodeLabel.setTransferHandler(new ImageSelection());

        // drag support
        MouseListener listener = new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JComponent comp = (JComponent) me.getSource();
                TransferHandler handler = comp.getTransferHandler();
                handler.exportAsDrag(comp, me, TransferHandler.COPY);
            }
        };
        qrCodeLabel.addMouseListener(listener);

        GridBagConstraints constraints = new GridBagConstraints();

        JPanel filler1 = new JPanel();
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.02;
        constraints.weighty = 0.02;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        qrCodePanel.add(filler1, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        qrCodePanel.add(qrCodeLabel, constraints);

        JPanel filler2 = new JPanel();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 0.02;
        constraints.weighty = 0.02;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        qrCodePanel.add(filler2, constraints);

        qrCodeTextArea = new JTextArea(3, 40);
        qrCodeTextArea.setEditable(false);
        qrCodeTextArea.setLineWrap(true);
        qrCodeTextArea.setMinimumSize(new Dimension(220, 60));
        qrCodeTextArea.setText("Drag bitcoin QRCode to target above to fill\nSend Form.");

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.weightx = 1;
        constraints.weighty = 6;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        qrCodePanel.add(qrCodeTextArea, constraints);

        JPanel filler3 = new JPanel();
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 2;
        constraints.gridy = 5;
        constraints.weightx = 0.05;
        constraints.weighty = 0.02;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.anchor = GridBagConstraints.LINE_END;
        qrCodePanel.add(filler3, constraints);

        return qrCodePanel;
    }

    private JPanel createAddressesPanel() {
        JPanel addressPanel = new JPanel();
        addressPanel.setOpaque(false);

        addressPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

        // get the stored previously selected send address

        addressPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        addressesTableModel = new AddressBookTableModel(controller.getLocaliser(), controller.getModel()
                .getAddressBook(), false);
        addressesTable = new JTable(addressesTableModel);
        addressesTable.setOpaque(false);
        addressesTable.setShowGrid(false);
        addressesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        addressesTable.setRowSelectionAllowed(true);
        addressesTable.setColumnSelectionAllowed(false);
        // TODO make sure table cannot be edited by double click

        TableColumn tableColumn = addressesTable.getColumnModel().getColumn(0); // label
        tableColumn.setPreferredWidth(40);

        tableColumn = addressesTable.getColumnModel().getColumn(1); // address
        tableColumn.setPreferredWidth(120);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        constraints.weighty = 0.1;
        constraints.anchor = GridBagConstraints.LINE_START;
        addressPanel.add(createAddressesHeaderPanel(), constraints);

        JScrollPane scrollPane = new JScrollPane(addressesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.weighty = 1;
        addressPanel.add(scrollPane, constraints);

        // add on a selection listener
        addressesListener = new SelectionListener();
        addressesTable.getSelectionModel().addListSelectionListener(addressesListener);

        return addressPanel;
    }

    private JPanel createAddressesHeaderPanel() {
        JPanel addressesHeaderPanel = new AddressesPanel();
        addressesHeaderPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        CreateNewSendingAddressAction createNewSendingAddressAction = new CreateNewSendingAddressAction(controller, this);
        JButton createNewButton = new JButton(createNewSendingAddressAction);
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0.3;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        addressesHeaderPanel.add(createNewButton, constraints);

        JLabel titleLabel = new JLabel();
        titleLabel.setHorizontalTextPosition(JLabel.CENTER);
        titleLabel.setText(controller.getLocaliser().getString("sendBitcoinPanel.sendingAddressesTitle"));
        Font font = new Font(MultiBitFrame.MULTIBIT_FONT_NAME, MultiBitFrame.MULTIBIT_FONT_STYLE,
                MultiBitFrame.MULTIBIT_LARGE_FONT_SIZE + 2);
        titleLabel.setFont(font);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        addressesHeaderPanel.add(titleLabel, constraints);
        
        return addressesHeaderPanel;
    }

//    private JPanel createQRCodeButtonPanel() {
//        JPanel buttonPanel = new JPanel();
//        FlowLayout flowLayout = new FlowLayout();
//        flowLayout.setAlignment(FlowLayout.LEFT);
//        buttonPanel.setLayout(flowLayout);
//
//        CopyQRCodeTextAction copyQRCodeTextAction = new CopyQRCodeTextAction(controller, this);
//        copyQRCodeTextButton = new JButton(copyQRCodeTextAction);
//        buttonPanel.add(copyQRCodeTextButton);
//
//        CopyQRCodeImageAction copyQRCodeImageAction = new CopyQRCodeImageAction(controller, this);
//        JButton copyQRCodeImageButton = new JButton(copyQRCodeImageAction);
//        buttonPanel.add(copyQRCodeImageButton);
//
//        return buttonPanel;
//    }

    public Data getData() {
        Data data = new Data();
        Item addressItem = new Item(MultiBitModel.SEND_ADDRESS);
        addressItem.setNewValue(addressTextField.getText());
        data.addItem(MultiBitModel.SEND_ADDRESS, addressItem);

        Item labelItem = new Item(MultiBitModel.SEND_LABEL);
        labelItem.setNewValue(labelTextField.getText());
        data.addItem(MultiBitModel.SEND_LABEL, labelItem);

        Item amountItem = new Item(MultiBitModel.SEND_AMOUNT);
        amountItem.setNewValue(amountTextField.getText());
        data.addItem(MultiBitModel.SEND_AMOUNT, amountItem);

//        Item uriTextItem = new Item(MultiBitModel.RECEIVE_URI_TEXT);
//        uriTextItem.setNewValue(qrCodeTextArea.getText());
//        data.addItem(MultiBitModel.RECEIVE_URI_TEXT, uriTextItem);
//
//        Item uriImageItem = new Item(MultiBitModel.RECEIVE_URI_IMAGE);
//        uriImageItem.setNewValue(qrCodeLabel);
//        data.addItem(MultiBitModel.RECEIVE_URI_IMAGE, uriImageItem);

        return data;
    }

    public void loadForm() {
        // get the current address, label and amount from the model
        String address = controller.getModel().getUserPreference(MultiBitModel.SEND_ADDRESS);
        String label = controller.getModel().getUserPreference(MultiBitModel.SEND_LABEL);
        String amount = controller.getModel().getUserPreference(MultiBitModel.SEND_AMOUNT);

        if (address != null) {
            addressTextField.setText(address);
        }
        if (label != null) {
            labelTextField.setText(label);
        }
        if (amount != null) {
            amountTextField.setText(amount);
        }
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MultiBitFrame.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("SEndBitcoinPanel#createImageIcon: Could not find file: " + path);
            return null;
        }
    }

    public String getDescription() {
        return controller.getLocaliser().getString("sendBitcoinPanel.title");
    }

    public void setPossibleActions(Collection<Action> possibleActions) {
        // not used in this viewSystem
    }

    public void displayView() {
        loadForm();
        selectRows();    
    }

    public void navigateAwayFromView(int nextViewId, int relationshipOfNewViewToPrevious) {
    }

    public void displayMessage(String messageKey, Object[] messageData, String titleKey) {
    }

    /**
     * select the rows that correspond to the current data
     */
    public void selectRows() {
        // stop listener firing
        addressesTable.getSelectionModel().removeListSelectionListener(addressesListener);

        String address = controller.getModel().getUserPreference(MultiBitModel.SEND_ADDRESS);
        //displayQRCode(BitcoinURI.convertToBitcoinURI(address, amountTextField.getText(), labelTextField.getText()));

        // see if the current address is on the table and select it
        int rowToSelect = addressesTableModel.findRowByAddress(address, false);
        if (rowToSelect >= 0) {
            addressesTable.getSelectionModel().setSelectionInterval(rowToSelect, rowToSelect);
            selectedAddressRow = rowToSelect;
        }

        // scroll to visible
        addressesTable.scrollRectToVisible(addressesTable.getCellRect(rowToSelect, 0, false));
        // put the listeners back
        addressesTable.getSelectionModel().addListSelectionListener(addressesListener);
    }

    /**
     * display the specified string as a QRCode
     */
//    private void displayQRCode(String stringToDisplay) {
//        if (qrCodeEncoderDecoder == null) {
//            qrCodeEncoderDecoder = new QRCodeEncoderDecoder(QRCODE_WIDTH, QRCODE_HEIGHT);
//        }
//        BufferedImage image = qrCodeEncoderDecoder.encode(stringToDisplay);
//        ImageIcon icon = new ImageIcon(image);
//        qrCodeLabel.setIcon(icon);
//        qrCodeTextArea.setText(stringToDisplay);
//    }

    class SelectionListener implements ListSelectionListener {
        SelectionListener() {
        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() instanceof DefaultListSelectionModel && !e.getValueIsAdjusting()) {
                // Column selection changed
                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();

                if (selectedAddressRow == firstIndex) {
                    selectedAddressRow = lastIndex;
                } else {
                    if (selectedAddressRow == lastIndex) {
                        selectedAddressRow = firstIndex;
                    }
                }
                AddressBookData rowData = addressesTableModel.getAddressBookDataByRow(selectedAddressRow, false);
                if (rowData != null) {
                    controller.getModel().setUserPreference(MultiBitModel.SEND_ADDRESS, rowData.getAddress());
                    controller.getModel().setUserPreference(MultiBitModel.SEND_LABEL, rowData.getLabel());
                    addressTextField.setText(rowData.getAddress());
                    labelTextField.setText(rowData.getLabel());
                    //displayQRCode(BitcoinURI.convertToBitcoinURI(rowData.getAddress(), amountTextField.getText(),
                    //        labelTextField.getText()));
                }
            }
        }
    }

    class QRCodeKeyListener implements KeyListener {
        /** Handle the key typed event from the text field. */
        public void keyTyped(KeyEvent e) {
        }

        /** Handle the key-pressed event from the text field. */
        public void keyPressed(KeyEvent e) {
            // do nothing
        }

        /** Handle the key-released event from the text field. */
        public void keyReleased(KeyEvent e) {
            String address = addressTextField.getText();
            String amount = amountTextField.getText();
            String label = labelTextField.getText();
            AddressBookData addressBookData = new AddressBookData(label, address);
            addressesTableModel.setAddressBookDataByRow(addressBookData, selectedAddressRow, false);
            controller.getModel().setUserPreference(MultiBitModel.SEND_ADDRESS, address);
            controller.getModel().setUserPreference(MultiBitModel.SEND_LABEL, label);
            controller.getModel().setUserPreference(MultiBitModel.SEND_AMOUNT, amount);

            //displayQRCode(BitcoinURI.convertToBitcoinURI(address, amount, label));
        }
    }
    
    public void setAddressBookDataByRow(AddressBookData addressBookData) {
        TextTransfer textTransfer = new TextTransfer();
        String stringToPaste = textTransfer.getClipboardContents();
        
        addressTextField.setText(addressBookData.getAddress());
        addressesTableModel.setAddressBookDataByRow(addressBookData, selectedAddressRow, false);
    }

    public JTextField getLabelTextField() {
        return labelTextField;
    }

    public JPanel getFormPanel() {
        return formPanel;
    }
}
