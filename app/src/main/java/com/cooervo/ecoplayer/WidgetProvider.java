package com.cooervo.ecoplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = WidgetProvider.class.getSimpleName();
    private static String WIDGET_CLICKED = "widget_clicked";

    private static boolean isPlaying = false;
    private RemoteViews remoteViews;
    private ComponentName playerWidget;


    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //Inflate remoteViews from R.layout.widget_layout
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        playerWidget = new ComponentName(context, WidgetProvider.class);

        Intent intentClick = new Intent(context, WidgetProvider.class);
        intentClick.setAction(WIDGET_CLICKED);
        intentClick.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, "" + appWidgetIds[0]);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetIds[0], intentClick, 0);
        remoteViews.setOnClickPendingIntent(R.id.img_btn, pendingIntent);
        remoteViews.setInt(R.id.img_btn, "setBackgroundResource", R.drawable.play);

        appWidgetManager.updateAppWidget(playerWidget, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Bundle extras = intent.getExtras();

        //If WIDGET_CLICKED
        if (extras != null && intent.getAction().equals(WIDGET_CLICKED)) {

            //If isPlaying
            if (isPlaying) {
                managePlay(context);

                //If NOT playing
            } else {
                manageStop(context);
            }

            playerWidget = new ComponentName(context, WidgetProvider.class);
            (AppWidgetManager.getInstance(context)).updateAppWidget(playerWidget, remoteViews);
        }
    }

    /**
     * When widget is deleted stop MediaPlayerService
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        Intent i = new Intent(context, MediaPlayerService.class);
        i.putExtra("command", "stopSong");
        context.stopService(i);
    }

    private void manageStop(Context context) {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        //setBackground as STOP
        remoteViews.setInt(R.id.img_btn, "setBackgroundResource", R.drawable.stop);
        isPlaying = true;

        Intent i = new Intent(context, MediaPlayerService.class);
        i.putExtra("command", "playRandomSong");
        context.startService(i);
    }

    private void managePlay(Context context) {
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        //setBackground as PLAY
        remoteViews.setInt(R.id.img_btn, "setBackgroundResource", R.drawable.play);
        isPlaying = false;

        Intent i = new Intent(context, MediaPlayerService.class);
        i.putExtra("command", "stopSong");
        context.startService(i);
    }
}


