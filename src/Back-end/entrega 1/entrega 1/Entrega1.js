//Codigo adaptado para mysql para entrega 1 :
const express = require("express");
const cors = require("cors");
const mysql = require("mysql2");
const bodyParser = require("body-parser");

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Conexão com o MySQL
const db = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: "", // Altere se necessário
  database: "transporte_db"
});

db.connect((err) => {
  if (err) {
    console.error("Erro ao conectar ao banco:", err.message);
  } else {
    console.log("Conectado ao MySQL!");
  }
});

// Rota GET Passageiro
app.get("/Passageiro", (req, res) => {
  db.query("SELECT * FROM Passageiro", (err, results) => {
    if (err) return res.status(500).json({ error: "Erro ao buscar dados" });
    res.json(results);
  });
});

// Rota GET Motorista
app.get("/Motorista", (req, res) => {
  db.query("SELECT * FROM Motorista", (err, results) => {
    if (err) return res.status(500).json({ error: "Erro ao buscar dados" });
    res.json(results);
  });
});

// Rota principal
app.get("/", (req, res) => {
  res.send("Olá do servidor com MySQL!");
});

// POST Passageiro
app.post("/regp", (req, res) => {
  const { Nome, Senha, CPF, Email } = req.body;
  if (!Nome || !Senha || !CPF || !Email) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios!" });
  }

  const sql = "INSERT INTO Passageiro (Nome, Senha, CPF, Email) VALUES (?, ?, ?, ?)";
  db.query(sql, [Nome, Senha, CPF, Email], (err, result) => {
    if (err) return res.status(500).json({ error: "Erro ao inserir passageiro" });
    res.json({ message: "Passageiro cadastrado com sucesso!", id: result.insertId });
  });
});

// POST Motorista
app.post("/regm", (req, res) => {
  const { Nome, Senha, Email, CPF, CNH, Placa, Marca, Modelo, Celular } = req.body;
  if (!Nome || !Senha || !CPF || !Email || !CNH || !Placa || !Marca || !Modelo || !Celular) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios!" });
  }

  const sql = `
    INSERT INTO Motorista (Nome, Senha, Email, CPF, CNH, Placa, Marca, Modelo, Celular)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`;
  db.query(sql, [Nome, Senha, Email, CPF, CNH, Placa, Marca, Modelo, Celular], (err, result) => {
    if (err) return res.status(500).json({ error: "Erro ao inserir motorista" });
    res.json({ message: "Motorista cadastrado com sucesso!", id: result.insertId });
  });
});

app.listen(port, () => {
  console.log(`Servidor rodando na porta ${port}`);
});

