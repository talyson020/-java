package view;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class Sobre extends JDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Sobre dialog = new Sobre();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public Sobre() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(Sobre.class.getResource("/img/logo.png")));
		setTitle("Sobre Carômetro Escolar");
		setResizable(false);
		setModal(true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Projeto Carômreto Escolar");
		lblNewLabel.setBounds(27, 11, 176, 14);
		getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("@Autor  Talyson M.Pedro");
		lblNewLabel_1.setBounds(27, 36, 151, 14);
		getContentPane().add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setIcon(new ImageIcon(Sobre.class.getResource("/img/estacio.png")));
		lblNewLabel_2.setBounds(358, 11, 66, 65);
		getContentPane().add(lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("Trabalho de Java 2023");
		lblNewLabel_3.setBounds(27, 61, 151, 14);
		getContentPane().add(lblNewLabel_3);
		
		JButton btnGithUb = new JButton("");
		btnGithUb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				link ("https://github.com/talyson020");
			}
		});
		btnGithUb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnGithUb.setContentAreaFilled(false);
		btnGithUb.setBorderPainted(false);
		btnGithUb.setIcon(new ImageIcon(Sobre.class.getResource("/img/github.png")));
		btnGithUb.setBounds(27, 114, 48, 48);
		getContentPane().add(btnGithUb);
		
		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		btnOK.setBounds(335, 214, 89, 23);
		getContentPane().add(btnOK);

	}
	
	private void link(String url) {
		Desktop desktop = Desktop.getDesktop();
		try {
			URI uri = new URI(url);
			desktop.browse(uri);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
