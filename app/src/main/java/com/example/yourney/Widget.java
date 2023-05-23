package com.example.yourney;

import static android.app.PendingIntent.*;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class Widget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context, Widget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        for (int widgetId : allWidgetIds) {
            Sesion sesion = new Sesion(context);
            String username = sesion.getUsername();
            if (!username.equals("")) {
                Intent configIntent = new Intent(context, MainActivity.class);
                PendingIntent configPendingIntent = getActivity(context, 0, configIntent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
                remoteViews.setOnClickPendingIntent(R.id.main, configPendingIntent);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);

                Intent configIntent2 = new Intent(context, GrabarRuta.class);
                PendingIntent configPendingIntent2 = getActivity(context, 0, configIntent2,  FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
                remoteViews.setOnClickPendingIntent(R.id.añadir, configPendingIntent2);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);

                Intent configIntent3 = new Intent(context, PublicRoutesActivity.class);
                PendingIntent configPendingIntent3 = getActivity(context, 0, configIntent3,  FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
                remoteViews.setOnClickPendingIntent(R.id.buscar, configPendingIntent3);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);

                Intent configIntent4 = new Intent(context, RutasFavoritas.class);
                PendingIntent configPendingIntent4 = getActivity(context, 0, configIntent4,  FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
                remoteViews.setOnClickPendingIntent(R.id.favorito, configPendingIntent4);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

}
