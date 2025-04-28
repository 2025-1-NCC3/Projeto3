package br.com.fecapccp.uber;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.GeoPoint;
import android.preference.PreferenceManager;
import android.widget.Button;

import java.io.File;

public class Mapa extends AppCompatActivity {
    private MapView mapView;
    private Button btnMenu;

    private Button btnvoltar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

      //  btnvoltar = findViewById(R.id.voltar);

       // btnvoltar.setOnClickListener(view ->{
       //     Intent voltar = new Intent(this, MainActivity.class);
       //     startActivity(voltar);
     //   });


        setContentView(R.layout.activity_mapa);
        btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(view ->{
            Intent menu = new Intent(this, Menu.class);
            startActivity(menu);
        });
        btnvoltar = findViewById(R.id.voltar);

        btnvoltar.setOnClickListener(view ->{
          Intent voltar = new Intent(this, MainActivity.class);
          startActivity(voltar);
        });


        MapView map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);


        GeoPoint startPoint = new GeoPoint(-23.55052, -46.633308);
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        //limiteMapa();

        Marker marker = new Marker(map);
        marker.setPosition(startPoint);
        marker.setTitle("Você");
        map.getOverlays().add(marker);
    }
 /*  private void limiteMapa(){
        double minLatitude = 23.75;
        double maxLatitude = -23.45;
        double minLongitude = -46.75;
        double maxLongitude = -46.50;

        BoundingBox boundingBox = new BoundingBox(maxLatitude, maxLongitude, minLatitude, minLongitude);
        mapView.zoomToBoundingBox(boundingBox, true);
    }
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }*/
}
