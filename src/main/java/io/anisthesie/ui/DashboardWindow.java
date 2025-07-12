package io.anisthesie.ui;

import io.anisthesie.db.ProduitDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardWindow extends JFrame {

    private final ProduitDAO produitDAO;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private int transactionCounter = 1;

    private boolean isFullscreen = false;
    private Rectangle windowedBounds;

    private final JLabel dateLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();

    public DashboardWindow(Connection conn) throws SQLException {
        this.produitDAO = new ProduitDAO(conn);
        setTitle("OptiGest - Gestion Commerciale");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        initUI();
        startClockUpdater();
    }

    private void initUI() {
        // === Gros boutons ===
        JButton btnNew = createButton("Nouvelle Vente", new Color(46, 204, 113), 32, 240);
        JButton btnStock = createButton("Historique des ventes", new Color(52, 152, 219), 20, 60);
        JButton btnPrint = createButton("Imprimer Journée", new Color(241, 196, 15), 20, 60);
        JButton btnExport = createButton("Ajouter Stock", new Color(155, 89, 182), 20, 60);

        btnNew.addActionListener(e -> openNewTransactionTab());
        btnStock.addActionListener(e -> JOptionPane.showMessageDialog(this, "TODO: Ajouter stock"));
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "TODO: Imprimer journée"));
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this, "TODO: Exporter recettes"));

        // === Horloge ===
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        JPanel clockPanel = new JPanel();
        clockPanel.setLayout(new BoxLayout(clockPanel, BoxLayout.Y_AXIS));
        clockPanel.setOpaque(false);
        clockPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        clockPanel.add(dateLabel);
        clockPanel.add(timeLabel);

        // === Bouton plein écran ===
        JButton fullscreenBtn = new JButton("🗖 Plein écran");
        fullscreenBtn.setFocusPainted(false);
        fullscreenBtn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        fullscreenBtn.setBackground(new Color(44, 62, 80));
        fullscreenBtn.setForeground(Color.WHITE);
        fullscreenBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        fullscreenBtn.addActionListener(e -> toggleFullscreen(fullscreenBtn));

        // === Panel gauche avec tous les boutons + horloge + plein écran ===
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        buttonPanel.add(btnNew);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(btnStock);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnPrint);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnExport);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(clockPanel);
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(fullscreenBtn, BorderLayout.SOUTH);

        // === Onglets à droite ===
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonPanel, tabbedPane);
        horizontalSplit.setResizeWeight(0);
        horizontalSplit.setDividerSize(4);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(horizontalSplit, BorderLayout.CENTER);
    }

    private JButton createButton(String text, Color color, int fontSize, int height) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(0, height));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return btn;
    }

    private void openNewTransactionTab() {
        String tabName = "Client " + transactionCounter++;
        JPanel panel = new TransactionPanel(this.produitDAO);
        tabbedPane.addTab(tabName, panel);
        tabbedPane.setSelectedComponent(panel);
    }

    private void toggleFullscreen(JButton btn) {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if (!isFullscreen) {
            windowedBounds = getBounds();
            dispose();
            setUndecorated(true);
            setVisible(true);
            setBounds(gd.getDefaultConfiguration().getBounds());
            btn.setText("🗗 Quitter plein écran");
        } else {
            dispose();
            setUndecorated(false);
            setBounds(windowedBounds != null ? windowedBounds : new Rectangle(100, 100, 1200, 800));
            setVisible(true);
            btn.setText("🗖 Plein écran");
        }

        isFullscreen = !isFullscreen;
    }

    private void startClockUpdater() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
                    timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                });
            }
        }, 0, 1000);
    }
}
