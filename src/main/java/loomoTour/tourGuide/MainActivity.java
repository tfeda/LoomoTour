package loomoTour.tourGuide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import loomoTour.tourGuide.base.BaseActivity;
import loomoTour.tourGuide.base.BaseService;
import loomoTour.tourGuide.head.HeadActivity;

public class MainActivity extends Activity {
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        context = getApplicationContext();
        TourControl tourControl = new TourControl();
        try {
            tourControl.setupTour(getApplicationContext());
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    public static Context getMainContext(){
        return context;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.base:
                startActivity(new Intent(this, BaseActivity.class));
                break;
            case R.id.head:
                startActivity(new Intent(this, HeadActivity.class));
                break;
            default:
                break;
        }
    }
}
