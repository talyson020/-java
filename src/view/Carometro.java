package view;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.undo.UndoManager;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import model.DAO;
import ultils.Validador;

public class Carometro extends JFrame {

    private static final long serialVersionUID = 1L;
    DAO dao = new DAO();
    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;

    private byte[] fotoByte = null;

    private JPanel contentPane;
    private JLabel lblStatus;
    private JLabel lblData;
    private JTextField txtRA;
    private JTextField txtNome;
    private JLabel lblFoto;
    private JButton btnCarregar;
    private JButton btnReset;
    private JButton btnBuscar;
    private JScrollPane scrollPaneLista;
    private JList<String> listNomes;
    private JButton btnAdicionar;
    private JButton btnEditar;
    private JButton btnExcluir;
    private JButton btnPdf;

    private JTextField txtTelefone;
    private JTextField txtEmail;
    private JTextArea txtAlergias;
    private JTextArea txtObservacoes;

    // -------------------------------------------------------
    // Ajuste aqui as credenciais do seu banco de dados MySQL
    // -------------------------------------------------------
    private static final String DB_USUARIO = "talyson020";
    private static final String DB_SENHA   = "Plokij@789";
    private static final String DB_BANCO   = "dbcarometro";

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Carometro frame = new Carometro();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Carometro() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                status();
                setarData();
            }
        });

        setTitle("Carômetro Escolar - Gestão Educacional");
        setIconImage(Toolkit.getDefaultToolkit().getImage(Carometro.class.getResource("/img/logo.png")));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 960, 560);

        contentPane = new JPanel();
        contentPane.setBackground(SystemColor.activeCaption);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // ── Painel de status (rodapé) ──────────────────────────────────────────
        JPanel panelStatus = new JPanel();
        panelStatus.setBackground(new Color(30, 144, 255));
        panelStatus.setBounds(0, 440, 944, 81);
        contentPane.add(panelStatus);
        panelStatus.setLayout(null);

        lblStatus = new JLabel("");
        lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dboff.png")));
        lblStatus.setBounds(900, 24, 32, 32);
        panelStatus.add(lblStatus);

        lblData = new JLabel("");
        lblData.setForeground(SystemColor.text);
        lblData.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblData.setBounds(437, 26, 400, 30);
        panelStatus.add(lblData);
        
                // ── Botão Backup ───────────────────────────────────────────────────────
                JButton btnBackup = new JButton("Backup");
                btnBackup.setBounds(10, 11, 130, 64);
                panelStatus.add(btnBackup);
                btnBackup.setIcon(carregarIcone("/img/backup_out.png"));
                btnBackup.setFont(new Font("Tahoma", Font.BOLD, 10));
                btnBackup.setToolTipText("Exportar backup do banco de dados");
                
                        // ── Botão Restaurar ────────────────────────────────────────────────────
                        JButton btnRestaurar = new JButton("Restaurar");
                        btnRestaurar.setBounds(174, 11, 130, 64);
                        panelStatus.add(btnRestaurar);
                        btnRestaurar.setIcon(carregarIcone("/img/backup_in.png"));
                        btnRestaurar.setFont(new Font("Tahoma", Font.BOLD, 10));
                        btnRestaurar.setToolTipText("Restaurar banco de dados a partir de backup");
                        btnRestaurar.addActionListener(e -> restaurarBackup());
                btnBackup.addActionListener(e -> fazerBackup());

        // ── Campos de busca ────────────────────────────────────────────────────
        JLabel lblRA = new JLabel("RA");
        lblRA.setBounds(10, 29, 24, 14);
        contentPane.add(lblRA);

        txtRA = new JTextField();
        txtRA.setBounds(58, 26, 86, 20);
        txtRA.setDocument(new Validador(7));
        contentPane.add(txtRA);

        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarRA());
        btnBuscar.setBounds(154, 25, 79, 23);
        contentPane.add(btnBuscar);

        JLabel lblNome = new JLabel("Nome");
        lblNome.setBounds(10, 60, 47, 14);
        contentPane.add(lblNome);

        txtNome = new JTextField();
        txtNome.setDocument(new Validador(30));
        txtNome.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                listarNomes();
            }
        });
        txtNome.setBounds(58, 57, 222, 20);
        contentPane.add(txtNome);

        scrollPaneLista = new JScrollPane();
        scrollPaneLista.setBorder(null);
        scrollPaneLista.setVisible(false);
        scrollPaneLista.setBounds(58, 75, 222, 91);
        contentPane.add(scrollPaneLista);

        listNomes = new JList<>();
        scrollPaneLista.setViewportView(listNomes);
        listNomes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                buscarNome();
            }
        });

        // ── Telefone / Email ───────────────────────────────────────────────────
        JLabel lblTelefone = new JLabel("Tel. Pais");
        lblTelefone.setBounds(300, 60, 60, 14);
        contentPane.add(lblTelefone);

        txtTelefone = new JTextField();
        txtTelefone.setDocument(new TelefoneMask());
        txtTelefone.setBounds(360, 56, 150, 22);
        contentPane.add(txtTelefone);

        JLabel lblEmail = new JLabel("Email");
        lblEmail.setBounds(10, 100, 47, 14);
        contentPane.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(58, 100, 222, 22);
        contentPane.add(txtEmail);

        // ── Painel Alergias ────────────────────────────────────────────────────
        JPanel painelAlergias = new JPanel();
        painelAlergias.setLayout(null);
        painelAlergias.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED),
                "Alergias", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Tahoma", Font.BOLD, 11), Color.RED.darker()));
        painelAlergias.setBackground(new Color(255, 240, 240));
        painelAlergias.setBounds(10, 165, 280, 75);
        contentPane.add(painelAlergias);

        txtAlergias = new JTextArea();
        txtAlergias.setLineWrap(true);
        txtAlergias.setBackground(new Color(255, 240, 240));
        txtAlergias.setBounds(5, 18, 268, 50);
        painelAlergias.add(txtAlergias);

        // ── Painel Obs. Pedagógicas ────────────────────────────────────────────
        JPanel painelObs = new JPanel();
        painelObs.setLayout(null);
        painelObs.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED),
                "Obs. Pedagógicas", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Tahoma", Font.BOLD, 11), new Color(0, 100, 0)));
        painelObs.setBackground(new Color(240, 255, 240));
        painelObs.setBounds(10, 248, 280, 75);
        contentPane.add(painelObs);

        txtObservacoes = new JTextArea();
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setBackground(new Color(240, 255, 240));
        txtObservacoes.setBounds(5, 18, 268, 50);
        painelObs.add(txtObservacoes);

        // ── Foto ───────────────────────────────────────────────────────────────
        lblFoto = new JLabel("");
        lblFoto.setBorder(new BevelBorder(BevelBorder.LOWERED));
        lblFoto.setIcon(new ImageIcon(Carometro.class.getResource("/img/camera.png")));
        lblFoto.setBounds(663, 11, 256, 256);
        contentPane.add(lblFoto);

        btnCarregar = new JButton("Foto");
        btnCarregar.setEnabled(false);
        btnCarregar.addActionListener(e -> carregarFoto());
        btnCarregar.setBounds(730, 275, 117, 23);
        contentPane.add(btnCarregar);

        // ── Botões CRUD ────────────────────────────────────────────────────────
        btnAdicionar = new JButton("");
        btnAdicionar.setEnabled(false);
        btnAdicionar.addActionListener(e -> adicionar());
        btnAdicionar.setIcon(new ImageIcon(Carometro.class.getResource("/img/create.png")));
        btnAdicionar.setBounds(10, 355, 64, 64);
        contentPane.add(btnAdicionar);

        btnEditar = new JButton("");
        btnEditar.setEnabled(false);
        btnEditar.addActionListener(e -> editar());
        btnEditar.setIcon(new ImageIcon(Carometro.class.getResource("/img/update.png")));
        btnEditar.setBounds(100, 355, 64, 64);
        contentPane.add(btnEditar);

        btnExcluir = new JButton("");
        btnExcluir.setEnabled(false);
        btnExcluir.addActionListener(e -> excluir());
        btnExcluir.setIcon(new ImageIcon(Carometro.class.getResource("/img/delete.png")));
        btnExcluir.setBounds(190, 355, 64, 64);
        contentPane.add(btnExcluir);

        btnReset = new JButton("");
        btnReset.addActionListener(e -> reset());
        btnReset.setIcon(new ImageIcon(Carometro.class.getResource("/img/eraser.png")));
        btnReset.setBounds(280, 355, 64, 64);
        contentPane.add(btnReset);

        // ── Botão PDF ──────────────────────────────────────────────────────────
        btnPdf = new JButton("");
        btnPdf.addActionListener(e -> gerarPdf());
        btnPdf.setIcon(new ImageIcon(Carometro.class.getResource("/img/pdf.png")));
        btnPdf.setToolTipText("Gerar relatório PDF");
        btnPdf.setBounds(589, 203, 64, 64);
        contentPane.add(btnPdf);

        // ── Botão Sobre ────────────────────────────────────────────────────────
        JButton btnSobre = new JButton("");
        btnSobre.setIcon(carregarIcone("/img/info.png"));
        btnSobre.setFont(new Font("Tahoma", Font.BOLD, 10));
        btnSobre.setToolTipText("Informações sobre o sistema");
        btnSobre.addActionListener(e -> mostrarSobre());
        btnSobre.setBounds(625, 11, 32, 32);
        contentPane.add(btnSobre);

        // ── Undo nos campos de texto ───────────────────────────────────────────
        adicionarUndo(txtNome);
        adicionarUndo(txtAlergias);
        adicionarUndo(txtObservacoes);
    }

    // ==========================================================================
    // STATUS / DATA
    // ==========================================================================

    private void status() {
        try {
            con = dao.conectar();
            if (con == null) {
                lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dboff.png")));
            } else {
                lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dbon.png")));
                con.close();
            }
        } catch (Exception e) { System.out.println(e); }
    }

    private void setarData() {
        lblData.setText(DateFormat.getDateInstance(DateFormat.FULL).format(new Date()));
    }

    // ==========================================================================
    // FOTO
    // ==========================================================================

    private void carregarFoto() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new FileNameExtensionFilter("Imagens", "png", "jpg", "jpeg"));
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File arquivo = jfc.getSelectedFile();
                fotoByte = new FileInputStream(arquivo).readAllBytes();
                Image foto = ImageIO.read(arquivo).getScaledInstance(
                        lblFoto.getWidth(), lblFoto.getHeight(), Image.SCALE_SMOOTH);
                lblFoto.setIcon(new ImageIcon(foto));
            } catch (Exception e) { System.out.println(e); }
        }
    }

    // ==========================================================================
    // CRUD
    // ==========================================================================

    private void adicionar() {
        if (txtNome.getText().isEmpty() || fotoByte == null) {
            JOptionPane.showMessageDialog(null, "Preencha Nome e Foto!"); return;
        }
        String sql = "INSERT INTO alunos(nome, foto, alergias, observacoes, telefone, email) VALUES(?,?,?,?,?,?)";
        try (Connection c = dao.conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, txtNome.getText());
            p.setBytes(2, fotoByte);
            p.setString(3, txtAlergias.getText());
            p.setString(4, txtObservacoes.getText());
            p.setString(5, txtTelefone.getText());
            p.setString(6, txtEmail.getText());
            if (p.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(null, "Cadastrado!"); reset();
            }
        } catch (Exception e) { System.out.println(e); }
    }

    private void buscarRA() {
        try (Connection c = dao.conectar();
             PreparedStatement p = c.prepareStatement("SELECT * FROM alunos WHERE ra = ?")) {
            p.setString(1, txtRA.getText());
            rs = p.executeQuery();
            if (rs.next()) preencherCampos();
            else {
                JOptionPane.showMessageDialog(null, "RA inexistente");
                btnAdicionar.setEnabled(true);
                btnCarregar.setEnabled(true);
            }
        } catch (Exception e) { System.out.println(e); }
    }

    private void preencherCampos() throws Exception {
        txtNome.setText(rs.getString("nome"));
        Blob b = rs.getBlob("foto");
        byte[] img = b.getBytes(1, (int) b.length());
        lblFoto.setIcon(new ImageIcon(ImageIO.read(new ByteArrayInputStream(img))
                .getScaledInstance(lblFoto.getWidth(), lblFoto.getHeight(), Image.SCALE_SMOOTH)));
        txtAlergias.setText(rs.getString("alergias"));
        txtObservacoes.setText(rs.getString("observacoes"));
        txtTelefone.setText(rs.getString("telefone"));
        txtEmail.setText(rs.getString("email"));
        txtRA.setEditable(false);
        btnEditar.setEnabled(true);
        btnExcluir.setEnabled(true);
        btnCarregar.setEnabled(true);
    }

    private void listarNomes() {
        DefaultListModel<String> modelo = new DefaultListModel<>();
        listNomes.setModel(modelo);
        if (txtNome.getText().isEmpty()) { scrollPaneLista.setVisible(false); return; }
        try (Connection c = dao.conectar();
             PreparedStatement p = c.prepareStatement("SELECT nome FROM alunos WHERE nome LIKE ?")) {
            p.setString(1, txtNome.getText() + "%");
            rs = p.executeQuery();
            while (rs.next()) modelo.addElement(rs.getString(1));
            scrollPaneLista.setVisible(!modelo.isEmpty());
        } catch (Exception e) { System.out.println(e); }
    }

    private void buscarNome() {
        try (Connection c = dao.conectar();
             PreparedStatement p = c.prepareStatement("SELECT * FROM alunos WHERE nome = ?")) {
            p.setString(1, listNomes.getSelectedValue());
            rs = p.executeQuery();
            if (rs.next()) {
                scrollPaneLista.setVisible(false);
                txtRA.setText(rs.getString("ra"));
                preencherCampos();
            }
        } catch (Exception e) { System.out.println(e); }
    }

    private void editar() {
        String sql = fotoByte != null
                ? "UPDATE alunos SET nome=?, foto=?, alergias=?, observacoes=?, telefone=?, email=? WHERE ra=?"
                : "UPDATE alunos SET nome=?, alergias=?, observacoes=?, telefone=?, email=? WHERE ra=?";
        try (Connection c = dao.conectar(); PreparedStatement p = c.prepareStatement(sql)) {
            if (fotoByte != null) {
                p.setString(1, txtNome.getText()); p.setBytes(2, fotoByte);
                p.setString(3, txtAlergias.getText()); p.setString(4, txtObservacoes.getText());
                p.setString(5, txtTelefone.getText()); p.setString(6, txtEmail.getText());
                p.setString(7, txtRA.getText());
            } else {
                p.setString(1, txtNome.getText()); p.setString(2, txtAlergias.getText());
                p.setString(3, txtObservacoes.getText()); p.setString(4, txtTelefone.getText());
                p.setString(5, txtEmail.getText()); p.setString(6, txtRA.getText());
            }
            p.executeUpdate();
            JOptionPane.showMessageDialog(null, "Alterado!"); reset();
        } catch (Exception e) { System.out.println(e); }
    }

    private void excluir() {
        if (JOptionPane.showConfirmDialog(null, "Excluir?", "Aviso",
                JOptionPane.YES_NO_OPTION) == 0) {
            try (Connection c = dao.conectar();
                 PreparedStatement p = c.prepareStatement("DELETE FROM alunos WHERE ra=?")) {
                p.setString(1, txtRA.getText());
                p.executeUpdate(); reset();
            } catch (Exception e) { System.out.println(e); }
        }
    }

    // ==========================================================================
    // GERAR PDF
    // ==========================================================================

    private void gerarPdf() {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Salvar Relatório PDF");
        jfc.setFileFilter(new FileNameExtensionFilter("Arquivos PDF (*.pdf)", "pdf"));
        jfc.setSelectedFile(new File("Relatorio_Alunos.pdf"));

        if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String caminho = jfc.getSelectedFile().getAbsolutePath();
            if (!caminho.toLowerCase().endsWith(".pdf")) caminho += ".pdf";

            Document document = new Document(PageSize.A4.rotate());
            try {
                PdfWriter.getInstance(document, new FileOutputStream(caminho));
                document.open();

                com.itextpdf.text.Font fTit    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                com.itextpdf.text.Font fCab    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
                com.itextpdf.text.Font fNormal  = FontFactory.getFont(FontFactory.HELVETICA, 10);
                com.itextpdf.text.Font fContato = FontFactory.getFont(FontFactory.HELVETICA, 9);

                Paragraph tit = new Paragraph("Relatório de Alunos - Ficha de Emergência", fTit);
                tit.setAlignment(Element.ALIGN_CENTER);
                document.add(tit);
                document.add(new Paragraph(" "));

                PdfPTable tab = new PdfPTable(6);
                tab.setWidthPercentage(100);
                tab.setWidths(new float[]{4, 13, 15, 30, 24, 14});

                for (String h : new String[]{"RA", "Nome", "Alergias", "Obs. Pedagógicas", "Contato", "Foto"}) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, fCab));
                    cell.setBackgroundColor(new BaseColor(30, 144, 255));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(6);
                    tab.addCell(cell);
                }

                try (Connection c = dao.conectar();
                     ResultSet res = c.prepareStatement("SELECT * FROM alunos").executeQuery()) {
                    while (res.next()) {
                        tab.addCell(new PdfPCell(new Phrase(res.getString("ra"), fNormal)));
                        tab.addCell(new PdfPCell(new Phrase(res.getString("nome"), fNormal)));

                        String al = res.getString("alergias");
                        PdfPCell cA = new PdfPCell(new Phrase(al, fNormal));
                        if (al != null && !al.isBlank() && !al.equalsIgnoreCase("Nenhuma"))
                            cA.setBackgroundColor(new BaseColor(255, 210, 210));
                        tab.addCell(cA);

                        String obs = res.getString("observacoes");
                        PdfPCell cObs = new PdfPCell(new Phrase(
                                obs == null || obs.isBlank() ? "-" : obs, fNormal));
                        cObs.setPadding(6);
                        tab.addCell(cObs);

                        String cont = "Tel: " + res.getString("telefone") + "\nEmail: " + res.getString("email");
                        tab.addCell(new PdfPCell(new Phrase(cont, fContato)));

                        Blob bl = res.getBlob("foto");
                        byte[] bytes = bl.getBytes(1, (int) bl.length());
                        com.itextpdf.text.Image im = com.itextpdf.text.Image.getInstance(bytes);
                        im.scaleToFit(85, 85);
                        PdfPCell cFoto = new PdfPCell(im, false);
                        cFoto.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cFoto.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cFoto.setPadding(4);
                        tab.addCell(cFoto);
                    }
                }
                document.add(tab);
                JOptionPane.showMessageDialog(null, "PDF gerado com sucesso!");
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                document.close();
            }
            try { Desktop.getDesktop().open(new File(caminho)); } catch (Exception e) { System.out.println(e); }
        }
    }

    // ==========================================================================
    // HELPER — localiza o executável MySQL automaticamente no Windows
    // ==========================================================================

    /**
     * Procura mysqldump.exe (ou mysql.exe) nas pastas padrão do MySQL no Windows.
     * Se não encontrar em nenhuma delas, retorna só o nome simples e torce
     * para estar no PATH.
     */
    private String localizarMysql(String executavel) {
        // Pastas‑raiz onde o MySQL costuma ser instalado no Windows
        String[] raizes = {
            System.getenv("ProgramFiles"),
            System.getenv("ProgramFiles(x86)"),
            "C:\\xampp\\mysql",
            "C:\\wamp64\\bin\\mysql",
            "C:\\wamp\\bin\\mysql"
        };

        for (String raiz : raizes) {
            if (raiz == null || raiz.isEmpty()) continue;

            File dirRaiz = new File(raiz);

            // Caso XAMPP / WAMP — bin já é o diretório direto
            File direto = new File(dirRaiz, "bin\\" + executavel + ".exe");
            if (direto.exists()) return direto.getAbsolutePath();

            // Caso instalação padrão: C:\Program Files\MySQL\MySQL Server X.Y\bin
            File mysqlDir = new File(dirRaiz, "MySQL");
            if (mysqlDir.exists() && mysqlDir.isDirectory()) {
                File[] versoes = mysqlDir.listFiles(File::isDirectory);
                if (versoes != null) {
                    for (File versao : versoes) {
                        File exe = new File(versao, "bin\\" + executavel + ".exe");
                        if (exe.exists()) return exe.getAbsolutePath();
                    }
                }
            }
        }
        // Fallback: espera estar no PATH
        return executavel;
    }

    // ==========================================================================
    // BACKUP
    // ==========================================================================

    private void fazerBackup() {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Salvar Backup do Banco de Dados");
        jfc.setFileFilter(new FileNameExtensionFilter("Arquivo SQL (*.sql)", "sql"));
        jfc.setSelectedFile(new File("backup_carometro_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".sql"));

        if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String caminho = jfc.getSelectedFile().getAbsolutePath();
        if (!caminho.toLowerCase().endsWith(".sql")) caminho += ".sql";

        String dump = localizarMysql("mysqldump");

        try {
            // --password= evita o warning de senha na linha de comando
            // stdout vai para o arquivo, stderr fica separado para capturar erros
            ProcessBuilder pb = new ProcessBuilder(
                dump,
                "--user=" + DB_USUARIO,
                "--password=" + DB_SENHA,
                "--result-file=" + caminho,  // grava direto no arquivo, sem redirecionar stdout
                DB_BANCO
            );
            pb.redirectErrorStream(false);
            Process proc = pb.start();

            // Descarta stdout (ja vai para o arquivo via --result-file)
            proc.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());

            String erros = new String(proc.getErrorStream().readAllBytes()).trim();
            int exit = proc.waitFor();

            // mysqldump retorna 0 mesmo com warnings; so falha se o arquivo ficou vazio
            File arqSql = new File(caminho);
            boolean vazio = !arqSql.exists() || arqSql.length() == 0;

            if (exit == 0 && !vazio) {
                JOptionPane.showMessageDialog(this,
                        "Backup salvo com sucesso em:\n" + caminho,
                        "Backup Concluido", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Erro ao gerar backup!\n\n" + (erros.isEmpty() ? "Arquivo vazio ou credenciais invalidas." : erros),
                        "Erro no Backup", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ==========================================================================
    // RESTAURAR
    // ==========================================================================

    private void restaurarBackup() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "ATENCAO!\n\nEsta operacao ira SUBSTITUIR todos os dados atuais\n"
                + "pelos dados do arquivo de backup selecionado.\n\nDeseja continuar?",
                "Restaurar Backup", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Selecionar arquivo de Backup (.sql)");
        jfc.setFileFilter(new FileNameExtensionFilter("Arquivo SQL (*.sql)", "sql"));

        if (jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File arquivoSql = jfc.getSelectedFile();
        String mysql = localizarMysql("mysql");

        try {
            ProcessBuilder pb = new ProcessBuilder(
                mysql,
                "--user=" + DB_USUARIO,
                "--password=" + DB_SENHA,
                DB_BANCO
            );
            pb.redirectInput(ProcessBuilder.Redirect.from(arquivoSql));
            pb.redirectErrorStream(false);
            Process proc = pb.start();

            // Descarta stdout do mysql
            proc.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());

            String erros = new String(proc.getErrorStream().readAllBytes()).trim();
            int exit = proc.waitFor();

            // Filtra warnings comuns que nao sao erros reais
            boolean temErroReal = erros.contains("ERROR") || exit != 0;

            if (!temErroReal) {
                JOptionPane.showMessageDialog(this,
                        "Banco de dados restaurado com sucesso!",
                        "Restauracao Concluida", JOptionPane.INFORMATION_MESSAGE);
                reset();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Erro ao restaurar o banco de dados!\n\n" + erros,
                        "Erro na Restauracao", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ==========================================================================
    // HELPER — carrega ícone sem NullPointerException
    // ==========================================================================

    private ImageIcon carregarIcone(String caminho) {
        return carregarIcone(caminho, 40, 40);
    }

    private ImageIcon carregarIcone(String caminho, int largura, int altura) {
        java.net.URL url = Carometro.class.getResource(caminho);
        if (url == null) {
            System.err.println("[AVISO] Ícone não encontrado: " + caminho);
            return null;
        }
        try {
            Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(largura, altura, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            System.err.println("[AVISO] Erro ao carregar ícone: " + caminho);
            return null;
        }
    }

    // ==========================================================================
    // SOBRE
    // ==========================================================================

    private void mostrarSobre() {
        // Instancia a classe Sobre
        Sobre sobre = new Sobre();
        
        // Centraliza a janela "Sobre" em relação à janela principal (Carometro)
        sobre.setLocationRelativeTo(this);
        
        // Exibe a tela
        sobre.setVisible(true);
    }

    // ==========================================================================
    // RESET
    // ==========================================================================

    private void reset() {
        txtRA.setText(null); txtNome.setText(null);
        txtTelefone.setText(null); txtEmail.setText(null);
        txtAlergias.setText(null); txtObservacoes.setText(null);
        lblFoto.setIcon(new ImageIcon(Carometro.class.getResource("/img/camera.png")));
        txtRA.setEditable(true); btnBuscar.setEnabled(true);
        btnAdicionar.setEnabled(false); btnEditar.setEnabled(false);
        btnExcluir.setEnabled(false); btnCarregar.setEnabled(false);
        fotoByte = null; scrollPaneLista.setVisible(false);
    }

    // ==========================================================================
    // UNDO
    // ==========================================================================

    private void adicionarUndo(JTextComponent c) {
        UndoManager u = new UndoManager();
        c.getDocument().addUndoableEditListener(e -> u.addEdit(e.getEdit()));
        c.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z && u.canUndo()) u.undo();
            }
        });
    }

    // ==========================================================================
    // GETTERS / SETTERS
    // ==========================================================================

    public PreparedStatement getPst() { return pst; }
    public void setPst(PreparedStatement pst) { this.pst = pst; }

    // ==========================================================================
    // INNER CLASS — MÁSCARA DE TELEFONE
    // ==========================================================================

    class TelefoneMask extends PlainDocument {
        @Override
        public void insertString(int o, String s, AttributeSet a) throws BadLocationException {
            if (s == null) return;
            String n = (getText(0, getLength()) + s).replaceAll("[^0-9]", "");
            if (n.length() > 11) return;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n.length(); i++) {
                if (i == 0) sb.append("(");
                else if (i == 2) sb.append(") ");
                else if (i == 7) sb.append("-");
                sb.append(n.charAt(i));
            }
            super.remove(0, getLength());
            super.insertString(0, sb.toString(), a);
        }
    }
}