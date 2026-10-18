package br.unicentro.sistema_concessionaria.controller;

import br.unicentro.sistema_concessionaria.dao.CarroDAO;
import br.unicentro.sistema_concessionaria.dao.VendaDAO;
import br.unicentro.sistema_concessionaria.main.GerenciadorTelas;
import br.unicentro.sistema_concessionaria.model.Carro;
import br.unicentro.sistema_concessionaria.model.Venda;
import br.unicentro.sistema_concessionaria.model.Vendedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.LocalDate;

public class VendaController {

    @FXML private ComboBox<String> cbPlacaCarro;
    @FXML private Label lblVendedorLogado;
    @FXML private TextField txtNomeComprador;
    @FXML private DatePicker dpDataVenda;
    @FXML private TextField txtBusca;
    @FXML private ImageView imgLogoVendas;

    @FXML private TableView<Venda> tabelaVendas;
    @FXML private TableColumn<Venda, Integer> colId;
    @FXML private TableColumn<Venda, String> colPlaca;
    @FXML private TableColumn<Venda, String> colCpfVendedor;
    @FXML private TableColumn<Venda, String> colComprador;
    @FXML private TableColumn<Venda, LocalDate> colData;

    private VendaDAO vendaDAO = new VendaDAO();
    private CarroDAO carroDAO = new CarroDAO();
    private ObservableList<Venda> listaVendas;
    private Venda vendaSelecionada = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colCpfVendedor.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        colComprador.setCellValueFactory(new PropertyValueFactory<>("nomeComprador"));
        colData.setCellValueFactory(new PropertyValueFactory<>("dataVenda"));

        // Carrega a logo da concessionária
        if (imgLogoVendas != null) {
            try {
                Image logo = new Image(getClass().getResourceAsStream("/br/unicentro/sistema_concessionaria/image/logo.png"));
                imgLogoVendas.setImage(logo);
            } catch (Exception e) {
                System.out.println("Logo não encontrada.");
            }
        }

        Vendedor logado = LoginController.getVendedorLogado();
        if (logado != null) {
            lblVendedorLogado.setText(logado.getNome() + " (CPF: " + logado.getCpf() + ")");
        } else {
            lblVendedorLogado.setText("Nenhum vendedor autenticado");
        }

        carregarPlacasCarros();
        atualizarTabela();

        tabelaVendas.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                vendaSelecionada = novo;
                cbPlacaCarro.setValue(novo.getPlaca());
                txtNomeComprador.setText(novo.getNomeComprador());
                dpDataVenda.setValue(novo.getDataVenda());
            }
        });
    }

    private void carregarPlacasCarros() {
        ObservableList<String> placas = FXCollections.observableArrayList();
        for (Carro c : carroDAO.selecionarTodos()) {
            placas.add(c.getPlaca());
        }
        cbPlacaCarro.setItems(placas);
    }

    @FXML
    public void salvarVenda() {
        try {
            String placa = cbPlacaCarro.getValue();
            String comprador = txtNomeComprador.getText();
            LocalDate data = dpDataVenda.getValue();
            Vendedor logado = LoginController.getVendedorLogado();

            if (placa == null || comprador == null || comprador.trim().isEmpty() || data == null || logado == null) {
                alerta("Erro", "Preencha todos os campos obrigatórios e certifique-se de estar logado.");
                return;
            }

            if (vendaSelecionada == null) {
                Venda novaVenda = new Venda(0, placa, logado.getCpf(), comprador, data);
                if (vendaDAO.inserir(novaVenda)) {
                    alerta("Sucesso", "Venda registrada com sucesso!");
                } else {
                    alerta("Erro", "Não foi possível registrar a venda.");
                }
            } else {
                vendaSelecionada.setPlaca(placa);
                vendaSelecionada.setCpf(logado.getCpf());
                vendaSelecionada.setNomeComprador(comprador);
                vendaSelecionada.setDataVenda(data);

                if (vendaDAO.alterar(vendaSelecionada)) {
                    alerta("Sucesso", "Venda atualizada com sucesso!");
                } else {
                    alerta("Erro", "Não foi possível atualizar a venda.");
                }
            }

            limparCampos();
            atualizarTabela();
        } catch (Exception e) {
            alerta("Erro", "Ocorreu um erro ao salvar a venda: " + e.getMessage());
        }
    }

    @FXML
    public void excluirVenda() {
        if (vendaSelecionada != null) {
            if (vendaDAO.excluir(vendaSelecionada.getId())) {
                alerta("Sucesso", "Venda excluída com sucesso!");
                limparCampos();
                atualizarTabela();
            } else {
                alerta("Erro", "Não foi possível excluir a venda.");
            }
        } else {
            alerta("Aviso", "Selecione uma venda na tabela para excluir.");
        }
    }

    @FXML
    public void limparCampos() {
        cbPlacaCarro.setValue(null);
        txtNomeComprador.clear();
        dpDataVenda.setValue(null);
        vendaSelecionada = null;
        tabelaVendas.getSelectionModel().clearSelection();
    }

    @FXML
    public void voltarMenu() {
        GerenciadorTelas.voltarMenu();
    }

    private void atualizarTabela() {
        listaVendas = FXCollections.observableArrayList(vendaDAO.selecionarTodas());

        FilteredList<Venda> dadosFiltrados = new FilteredList<>(listaVendas, b -> true);

        if (txtBusca != null) {
            txtBusca.textProperty().addListener((observable, oldValue, newValue) -> {
                dadosFiltrados.setPredicate(venda -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();

                    if (venda.getPlaca() != null && venda.getPlaca().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (venda.getNomeComprador() != null && venda.getNomeComprador().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (venda.getCpf() != null && venda.getCpf().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (String.valueOf(venda.getId()).contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false;
                });
            });
        }

        SortedList<Venda> dadosOrdenados = new SortedList<>(dadosFiltrados);
        dadosOrdenados.comparatorProperty().bind(tabelaVendas.comparatorProperty());
        tabelaVendas.setItems(dadosOrdenados);
    }

    private void alerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}