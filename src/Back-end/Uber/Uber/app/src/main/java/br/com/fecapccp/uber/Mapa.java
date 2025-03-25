package br.com.fecapccp.uber;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.GeoPoint;
import android.preference.PreferenceManager;

import java.io.File;

public class Mapa extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());


        setContentView(R.layout.activity_mapa);


        MapView map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);


        GeoPoint startPoint = new GeoPoint(-23.55052, -46.633308);
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        Marker marker = new Marker(map);
        marker.setPosition(startPoint);
        marker.setTitle("Você");
        map.getOverlays().add(marker);
    }
}
