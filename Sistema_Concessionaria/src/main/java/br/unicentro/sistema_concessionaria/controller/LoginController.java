package br.unicentro.sistema_concessionaria.controller;

import br.unicentro.sistema_concessionaria.dao.VendedorDAO;
import br.unicentro.sistema_concessionaria.main.GerenciadorTelas;
import br.unicentro.sistema_concessionaria.model.Vendedor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginController {

    @FXML private ImageView imgLogo;
    @FXML private TextField txtCpf;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblMensagem;

    private VendedorDAO vendedorDAO = new VendedorDAO();

    private static Vendedor vendedorLogado;

    @FXML
    public void initialize() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/br/unicentro/sistema_concessionaria/image/logo.png"));
            imgLogo.setImage(logo);
        } catch (Exception e) {
            System.out.println("Logo não encontrada na pasta de recursos.");
        }
    }

    @FXML
    public void entrar() {
        String cpf = txtCpf.getText();
        String senha = txtSenha.getText();

        if (cpf == null || cpf.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
            lblMensagem.setText("Por favor, preencha o CPF e a Senha.");
            return;
        }

        Vendedor vendedor = vendedorDAO.autenticar(cpf.trim(), senha.trim());

        if (vendedor != null) {
            vendedorLogado = vendedor;
            lblMensagem.setText("");
            GerenciadorTelas.abreTela("IncluiMenu.fxml", "Sistema de Concessionária - Menu Principal");
        } else {
            lblMensagem.setText("CPF ou senha incorretos.");
        }
    }

    @FXML
    public void sair() {
        Platform.exit();
    }

    public static Vendedor getVendedorLogado() {
        return vendedorLogado;
    }
}