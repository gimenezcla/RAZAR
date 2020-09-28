package com.example.trazabilidadg;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
    public static void showError(Context context, String msg, int length){
        Toast toast = Toast.makeText(context,
                msg,
                length);//.show();
        View view = toast.getView();
        //To change the Background of Toast
        view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        TextView text = (TextView) view.findViewById(android.R.id.message);
        //Shadow of the Of the Text Color
        text.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        text.setTextColor(Color.WHITE);
        toast.show();
    }

    public static void showSuccess(Context context, String msg, int length){
        Toast toast = Toast.makeText(context,
                msg,
                length);//.show();
        View view = toast.getView();
        //To change the Background of Toast
        view.setBackgroundColor(Color.rgb(74,146,0));
        TextView text = (TextView) view.findViewById(android.R.id.message);
        //Shadow of the Of the Text Color
        text.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        text.setTextColor(Color.WHITE);
        toast.show();
    }

    public static void showInfo(Context context, String msg, int length){
        Toast toast = Toast.makeText(context,
                msg,
                length);//.show();
        View view = toast.getView();
        //To change the Background of Toast
        view.setBackgroundColor(Color.rgb(214,214,0));
        TextView text = (TextView) view.findViewById(android.R.id.message);
        //Shadow of the Of the Text Color
        text.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        text.setTextColor(Color.BLACK);
        toast.show();
    }
}
