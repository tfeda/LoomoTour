package loomoTour.tourGuide.speech;
import android.content.Context;
import android.util.Log;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.tts.TtsListener;
import loomoTour.tourGuide.TourControl;


public class SpeechService {
    private static final String TAG = "SpeechService";
    private Speaker speaker;
    private TtsListener tts;
    private Context context;
    private TourControl tourControl;
    private static SpeechService instance;

    public static SpeechService getInstance(){
        return instance;
    }
    public SpeechService(Context context, TourControl tourControl){
        this.context = context;
        this.tourControl = tourControl;
        instance = this;
        init();
    }

    private void init() {
        speaker = Speaker.getInstance();
        speaker.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
            }

            @Override
            public void onUnbind(String reason) {
            }
        });

        tts = new TtsListener() {
            @Override
            public void onSpeechStarted(String s) {

            }

            @Override
            public void onSpeechFinished(String s) {
                tourControl.completedTask("Speaking");
            }

            @Override
            public void onSpeechError(String s, String s1) {
                //s is speech content, callback this method when speech occurs error.
            }
        };
    }

    public void speak(String message){
        try {
            speaker.speak(message, tts);
        } catch(VoiceException e){
            e.printStackTrace();
        }
    }

    public void disconnect() { this.speaker.unbindService();}
}
