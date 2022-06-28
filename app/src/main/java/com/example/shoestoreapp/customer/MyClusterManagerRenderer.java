package com.example.shoestoreapp.customer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.shoestoreapp.DataModels.ClusterMarker;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {

    private final IconGenerator iconGenerator;
    private final ImageView imageView;
    private final int markerWidth;
    private final int markerHeight;

    public MyClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);

        iconGenerator = new IconGenerator(context);
        imageView = new ImageView(context);
        markerWidth = 64;
        markerHeight = 64;
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = 4;
        imageView.setPadding(padding,padding,padding,padding);
        iconGenerator.setContentView(imageView);

    }

    /**
     * Rendering of the individual ClusterItems
     * @param item
     * @param markerOptions
     */
    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {

        imageView.setImageResource(item.getIconPicture());
        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());
        markerOptions.snippet(item.getSnippet());
    }


    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return false;
    }
}
