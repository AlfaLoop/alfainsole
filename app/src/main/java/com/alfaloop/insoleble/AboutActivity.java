package com.alfaloop.insoleble;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView license = (TextView)findViewById(R.id.about_license_textview);
        license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AlfaLoop/alfabase/blob/master/LICENSE"));
                startActivity(myIntent);
            }
        });

        TextView openSource = (TextView)findViewById(R.id.about_open_source_textview);
        openSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AlfaLoop"));
                startActivity(myIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AboutActivity.this.finish();
    }
}
