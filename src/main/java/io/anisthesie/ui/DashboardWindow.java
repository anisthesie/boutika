package io.anisthesie.ui;

import io.anisthesie.db.dao.ProduitDAO;
import io.anisthesie.db.dao.VenteDAO;
import io.anisthesie.db.dao.VenteProduitsDAO;
import io.anisthesie.db.service.VenteService;
import io.anisthesie.ui.panels.HistoriqueVentesPanel;
import io.anisthesie.ui.panels.TransactionPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardWindow extends JFrame {

    private final ProduitDAO produitDAO;
    private final VenteDAO venteDAO;
    private final VenteProduitsDAO venteProduitsDAO;

    private final VenteService venteService;

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final JLabel dateLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();

    private Rectangle windowedBounds;

    private int transactionCounter = 1;
    private boolean isFullscreen = false;


    public DashboardWindow(Connection conn) throws SQLException {
        this.produitDAO = new ProduitDAO(conn);
        this.venteDAO = new VenteDAO(conn);
        this.venteProduitsDAO = new VenteProduitsDAO(conn);

        this.venteService = new VenteService(conn);

        setTitle("Boutika - Gestion Commerciale");
        setSize(1000, 562);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        initUI();
        startClockUpdater();
    }

    private void initUI() {
        JPanel buttonPanel = initLeftPanel();

        
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonPanel, tabbedPane);


        horizontalSplit.setResizeWeight(0);
        horizontalSplit.setDividerSize(4);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(horizontalSplit, BorderLayout.CENTER);
    }

    private JPanel initLeftPanel() {
        
        JButton btnNew = createButton("Nouvelle Vente", new Color(46, 204, 113), 32, 240,e -> openNewTransactionTab());
        JButton btnHistorique = createButton("Historique des ventes", new Color(52, 152, 219), 20, 60,e -> openHistoriqueTab());
        JButton btnPrint = createButton("Imprimer Journée", new Color(241, 196, 15), 20, 60,e -> JOptionPane.showMessageDialog(this, "TODO: Imprimer journée"));
        JButton btnStock = createButton("Ajouter Stock", new Color(155, 89, 182), 20, 60,e -> JOptionPane.showMessageDialog(this, "TODO: Exporter recettes"));

        
        JPanel clockPanel = initClockPanel();

        
        JButton fullscreenBtn = initFullscreenBtn();


        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        leftPanel.add(btnNew);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(btnHistorique);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(btnPrint);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(btnStock);
        leftPanel.add(Box.createVerticalStrut(30));
        leftPanel.add(clockPanel);
        leftPanel.add(Box.createVerticalStrut(30));
        leftPanel.add(fullscreenBtn, BorderLayout.SOUTH);
        return leftPanel;
    }


    private JButton initFullscreenBtn() {
        JButton fullscreenBtn = new JButton("🗖 Plein écran");
        fullscreenBtn.setFocusPainted(false);
        fullscreenBtn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        fullscreenBtn.setBackground(new Color(44, 62, 80));
        fullscreenBtn.setForeground(Color.WHITE);
        fullscreenBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        fullscreenBtn.addActionListener(e -> toggleFullscreen(fullscreenBtn));
        toggleFullscreen(fullscreenBtn);
        return fullscreenBtn;
    }

    private JPanel initClockPanel() {
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        JPanel clockPanel = new JPanel();
        clockPanel.setLayout(new BoxLayout(clockPanel, BoxLayout.Y_AXIS));
        clockPanel.setOpaque(false);
        clockPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        clockPanel.add(dateLabel);
        clockPanel.add(timeLabel);
        return clockPanel;
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

    private JButton createButton(String text, Color color, int fontSize, int height, ActionListener actionListener) {
        JButton btn = createButton(text, color, fontSize, height);
        btn.addActionListener(actionListener);
        return btn;
    }
    public void openHistoriqueTab() {
        
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component tab = tabbedPane.getComponentAt(i);
            if (tab instanceof HistoriqueVentesPanel) {
                tabbedPane.removeTabAt(i);
                break;
            }
        }
        
        HistoriqueVentesPanel panel = new HistoriqueVentesPanel(venteDAO, venteProduitsDAO);
        tabbedPane.addTab("Historique des ventes", panel);
        tabbedPane.setSelectedComponent(panel);
    }


    private void openNewTransactionTab() {
        String tabName = "Client " + transactionCounter++;
        JPanel panel = new TransactionPanel(this.produitDAO, this.venteService, this);
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
            if (btn != null)
                btn.setText("🗗 Quitter plein écran");
        } else {
            dispose();
            setUndecorated(false);
            setBounds(windowedBounds != null ? windowedBounds : new Rectangle(100, 100, 1200, 800));
            setVisible(true);
            if (btn != null)
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

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
}
