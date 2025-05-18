//entrega 2 
//vamos adaptar o código usando a biblioteca node-forge.



//instalar pacotes necessarios:
//npm install mysql2 node-forge
//codigo mtsql:
const express = require("express");
const cors = require("cors");
const mysql = require("mysql2");
const bodyParser = require("body-parser");
const forge = require("node-forge");

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Conexão com o MySQL
const db = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: "", // ajuste conforme necessário
  database: "transporte_db"
});

db.connect((err) => {
  if (err) {
    console.error("Erro ao conectar ao banco:", err.message);
  } else {
    console.log("Conectado ao MySQL!");
  }
});

// ===================
// Funções de RSA
// ===================
// Função para calcular o MDC (Máximo Divisor Comum) usando o algoritmo de Euclides
function gcd(a, b) {
    while (b !== 0) {
        let temp = b;
        b = a % b;
        a = temp;
    }
    return a;
}

// Função para calcular a inversa modular (usando o algoritmo estendido de Euclides)
function modInverse(e, m) {
    let m0 = m, x0 = 0, x1 = 1;
    if (m === 1) return 0;

    while (e > 1) {
        let q = Math.floor(e / m);
        let t = m;

        m = e % m;
        e = t;
        t = x0;

        x0 = x1 - q * x0;
        x1 = t;
    }

    if (x1 < 0) x1 += m0;
    return x1;
}

// Função simples para verificar se um número é primo
function isPrime(num) {
    if (num <= 1) return false;
    for (let i = 2; i <= Math.sqrt(num); i++) {
        if (num % i === 0) return false;
    }
    return true;
}

// Função para gerar um número primo aleatório dentro de um intervalo
function generatePrime(bits) {
    let prime;
    while (true) {
        prime = Math.floor(Math.random() * (2 ** bits)); // Gerar número aleatório no intervalo
        if (isPrime(prime)) break;
    }
    return prime;
}

// Geração das chaves RSA
function generateRSAKeys(bits = 2048) {
    const p = generatePrime(bits / 2); // Gerar dois números primos grandes
    const q = generatePrime(bits / 2);
    const n = p * q;
    const phi = (p - 1) * (q - 1);

    let e = 3;
    while (gcd(e, phi) !== 1) {
        e += 2;  // Escolhe um valor ímpar para e
    }

    const d = modInverse(e, phi); // Calcula a chave privada d

    return { p, q, n, e, d };
}

// Cifrar a mensagem com a chave pública (e, n)
function encrypt(message, e, n) {
    let messageBytes = Buffer.from(message, 'utf-8');
    let messageInt = BigInt('0x' + messageBytes.toString('hex')); // Convert to BigInt
    let cipherText = messageInt ** BigInt(e) % BigInt(n);
    return cipherText.toString();
}

// Decifrar a mensagem com a chave privada (d, n)
function decrypt(cipherText, d, n) {
    let cipherInt = BigInt(cipherText);
    let decryptedInt = cipherInt ** BigInt(d) % BigInt(n);
    let decryptedHex = decryptedInt.toString(16);
    let decryptedBuffer = Buffer.from(decryptedHex, 'hex');
    return decryptedBuffer.toString('utf-8');
}

// Teste
const { p, q, n, e, d } = generateRSAKeys(2048);
console.log(`p: ${p}`);
console.log(`q: ${q}`);
console.log(`n: ${n}`);
console.log(`e: ${e}`);
console.log(`d: ${d}`);

// Chave para criptografar
const chave = "mPhoRFh1cxOXROy/70dgNzPBSTw=";

// Cifrar
const chaveCifrada = encrypt(chave, e, n);
console.log("Chave cifrada:", chaveCifrada);

// Decifrar
const chaveDecifrada = decrypt(chaveCifrada, d, n);
console.log("Chave decifrada:", chaveDecifrada);

// ===================
// Rotas
// ===================

app.get("/", (req, res) => {
  res.send("Olá do servidor com MySQL e RSA!");
});

app.get("/Passageiro", (req, res) => {
  db.query("SELECT * FROM Passageiro", (err, results) => {
    if (err) return res.status(500).json({ error: "Erro ao buscar dados" });
    res.json(results);
  });
});

app.get("/Motorista", (req, res) => {
  db.query("SELECT * FROM Motorista", (err, results) => {
    if (err) return res.status(500).json({ error: "Erro ao buscar dados" });
    res.json(results);
  });
});

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
