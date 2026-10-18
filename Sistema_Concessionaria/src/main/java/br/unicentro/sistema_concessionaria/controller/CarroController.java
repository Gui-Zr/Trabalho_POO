package br.unicentro.sistema_concessionaria.controller;

import br.unicentro.sistema_concessionaria.dao.CarroDAO;
import br.unicentro.sistema_concessionaria.main.GerenciadorTelas;
import br.unicentro.sistema_concessionaria.model.Carro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CarroController {

    @FXML private TextField txtPlaca;
    @FXML private TextField txtModelo;
    @FXML private TextField txtMarca;
    @FXML private TextField txtAno;
    @FXML private TextField txtPreco;
    @FXML private Label lblCaminhoImagem;
    @FXML private ImageView imgCarro;
    @FXML private ImageView imgLogoCarros;

    @FXML private TableView<Carro> tabelaCarros;
    @FXML private TableColumn<Carro, String> colPlaca;
    @FXML private TableColumn<Carro, String> colModelo;
    @FXML private TableColumn<Carro, String> colMarca;
    @FXML private TableColumn<Carro, Integer> colAno;
    @FXML private TableColumn<Carro, Double> colPreco;

    private CarroDAO carroDAO = new CarroDAO();
    private String nomeArquivoImagem = "";

    @FXML
    public void initialize() {
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("ano"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));

        atualizarTabela();

        try {
            Image logo = new Image(getClass().getResourceAsStream("/br/unicentro/sistema_concessionaria/image/logo.png"));
            imgLogoCarros.setImage(logo);
        } catch (Exception e) {
        }

        tabelaCarros.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                txtPlaca.setText(novo.getPlaca());
                txtPlaca.setDisable(true);
                txtModelo.setText(novo.getModelo());
                txtMarca.setText(novo.getMarca());
                txtAno.setText(String.valueOf(novo.getAno()));
                txtPreco.setText(String.valueOf(novo.getPreco()));
                nomeArquivoImagem = novo.getImagem();
                lblCaminhoImagem.setText(nomeArquivoImagem);

                if (nomeArquivoImagem != null && !nomeArquivoImagem.isEmpty()) {
                    File file = new File("imagens_carros/" + nomeArquivoImagem);
                    if (file.exists()) {
                        imgCarro.setImage(new Image(file.toURI().toString()));
                    }
                }
            }
        });
    }

    @FXML
    public void selecionarImagem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Imagem do Carro");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );
        File arquivoSelecionado = fileChooser.showOpenDialog(null);

        if (arquivoSelecionado != null) {
            try {
                File pastaDestino = new File("imagens_carros");
                if (!pastaDestino.exists()) {
                    pastaDestino.mkdirs();
                }

                nomeArquivoImagem = System.currentTimeMillis() + "_" + arquivoSelecionado.getName();
                File arquivoDestino = new File(pastaDestino, nomeArquivoImagem);

                Files.copy(arquivoSelecionado.toPath(), arquivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);

                lblCaminhoImagem.setText(nomeArquivoImagem);
                imgCarro.setImage(new Image(arquivoDestino.toURI().toString()));
            } catch (IOException e) {
                System.err.println("Erro ao salvar imagem: " + e.getMessage());
            }
        }
    }

    @FXML
    public void salvarCarro() {
        try {
            String placa = txtPlaca.getText();
            String modelo = txtModelo.getText();
            String marca = txtMarca.getText();
            int ano = Integer.parseInt(txtAno.getText());
            double preco = Double.parseDouble(txtPreco.getText());

            Carro carro = new Carro(placa, modelo, marca, ano, preco, nomeArquivoImagem);

            if (carroDAO.selecionarPorPlaca(placa) == null) {
                if (carroDAO.inserir(carro)) {
                    alerta("Sucesso", "Carro cadastrado com sucesso!");
                } else {
                    alerta("Erro", "Não foi possível cadastrar o carro.");
                }
            } else {
                if (carroDAO.alterar(carro)) {
                    alerta("Sucesso", "Carro atualizado com sucesso!");
                } else {
                    alerta("Erro", "Não foi possível atualizar o carro.");
                }
            }

            limparCampos();
            atualizarTabela();
        } catch (NumberFormatException e) {
            alerta("Erro de Validação", "Verifique se Ano e Preço contêm apenas números válidos.");
        }
    }

    @FXML
    public void excluirCarro() {
        Carro selecionado = tabelaCarros.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            if (carroDAO.excluir(selecionado.getPlaca())) {
                alerta("Sucesso", "Carro excluído com sucesso!");
                limparCampos();
                atualizarTabela();
            } else {
                alerta("Erro", "Não foi possível excluir o carro.");
            }
        } else {
            alerta("Aviso", "Selecione um carro na tabela para excluir.");
        }
    }

    @FXML
    public void limparCampos() {
        txtPlaca.clear();
        txtPlaca.setDisable(false);
        txtModelo.clear();
        txtMarca.clear();
        txtAno.clear();
        txtPreco.clear();
        lblCaminhoImagem.setText("Nenhuma imagem selecionada");
        imgCarro.setImage(null);
        nomeArquivoImagem = "";
        tabelaCarros.getSelectionModel().clearSelection();
    }

    @FXML
    public void voltarMenu() {
        GerenciadorTelas.voltarMenu();
    }

    private void atualizarTabela() {
        ObservableList<Carro> lista = FXCollections.observableArrayList(carroDAO.selecionarTodos());
        tabelaCarros.setItems(lista);
    }

    private void alerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}