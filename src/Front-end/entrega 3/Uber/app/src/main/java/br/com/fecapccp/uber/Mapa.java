package br.com.fecapccp.uber;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import br.com.fecapccp.uber.model.AvaliacaoZona;
import br.com.fecapccp.uber.model.Zona;
import br.com.fecapccp.uber.remote.ApiService;
import br.com.fecapccp.uber.remote.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Mapa extends AppCompatActivity {
    private static final String TAG = "MapaActivity";

    // Componentes da UI
    private MapView map;
    private Button btnVoltar;
    private Button btnMenu;
    private Button btnMostrarZonas;
    private ImageView btnAlerta;
    private ScrollView scrollAvaliacoes;
    private LinearLayout avaliacoesContainer;

    // Variáveis para API e dados
    private ApiService apiService;
    private List<Zona> zonas = new ArrayList<>();
    private Map<String, List<AvaliacaoZona>> avaliacoesPorZona = new HashMap<>();
    private String zonaAtualId = null;
    private String userId = null;
    private String userTipo = "passageiro"; // Ou "motorista", dependendo do tipo de usuário
    private String userName = null;
    private String userEmail = null;
    private String userTelefone = null;

    // Variáveis para alertas
    private BoundingBox spBounds;
    private Random random = new Random();
    private String[] tiposDeAlerta = {"Acidente na via", "Trânsito intenso", "Veículo quebrado", "Obras na pista", "Nevoeiro denso"};

    // Cores para classificação de zonas
    private final int[] CORES_CLASSIFICACAO = {
            Color.RED,       // 1 estrela - Vermelho
            Color.rgb(255, 165, 0), // 2 estrelas - Laranja
            Color.YELLOW,    // 3 estrelas - Amarelo
            Color.rgb(144, 238, 144), // 4 estrelas - Verde claro
            Color.GREEN      // 5 estrelas - Verde
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mapa);

        // Configuração do OpenStreetMap
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Inicializar componentes da UI
        map = findViewById(R.id.map);
        btnVoltar = findViewById(R.id.voltar);
        btnMenu = findViewById(R.id.btnMenu);
        btnMostrarZonas = findViewById(R.id.btnMostrarZonas);
        btnAlerta = findViewById(R.id.imageView9); // ID do ImageView do alerta
        scrollAvaliacoes = findViewById(R.id.scrollAvaliacoes);
        avaliacoesContainer = findViewById(R.id.avaliacoesContainer);

        // Configurar o mapa
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        // Definir limites do mapa (São Paulo)
        spBounds = new BoundingBox(
                -23.3, // North
                -46.3, // East
                -24.1, // South
                -47.1  // West
        );
        map.setScrollableAreaLimitDouble(spBounds);

        // Obter dados do usuário da Intent
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("USER_ID");
            userTipo = intent.getStringExtra("USER_TIPO");
            userName = intent.getStringExtra("USER_NAME");
            userEmail = intent.getStringExtra("USER_EMAIL");
            userTelefone = intent.getStringExtra("USER_TELEFONE");

            if (userTipo == null) {
                userTipo = "passageiro"; // Valor padrão
            }
        }

        if (userId == null) {
            // Se não foi passado, usar um valor padrão para testes
            userId = "507f191e810c19729de860ea"; // ID de teste válido (formato ObjectId)
            Log.w(TAG, "USER_ID não foi passado na Intent. Usando valor padrão para testes.");
        }

        // Inicializar o ApiService
        apiService = RetrofitClient.getApiService();

        // Configurar listeners
        btnVoltar.setOnClickListener(v -> finish());

        btnMenu.setOnClickListener(v -> {
            // Navegar para a tela de Menu
            Intent menuIntent = new Intent(Mapa.this, Menu.class);
            // Passar os dados do usuário para o Menu
            menuIntent.putExtra("USER_ID", userId);
            menuIntent.putExtra("USER_TIPO", userTipo);
            menuIntent.putExtra("USER_NAME", userName);
            menuIntent.putExtra("USER_EMAIL", userEmail);
            menuIntent.putExtra("USER_TELEFONE", userTelefone);
            startActivity(menuIntent);
        });

        btnMostrarZonas.setOnClickListener(v -> {
            carregarZonas();
        });

        // Configurar botão de alerta
        if (btnAlerta != null) {
            btnAlerta.setOnClickListener(v -> {
                gerarAlertaAleatorio();
            });
        } else {
            Log.e(TAG, "Botão de alerta não encontrado no layout!");
        }

        // Definir localização inicial (exemplo: São Paulo)
        GeoPoint startPoint = new GeoPoint(-23.5505, -46.6333);
        map.getController().setCenter(startPoint);

        // Carregar zonas automaticamente ao iniciar
        carregarZonas();

        // Configurar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void carregarZonas() {
        // Mostrar progresso
        Toast.makeText(this, "Carregando zonas...", Toast.LENGTH_SHORT).show();

        // Limpar mapa (mantendo apenas marcadores de alerta)
        List<Marker> marcadoresAlerta = new ArrayList<>();
        for (int i = 0; i < map.getOverlays().size(); i++) {
            if (map.getOverlays().get(i) instanceof Marker) {
                Marker marker = (Marker) map.getOverlays().get(i);
                if (marker.getTitle() != null && marker.getTitle().startsWith("ALERTA:")) {
                    marcadoresAlerta.add(marker);
                }
            }
        }

        map.getOverlays().clear();

        // Restaurar marcadores de alerta
        for (Marker marker : marcadoresAlerta) {
            map.getOverlays().add(marker);
        }

        // Chamar API para obter zonas
        apiService.getZonas().enqueue(new Callback<List<Zona>>() {
            @Override
            public void onResponse(Call<List<Zona>> call, Response<List<Zona>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    zonas = response.body();
                    exibirZonasNoMapa();
                    Toast.makeText(Mapa.this, "Zonas carregadas: " + zonas.size(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Mapa.this, "Erro ao carregar zonas: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao carregar zonas: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Zona>> call, Throwable t) {
                Toast.makeText(Mapa.this, "Falha ao carregar zonas: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha ao carregar zonas", t);

                // Para testes, criar zonas fictícias se a API falhar
                criarZonasFicticias();
            }
        });
    }

    private void exibirZonasNoMapa() {
        if (zonas.isEmpty()) {
            Toast.makeText(this, "Nenhuma zona disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Zona zona : zonas) {
            try {
                // Criar polígono para a zona
                Polygon poligono = criarPoligonoParaZona(zona);
                map.getOverlays().add(poligono);

                // Criar marcador para o centro da zona
                Marker marcador = criarMarcadorParaZona(zona);
                map.getOverlays().add(marcador);

                // Carregar avaliações para esta zona
                carregarAvaliacoesParaZona(zona.get_id());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao exibir zona: " + zona.getNome(), e);
            }
        }

        // Atualizar mapa
        map.invalidate();
    }

    private Polygon criarPoligonoParaZona(Zona zona) {
        Polygon poligono = new Polygon();
        List<GeoPoint> pontos = new ArrayList<>();

        try {
            // Extrair coordenadas do polígono GeoJSON
            List<List<List<Double>>> coordenadas = zona.getPoligono().getCoordinates();

            // O primeiro nível é o polígono (pode haver vários, pegamos o primeiro)
            List<List<Double>> coordenadasPoligono = coordenadas.get(0);

            // O segundo nível são os pontos do polígono
            for (List<Double> ponto : coordenadasPoligono) {
                // Formato GeoJSON: [longitude, latitude]
                double longitude = ponto.get(0);
                double latitude = ponto.get(1);
                pontos.add(new GeoPoint(latitude, longitude));
            }

            poligono.setPoints(pontos);

            // Definir estilo do polígono
            poligono.setFillColor(Color.argb(50, 0, 0, 255)); // Azul semi-transparente
            poligono.setStrokeColor(Color.BLUE);
            poligono.setStrokeWidth(3);

            // Adicionar listener de clique
            poligono.setOnClickListener((polygon, mapView, eventPos) -> {
                exibirAvaliacoesZona(zona);
                return true;
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar polígono para zona: " + zona.getNome(), e);
        }

        return poligono;
    }

    private Marker criarMarcadorParaZona(Zona zona) {
        Marker marcador = new Marker(map);

        try {
            // Calcular centro aproximado do polígono
            List<List<List<Double>>> coordenadas = zona.getPoligono().getCoordinates();
            List<List<Double>> coordenadasPoligono = coordenadas.get(0);

            double somLat = 0, somLon = 0;
            for (List<Double> ponto : coordenadasPoligono) {
                somLon += ponto.get(0);
                somLat += ponto.get(1);
            }

            double centroLat = somLat / coordenadasPoligono.size();
            double centroLon = somLon / coordenadasPoligono.size();

            marcador.setPosition(new GeoPoint(centroLat, centroLon));
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marcador.setTitle(zona.getNome());
            marcador.setSnippet(zona.getCidade());

            // Adicionar listener de clique
            marcador.setOnMarkerClickListener((marker, mapView) -> {
                exibirAvaliacoesZona(zona);
                return true;
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar marcador para zona: " + zona.getNome(), e);
        }

        return marcador;
    }

    private void carregarAvaliacoesParaZona(String zonaId) {
        apiService.getAvaliacoesByZonaId(zonaId).enqueue(new Callback<List<AvaliacaoZona>>() {
            @Override
            public void onResponse(Call<List<AvaliacaoZona>> call, Response<List<AvaliacaoZona>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AvaliacaoZona> avaliacoes = response.body();
                    avaliacoesPorZona.put(zonaId, avaliacoes);
                    Log.d(TAG, "Avaliações carregadas para zona " + zonaId + ": " + avaliacoes.size());

                    // Se esta é a zona atual sendo exibida, atualizar a visualização
                    if (zonaId.equals(zonaAtualId)) {
                        atualizarVisualizacaoAvaliacoes(avaliacoes);
                    }

                    // Atualizar cor do marcador baseado na classificação média
                    atualizarCorMarcadorPorClassificacao(zonaId, avaliacoes);
                } else {
                    Log.e(TAG, "Erro ao carregar avaliações para zona " + zonaId + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<AvaliacaoZona>> call, Throwable t) {
                Log.e(TAG, "Falha ao carregar avaliações para zona " + zonaId, t);

                // Para testes, criar avaliações fictícias se a API falhar
                if (!avaliacoesPorZona.containsKey(zonaId)) {
                    avaliacoesPorZona.put(zonaId, criarAvaliacoesFicticias(zonaId));

                    // Se esta é a zona atual sendo exibida, atualizar a visualização
                    if (zonaId.equals(zonaAtualId)) {
                        atualizarVisualizacaoAvaliacoes(avaliacoesPorZona.get(zonaId));
                    }
                }
            }
        });
    }

    private void atualizarCorMarcadorPorClassificacao(String zonaId, List<AvaliacaoZona> avaliacoes) {
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            return;
        }

        // Calcular classificação média
        float somaClassificacoes = 0;
        for (AvaliacaoZona avaliacao : avaliacoes) {
            somaClassificacoes += avaliacao.getClassificacaoEstrelas();
        }
        float classificacaoMedia = somaClassificacoes / avaliacoes.size();

        // Determinar cor baseada na classificação média
        int indiceCorClassificacao = Math.min(Math.max((int) Math.round(classificacaoMedia) - 1, 0), CORES_CLASSIFICACAO.length - 1);
        int corClassificacao = CORES_CLASSIFICACAO[indiceCorClassificacao];

        // Atualizar cor do marcador
        for (Zona zona : zonas) {
            if (zona.get_id().equals(zonaId)) {
                for (int i = 0; i < map.getOverlays().size(); i++) {
                    if (map.getOverlays().get(i) instanceof Marker) {
                        Marker marcador = (Marker) map.getOverlays().get(i);
                        if (marcador.getTitle() != null && marcador.getTitle().equals(zona.getNome())) {
                            marcador.setIcon(getResources().getDrawable(android.R.drawable.btn_star_big_on));
                            marcador.setTitle(zona.getNome() + " (" + String.format("%.1f", classificacaoMedia) + "★)");
                            break;
                        }
                    }
                }
                break;
            }
        }

        // Atualizar mapa
        map.invalidate();
    }

    private void exibirAvaliacoesZona(Zona zona) {
        zonaAtualId = zona.get_id();

        // Limpar container de avaliações
        avaliacoesContainer.removeAllViews();

        // Adicionar título
        TextView txtTitulo = new TextView(this);
        txtTitulo.setText("Avaliações para " + zona.getNome());
        txtTitulo.setTextSize(18);
        txtTitulo.setPadding(0, 0, 0, 16);
        avaliacoesContainer.addView(txtTitulo);

        // Adicionar botão para nova avaliação
        Button btnNovaAvaliacao = new Button(this);
        btnNovaAvaliacao.setText("+ Nova Avaliação");
        btnNovaAvaliacao.setOnClickListener(v -> {
            Intent intent = new Intent(Mapa.this, AvaliarZonaActivity.class);
            intent.putExtra("ZONA_ID", zona.get_id());
            intent.putExtra("USER_ID", userId);
            intent.putExtra("USER_TIPO", userTipo);
            startActivity(intent);
        });
        avaliacoesContainer.addView(btnNovaAvaliacao);

        // Adicionar botão para reportar alerta nesta zona
        Button btnReportarAlerta = new Button(this);
        btnReportarAlerta.setText("⚠️ Reportar Alerta");
        btnReportarAlerta.setTextColor(Color.RED);
        btnReportarAlerta.setOnClickListener(v -> {
            // Abrir diálogo para escolher tipo de alerta
            reportarAlertaParaZona(zona);
        });
        avaliacoesContainer.addView(btnReportarAlerta);

        // Verificar se já temos avaliações para esta zona
        List<AvaliacaoZona> avaliacoes = avaliacoesPorZona.get(zona.get_id());
        if (avaliacoes != null && !avaliacoes.isEmpty()) {
            atualizarVisualizacaoAvaliacoes(avaliacoes);
        } else {
            // Adicionar mensagem de carregamento
            TextView txtCarregando = new TextView(this);
            txtCarregando.setText("Carregando avaliações...");
            txtCarregando.setPadding(0, 16, 0, 0);
            avaliacoesContainer.addView(txtCarregando);

            // Carregar avaliações
            carregarAvaliacoesParaZona(zona.get_id());
        }

        // Mostrar o ScrollView
        scrollAvaliacoes.setVisibility(View.VISIBLE);
    }

    private void atualizarVisualizacaoAvaliacoes(List<AvaliacaoZona> avaliacoes) {
        // Remover todas as views exceto o título e os botões
        if (avaliacoesContainer.getChildCount() > 3) {
            avaliacoesContainer.removeViews(3, avaliacoesContainer.getChildCount() - 3);
        }

        if (avaliacoes == null || avaliacoes.isEmpty()) {
            TextView txtSemAvaliacoes = new TextView(this);
            txtSemAvaliacoes.setText("Nenhuma avaliação disponível para esta zona.");
            txtSemAvaliacoes.setPadding(0, 16, 0, 0);
            avaliacoesContainer.addView(txtSemAvaliacoes);
            return;
        }

        // Calcular classificação média
        float somaClassificacoes = 0;
        for (AvaliacaoZona avaliacao : avaliacoes) {
            somaClassificacoes += avaliacao.getClassificacaoEstrelas();
        }
        float classificacaoMedia = somaClassificacoes / avaliacoes.size();

        // Adicionar classificação média
        View viewClassificacaoMedia = LayoutInflater.from(this).inflate(R.layout.item_avaliacao_zona, avaliacoesContainer, false);
        RatingBar ratingBarMedia = viewClassificacaoMedia.findViewById(R.id.ratingBarAvaliacao);
        TextView txtTipoUsuarioMedia = viewClassificacaoMedia.findViewById(R.id.txtTipoUsuario);
        TextView txtComentarioMedia = viewClassificacaoMedia.findViewById(R.id.txtComentario);
        TextView txtAlertaMedia = viewClassificacaoMedia.findViewById(R.id.txtAlerta);
        TextView txtDataMedia = viewClassificacaoMedia.findViewById(R.id.txtData);

        ratingBarMedia.setRating(classificacaoMedia);
        txtTipoUsuarioMedia.setText("Classificação Média (" + avaliacoes.size() + " avaliações)");
        txtComentarioMedia.setText("Classificação média: " + String.format("%.1f", classificacaoMedia) + " de 5 estrelas");
        txtAlertaMedia.setVisibility(View.GONE);
        txtDataMedia.setVisibility(View.GONE);

        avaliacoesContainer.addView(viewClassificacaoMedia);

        // Adicionar separador
        View separador = new View(this);
        separador.setBackgroundColor(Color.GRAY);
        separador.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        avaliacoesContainer.addView(separador);

        // Adicionar cada avaliação
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        SimpleDateFormat formatoSaida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (AvaliacaoZona avaliacao : avaliacoes) {
            View viewAvaliacao = LayoutInflater.from(this).inflate(R.layout.item_avaliacao_zona, avaliacoesContainer, false);
            RatingBar ratingBar = viewAvaliacao.findViewById(R.id.ratingBarAvaliacao);
            TextView txtTipoUsuario = viewAvaliacao.findViewById(R.id.txtTipoUsuario);
            TextView txtComentario = viewAvaliacao.findViewById(R.id.txtComentario);
            TextView txtAlerta = viewAvaliacao.findViewById(R.id.txtAlerta);
            TextView txtData = viewAvaliacao.findViewById(R.id.txtData);

            ratingBar.setRating(avaliacao.getClassificacaoEstrelas());
            txtTipoUsuario.setText("Avaliação de " + (avaliacao.getTipoUsuario() != null ? avaliacao.getTipoUsuario() : "usuário"));

            if (avaliacao.getComentario() != null && !avaliacao.getComentario().isEmpty()) {
                txtComentario.setText(avaliacao.getComentario());
                txtComentario.setVisibility(View.VISIBLE);
            } else {
                txtComentario.setVisibility(View.GONE);
            }

            if (avaliacao.getTipoAlerta() != null && !avaliacao.getTipoAlerta().isEmpty()) {
                txtAlerta.setText("⚠️ Alerta: " + avaliacao.getTipoAlerta());
                txtAlerta.setTextColor(Color.RED);
                txtAlerta.setVisibility(View.VISIBLE);
            } else {
                txtAlerta.setVisibility(View.GONE);
            }

            if (avaliacao.getDataAvaliacao() != null && !avaliacao.getDataAvaliacao().isEmpty()) {
                try {
                    Date data = formatoEntrada.parse(avaliacao.getDataAvaliacao());
                    txtData.setText(formatoSaida.format(data));
                } catch (ParseException e) {
                    txtData.setText(avaliacao.getDataAvaliacao());
                }
                txtData.setVisibility(View.VISIBLE);
            } else {
                txtData.setVisibility(View.GONE);
            }

            avaliacoesContainer.addView(viewAvaliacao);
        }
    }

    // Método para reportar um alerta para uma zona específica
    private void reportarAlertaParaZona(Zona zona) {
        // Aqui você pode abrir um diálogo para escolher o tipo de alerta
        // Para simplificar, vamos usar um alerta aleatório
        String tipoAlerta = tiposDeAlerta[random.nextInt(tiposDeAlerta.length)];

        // Criar uma avaliação com alerta
        AvaliacaoZona avaliacaoComAlerta = new AvaliacaoZona();
        avaliacaoComAlerta.setZonaId(zona.get_id());
        avaliacaoComAlerta.setUsuarioId(userId);
        avaliacaoComAlerta.setTipoUsuario(userTipo);
        avaliacaoComAlerta.setClassificacaoEstrelas(3.0f); // Classificação neutra
        avaliacaoComAlerta.setTipoAlerta(tipoAlerta);
        avaliacaoComAlerta.setComentario("Alerta reportado: " + tipoAlerta);

        // Calcular centro da zona para o alerta
        List<List<List<Double>>> coordenadas = zona.getPoligono().getCoordinates();
        List<List<Double>> coordenadasPoligono = coordenadas.get(0);
        double somLat = 0, somLon = 0;
        for (List<Double> ponto : coordenadasPoligono) {
            somLon += ponto.get(0);
            somLat += ponto.get(1);
        }
        double centroLat = somLat / coordenadasPoligono.size();
        double centroLon = somLon / coordenadasPoligono.size();

        // Adicionar marcador de alerta no mapa
        GeoPoint pontoAlerta = new GeoPoint(centroLat, centroLon);
        Marker alertaMarker = new Marker(map);
        alertaMarker.setPosition(pontoAlerta);
        alertaMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        alertaMarker.setTitle("ALERTA: " + tipoAlerta);
        alertaMarker.setSnippet("Zona: " + zona.getNome());

        Drawable alertIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_alert);
        if (alertIcon != null) {
            alertaMarker.setIcon(alertIcon);
        }

        map.getOverlays().add(alertaMarker);
        map.invalidate();

        // Enviar para o backend
        apiService.enviarAvaliacaoZona(avaliacaoComAlerta).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(Mapa.this, "Alerta reportado com sucesso!", Toast.LENGTH_SHORT).show();
                    // Recarregar avaliações para atualizar a lista
                    carregarAvaliacoesParaZona(zona.get_id());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Sem detalhes";
                        Log.e(TAG, "Erro ao reportar alerta: " + response.code() + " - " + errorBody);
                        Toast.makeText(Mapa.this, "Erro ao reportar alerta: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e(TAG, "Falha ao reportar alerta", t);
                Toast.makeText(Mapa.this, "Falha ao reportar alerta: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Para testes, adicionar o alerta localmente
                if (!avaliacoesPorZona.containsKey(zona.get_id())) {
                    avaliacoesPorZona.put(zona.get_id(), new ArrayList<>());
                }
                avaliacoesPorZona.get(zona.get_id()).add(avaliacaoComAlerta);

                // Se esta é a zona atual sendo exibida, atualizar a visualização
                if (zona.get_id().equals(zonaAtualId)) {
                    atualizarVisualizacaoAvaliacoes(avaliacoesPorZona.get(zona.get_id()));
                }
            }
        });
    }

    // Método para gerar um alerta aleatório no mapa
    private void gerarAlertaAleatorio() {
        double latMin = spBounds.getLatSouth();
        double latMax = spBounds.getLatNorth();
        double lonMin = spBounds.getLonWest();
        double lonMax = spBounds.getLonEast();

        double latAleatoria = latMin + (latMax - latMin) * random.nextDouble();
        double lonAleatoria = lonMin + (lonMax - lonMin) * random.nextDouble();
        GeoPoint pontoAlerta = new GeoPoint(latAleatoria, lonAleatoria);

        String tipoAlerta = tiposDeAlerta[random.nextInt(tiposDeAlerta.length)];
        String detalhesAlerta = "Localização: " + String.format("%.4f", latAleatoria) + ", " + String.format("%.4f", lonAleatoria);

        Marker alertaMarker = new Marker(map);
        alertaMarker.setPosition(pontoAlerta);
        alertaMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        alertaMarker.setTitle("ALERTA: " + tipoAlerta);
        alertaMarker.setSnippet(detalhesAlerta);

        Drawable alertIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_alert);
        if (alertIcon != null) {
            alertaMarker.setIcon(alertIcon);
        }

        // Usar a InfoWindow customizada com o layout info_window_alerta.xml
        alertaMarker.setInfoWindow(new CustomInfoWindow(map, tipoAlerta, detalhesAlerta));

        map.getOverlays().add(alertaMarker);
        map.invalidate();

    }

    // Classe interna para InfoWindow customizada usando R.layout.info_window_alerta
    public class CustomInfoWindow extends InfoWindow {
        private String mTitleText;
        private String mSnippetText;

        public CustomInfoWindow(MapView mapView, String title, String snippet) {
            super(R.layout.info_window_alerta, mapView); // Usar diretamente o layout customizado
            mTitleText = title;
            mSnippetText = snippet;
        }

        @Override
        public void onOpen(Object item) {
            InfoWindow.closeAllInfoWindowsOn(mMapView); // Fechar outras InfoWindows

            TextView txtTitle = mView.findViewById(R.id.iw_title); // ID do layout info_window_alerta.xml
            if (txtTitle != null) {
                txtTitle.setText("ALERTA: " + mTitleText);
            }

            TextView txtSnippet = mView.findViewById(R.id.iw_snippet); // ID do layout info_window_alerta.xml
            if (txtSnippet != null) {
                txtSnippet.setText(mSnippetText);
            }

            // Listener para fechar a InfoWindow ao clicar nela (comportamento comum)
            mView.setOnClickListener(v -> close());
        }

        @Override
        public void onClose() {
            // Nada especial a fazer aqui por enquanto
        }
    }

    // Métodos para testes (quando a API não está disponível)
    private void criarZonasFicticias() {
        zonas = new ArrayList<>();

        // Zona 1: Centro de São Paulo
        Zona zona1 = new Zona();
        zona1.set_id("zona1");
        zona1.setNome("Centro");
        zona1.setCidade("São Paulo");

        Zona.Poligono poligono1 = new Zona.Poligono();
        poligono1.setType("Polygon");

        List<List<List<Double>>> coordenadas1 = new ArrayList<>();
        List<List<Double>> pontos1 = new ArrayList<>();

        // Coordenadas aproximadas do centro de São Paulo
        pontos1.add(List.of(-46.6500, -23.5450));
        pontos1.add(List.of(-46.6300, -23.5450));
        pontos1.add(List.of(-46.6300, -23.5550));
        pontos1.add(List.of(-46.6500, -23.5550));
        pontos1.add(List.of(-46.6500, -23.5450)); // Fechar o polígono

        coordenadas1.add(pontos1);
        poligono1.setCoordinates(coordenadas1);
        zona1.setPoligono(poligono1);

        zonas.add(zona1);

        // Zona 2: Paulista
        Zona zona2 = new Zona();
        zona2.set_id("zona2");
        zona2.setNome("Paulista");
        zona2.setCidade("São Paulo");

        Zona.Poligono poligono2 = new Zona.Poligono();
        poligono2.setType("Polygon");

        List<List<List<Double>>> coordenadas2 = new ArrayList<>();
        List<List<Double>> pontos2 = new ArrayList<>();

        // Coordenadas aproximadas da Av. Paulista
        pontos2.add(List.of(-46.6600, -23.5650));
        pontos2.add(List.of(-46.6400, -23.5650));
        pontos2.add(List.of(-46.6400, -23.5750));
        pontos2.add(List.of(-46.6600, -23.5750));
        pontos2.add(List.of(-46.6600, -23.5650)); // Fechar o polígono

        coordenadas2.add(pontos2);
        poligono2.setCoordinates(coordenadas2);
        zona2.setPoligono(poligono2);

        zonas.add(zona2);

        // Exibir zonas no mapa
        exibirZonasNoMapa();
    }

    private List<AvaliacaoZona> criarAvaliacoesFicticias(String zonaId) {
        List<AvaliacaoZona> avaliacoes = new ArrayList<>();

        // Avaliação 1
        AvaliacaoZona avaliacao1 = new AvaliacaoZona();
        avaliacao1.setId("av1_" + zonaId);
        avaliacao1.setZonaId(zonaId);
        avaliacao1.setUsuarioId("user1");
        avaliacao1.setTipoUsuario("passageiro");
        avaliacao1.setClassificacaoEstrelas(4.5f);
        avaliacao1.setComentario("Ótima região, bem movimentada e segura durante o dia.");
        avaliacao1.setDataAvaliacao("2025-05-10T14:30:00.000Z");
        avaliacao1.setAtiva(true);

        // Avaliação 2
        AvaliacaoZona avaliacao2 = new AvaliacaoZona();
        avaliacao2.setId("av2_" + zonaId);
        avaliacao2.setZonaId(zonaId);
        avaliacao2.setUsuarioId("user2");
        avaliacao2.setTipoUsuario("motorista");
        avaliacao2.setClassificacaoEstrelas(3.0f);
        avaliacao2.setComentario("Trânsito intenso em horários de pico.");
        avaliacao2.setTipoAlerta("Trânsito intenso");
        avaliacao2.setLocalizacaoAproximada("Próximo ao cruzamento principal");
        avaliacao2.setDataAvaliacao("2025-05-12T18:45:00.000Z");
        avaliacao2.setAtiva(true);

        // Avaliação 3
        AvaliacaoZona avaliacao3 = new AvaliacaoZona();
        avaliacao3.setId("av3_" + zonaId);
        avaliacao3.setZonaId(zonaId);
        avaliacao3.setUsuarioId("user3");
        avaliacao3.setTipoUsuario("passageiro");
        avaliacao3.setClassificacaoEstrelas(5.0f);
        avaliacao3.setComentario("Excelente área para embarque e desembarque, muitos pontos de referência.");
        avaliacao3.setDataAvaliacao("2025-05-15T09:20:00.000Z");
        avaliacao3.setAtiva(true);

        avaliacoes.add(avaliacao1);
        avaliacoes.add(avaliacao2);
        avaliacoes.add(avaliacao3);

        return avaliacoes;
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();

        // Recarregar avaliações se estiver visualizando uma zona
        if (zonaAtualId != null) {
            carregarAvaliacoesParaZona(zonaAtualId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}
