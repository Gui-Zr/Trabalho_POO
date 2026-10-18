-- Tabela de Carros
CREATE TABLE carro (
    placa CHAR(7) PRIMARY KEY,
    modelo VARCHAR(100) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    ano INT NOT NULL,
    preco NUMERIC(10, 2) NOT NULL,
    imagem VARCHAR(255)
);

-- Tabela de Vendedores
CREATE TABLE vendedor (
    cpf CHAR(11) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100),
	senha VARCHAR(100)
);

-- Tabela de Vendas
CREATE TABLE venda (
    id SERIAL PRIMARY KEY,
    placa CHAR(7) NOT NULL,
    cpf CHAR(11) NOT NULL,
    nome_comprador VARCHAR(100) NOT NULL,
    data_venda DATE NOT NULL,
    CONSTRAINT fk_carro FOREIGN KEY (placa) REFERENCES carro(placa) ON DELETE CASCADE,
    CONSTRAINT fk_vendedor FOREIGN KEY (cpf) REFERENCES vendedor(cpf) ON DELETE CASCADE
);

INSERT INTO vendedor (cpf, nome, email, senha) 
VALUES ('12345678900', 'Vendedor Teste', 'vendedor@concessionaria.com', '1');

