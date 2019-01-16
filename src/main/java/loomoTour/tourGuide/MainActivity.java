package loomoTour.tourGuide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import loomoTour.tourGuide.base.BaseActivity;
import loomoTour.tourGuide.base.BaseService;

public class MainActivity extends Activity {
    private BaseService base;
    private TourControl tourControl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        tourControl = new TourControl();
        initServices();
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
                tourControl.beginTour(getApplicationContext());
                break;
            default:
                break;
        }
    }

    public void initServices(){
    base = new BaseService(getApplicationContext(), tourControl);
    };
}
