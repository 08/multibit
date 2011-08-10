package org.multibit.viewsystem.swing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;

import org.multibit.viewsystem.swing.MultiBitFrame;

public class Browser extends JFrame {

    private static final long serialVersionUID = 4921443778446348403L;
    private JTextField statusLabel;
    private JEditorPane contents;
    private String currentUrl;

    private JFrame mainFrame;

    public Browser(JFrame mainFrame, String openingUrl) {
        super("MultiBit Help Contents");
        this.mainFrame = mainFrame;
        this.currentUrl = openingUrl;

        Container contentPane = getContentPane();

        // text field is used so that user can copy the url
        statusLabel = new JTextField(MultiBitFrame.SPACER + openingUrl);
        statusLabel.setEditable(false);
        statusLabel.setBorder(null);
        statusLabel.setDisabledTextColor(Color.black);
        statusLabel.setBackground(Color.LIGHT_GRAY);

        contentPane.add(statusLabel, BorderLayout.SOUTH);

        contents = new JEditorPane();

        contents.setEditable(false);

        contents.addHyperlinkListener(new ActivatedHyperlinkListener(mainFrame, contents));
        
        JScrollPane scrollPane = new JScrollPane(contents);
        scrollPane.setPreferredSize(new Dimension(800,400));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        positionDialogRelativeToParent(this, 0.1D, 0.1D);
        //setMinimumSize(new Dimension(800, 400));

        pack();
        setVisible(true);
        getThePage(openingUrl);
    }

    private void getThePage(String location) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            contents.setPage(location);
            statusLabel.setText(location);
        } catch (IOException io) {
            JOptionPane.showMessageDialog(this, "Error retrieving URL '" + location + "'.  \nCheck your network connection.", "Could not load URL", JOptionPane.ERROR_MESSAGE);
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Positions the specified dialog at a position relative to its parent.
     * 
     * @param frame
     *            the dialog to be positioned.
     * @param horizontalPercent
     *            the relative location.
     * @param verticalPercent
     *            the relative location.
     */
    protected void positionDialogRelativeToParent(final JFrame frame, final double horizontalPercent,
            final double verticalPercent) {
        final Dimension d = frame.getSize();
        final Dimension p = mainFrame.getSize();

        final int baseX = mainFrame.getX() - d.width;
        final int baseY = mainFrame.getY() - d.height;
        final int w = d.width + p.width;
        final int h = d.height + p.height;
        int x = baseX + (int) (horizontalPercent * w);
        int y = baseY + (int) (verticalPercent * h);

        // make sure the dialog fits completely on the screen...
        final Rectangle s = getMaximumWindowBounds();
        x = Math.min(x, (s.width - d.width));
        x = Math.max(x, 0);
        y = Math.min(y, (s.height - d.height));
        y = Math.max(y, 0);

        frame.setBounds(x + s.x, y + s.y, d.width, d.height);

    }

    /**
     * Computes the maximum bounds of the current screen device. If this method
     * is called on JDK 1.4, Xinerama-aware results are returned. (See
     * Sun-Bug-ID 4463949 for details).
     * 
     * @return the maximum bounds of the current screen.
     */
    protected Rectangle getMaximumWindowBounds() {
        final GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            final Method method = GraphicsEnvironment.class.getMethod("getMaximumWindowBounds", (Class[]) null);
            return (Rectangle) method.invoke(localGraphicsEnvironment, (Object[]) null);
        } catch (Exception e) {
            // ignore ... will fail if this is not a JDK 1.4 ..
        }

        final Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
        return new Rectangle(0, 0, s.width, s.height);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MultiBitFrame.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Browser#createImageIcon: Could not find file: " + path);
            return null;
        }
    }

    class ActivatedHyperlinkListener implements HyperlinkListener {

        private static final String MULTIBIT_HOST_NAME = "www.multibit.org";

        private static final String HTTP_PROTOCOL = "http";

        JFrame frame;

        JEditorPane editorPane;

        public ActivatedHyperlinkListener(JFrame frame, JEditorPane editorPane) {
            this.frame = frame;
            this.editorPane = editorPane;
        }

        public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
            HyperlinkEvent.EventType type = hyperlinkEvent.getEventType();
            final URL url = hyperlinkEvent.getURL();
            if (type == HyperlinkEvent.EventType.ENTERED) {
                statusLabel.setText(MultiBitFrame.SPACER + url.toString());
            } else if (type == HyperlinkEvent.EventType.EXITED) {
                statusLabel.setText(MultiBitFrame.SPACER + currentUrl);
            } else if (type == HyperlinkEvent.EventType.ACTIVATED) {
                Runnable runner = new Runnable() {
                    public void run() {
                        // Retain reference to original
                        Document doc = editorPane.getDocument();
                        try {
                            if (HTTP_PROTOCOL.equals(url.getProtocol()) && MULTIBIT_HOST_NAME.equals(url.getHost())) {
                                editorPane.setPage(url);
                                currentUrl = url.toString();
                            } else {
                                JOptionPane.showMessageDialog(frame, "The help contents can only show HTTP content from " + MULTIBIT_HOST_NAME + "\nPlease use your main browser to view the URL:\n" + url.toString(), "Cannot follow link",
                                        JOptionPane.INFORMATION_MESSAGE);
                                
                            }
                        } catch (IOException ioException) {
                            JOptionPane.showMessageDialog(frame, "Error following link", "Invalid link",
                                    JOptionPane.ERROR_MESSAGE);
                            editorPane.setDocument(doc);
                        }
                    }
                };
                SwingUtilities.invokeLater(runner);
            }
        }
    }
}