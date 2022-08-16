package com.rhossain.remotesms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SMS_Adapter extends ArrayAdapter<SMS> {
    List<SMS> smss;
    Context context;
    int resource;
    boolean inDetail;
    LinearLayout des;
    public SMS_Adapter(@NonNull Context context, int resource, @NonNull List<SMS> smss, boolean inDetail) {
        super(context, resource, smss);
        this.smss = smss;
        this.resource = resource;
        this.context = context;
        this.inDetail = inDetail;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resource, null);
        TextView from = view.findViewById(R.id.txt_from);
        TextView time = view.findViewById(R.id.txt_dt);
        TextView body = view.findViewById(R.id.txt_body);

        SMS n = smss.get(position);
        from.setText(n.getSender());
        body.setText(n.getBody());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(n.getDatetime());
        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");
        time.setText(dateFormat.format(date));
        return view;
    }
}
