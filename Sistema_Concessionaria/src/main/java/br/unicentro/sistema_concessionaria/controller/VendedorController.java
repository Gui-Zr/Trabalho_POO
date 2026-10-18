package br.unicentro.sistema_concessionaria.controller;

import br.unicentro.sistema_concessionaria.dao.VendedorDAO;
import br.unicentro.sistema_concessionaria.main.GerenciadorTelas;
import br.unicentro.sistema_concessionaria.model.Vendedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VendedorController {

    @FXML private TextField txtCpf;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private ImageView imgLogoVendedores;

    @FXML private TableView<Vendedor> tabelaVendedores;
    @FXML private TableColumn<Vendedor, String> colCpf;
    @FXML private TableColumn<Vendedor, String> colNome;
    @FXML private TableColumn<Vendedor, String> colEmail;

    private VendedorDAO vendedorDAO = new VendedorDAO();

    @FXML
    public void initialize() {
        colCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        if (imgLogoVendedores != null) {
            try {
                Image logo = new Image(getClass().getResourceAsStream("/br/unicentro/sistema_concessionaria/image/logo.png"));
                imgLogoVendedores.setImage(logo);
            } catch (Exception e) {
            }
        }

        atualizarTabela();

        tabelaVendedores.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                txtCpf.setText(novo.getCpf());
                txtCpf.setDisable(true); // CPF é chave primária
                txtNome.setText(novo.getNome());
                txtEmail.setText(novo.getEmail());
                txtSenha.setText(novo.getSenha()); // Preenche a senha do vendedor selecionado
            }
        });
    }

    @FXML
    public void salvarVendedor() {
        try {
            String cpf = txtCpf.getText();
            String nome = txtNome.getText();
            String email = txtEmail.getText();
            String senha = txtSenha.getText();

            if (cpf.trim().isEmpty() || nome.trim().isEmpty() || senha.trim().isEmpty()) {
                alerta("Erro de Validação", "CPF, Nome e Senha são campos obrigatórios.");
                return;
            }

            Vendedor vendedor = new Vendedor(cpf, nome, email, senha);

            if (vendedorDAO.selecionarPorCpf(cpf) == null) {
                if (vendedorDAO.inserir(vendedor)) {
                    alerta("Sucesso", "Vendedor cadastrado com sucesso!");
                } else {
                    alerta("Erro", "Não foi possível cadastrar o vendedor.");
                }
            } else {
                if (vendedorDAO.alterar(vendedor)) {
                    alerta("Sucesso", "Vendedor atualizado com sucesso!");
                } else {
                    alerta("Erro", "Não foi possível atualizar o vendedor.");
                }
            }

            limparCampos();
            atualizarTabela();
        } catch (Exception e) {
            alerta("Erro", "Ocorreu um erro ao salvar o vendedor: " + e.getMessage());
        }
    }

    @FXML
    public void excluirVendedor() {
        Vendedor selecionado = tabelaVendedores.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            if (vendedorDAO.excluir(selecionado.getCpf())) {
                alerta("Sucesso", "Vendedor excluído com sucesso!");
                limparCampos();
                atualizarTabela();
            } else {
                alerta("Erro", "Não foi possível excluir o vendedor.");
            }
        } else {
            alerta("Aviso", "Selecione um vendedor na tabela para excluir.");
        }
    }

    @FXML
    public void limparCampos() {
        txtCpf.clear();
        txtCpf.setDisable(false);
        txtNome.clear();
        txtEmail.clear();
        txtSenha.clear();
        tabelaVendedores.getSelectionModel().clearSelection();
    }

    @FXML
    public void voltarMenu() {
        GerenciadorTelas.voltarMenu();
    }

    private void atualizarTabela() {
        ObservableList<Vendedor> lista = FXCollections.observableArrayList(vendedorDAO.selecionarTodos());
        tabelaVendedores.setItems(lista);
    }

    private void alerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}