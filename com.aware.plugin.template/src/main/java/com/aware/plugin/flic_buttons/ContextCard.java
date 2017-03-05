package com.aware.plugin.flic_buttons;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aware.utils.IContextCard;

public class ContextCard implements IContextCard {

    //Constructor used to instantiate this card
    public ContextCard() {}

    @Override
    public View getContextCard(Context context) {
        //Load card layout
        View card = LayoutInflater.from(context).inflate(R.layout.card, null);

        TextView number_clicks = (TextView) card.findViewById(R.id.number_clicks);
        TextView last_clicked = (TextView) card.findViewById(R.id.last_clicked_button);

        String count;
        Cursor clicks = context.getContentResolver().query(Provider.FlicButtons_Data.CONTENT_URI, new String[] {"count(*) AS count"}, null, null, null);
        if (clicks != null && clicks.moveToFirst()) {
            count = clicks.getString(clicks.getColumnIndex("count"));
            number_clicks.setText((count != null ? count : "0") + " clicks total");
        }

        if (clicks != null && !clicks.isClosed()) clicks.close();

        String latest_button_label;
        Cursor latest_button = context.getContentResolver().query(Provider.FlicButtons_Data.CONTENT_URI, null, null, null, Provider.FlicButtons_Data.TIMESTAMP + " DESC LIMIT 1");
        if (latest_button != null && latest_button.moveToFirst()) {
            latest_button_label = latest_button.getString(latest_button.getColumnIndex(Provider.FlicButtons_Data.BUTTON));
            last_clicked.setText("Latest: " + (latest_button_label != null ? latest_button_label : "No clicks so far"));
        }

        if (latest_button != null && !latest_button.isClosed()) latest_button.close();

        return card;
    }
}
