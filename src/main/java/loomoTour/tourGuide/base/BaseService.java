package loomoTour.tourGuide.base;

import android.content.Context;
import android.util.Log;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.VLSPoseListener;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.algo.minicontroller.ObstacleStateChangedListener;
import loomoTour.tourGuide.TourControl;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.locomotion.sbv.StartVLSListener;

/**
 * Created by abr on 22.12.17.
 */

public class BaseService {

    private static final String TAG = "BaseService";

    private Base base = null;
    private Context context;
    private RobotCheckpointListener checkpointListener = null;
    private RobotVLSListener vlsListener = null;
    private TourControl tourControl;
    private float lastXPosition = 0f;
    private float lastYPosition = 0f;

    public static BaseService instance;

    public static BaseService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BaseService instance not initialized yet");
        }
        return instance;
    }

    public BaseService(Context context, TourControl tourControl) {
        this.context = context;
        this.tourControl = tourControl;
        initBase();
        this.instance = this;
    }

    /**
     * restarts the base service
     */
    public void restartService() {
        initBase();
    }

    /**
     * Sets the current position of Loomo to be the origin for future navigation.
     */
    public void resetPosition() {
        while(base.isVLSStarted() == false){ }
        Log.d(TAG, "Resetting navigation coordinates to 0,0");
        base.cleanOriginalPoint();
        PoseVLS pose2D = base.getVLSPose(-1);
        base.setOriginalPoint(pose2D);

        lastXPosition = 0f;
        lastYPosition = 0f;
        Log.i(TAG, "Reset position");
    }

    /**
     *  Moves the loomo to the specified coordinates. The coordinates are based on Loomo's tracked starting point.
     * @param x x location, in meters
     * @param y y location, in meters
     */
    public void moveToCoordinate(float x, float y) {
        base.addCheckPoint(x, y);
    }

    /**
     * Sets up Loomo's navigation to be based on its visual Localization Service
     */
    private void setupNavigationVLS() {
        if (checkpointListener == null) {
            checkpointListener = new RobotCheckpointListener();
            base.setOnCheckPointArrivedListener(checkpointListener);
        }
        Log.d(TAG, "is vls started?" + base.isVLSStarted());

        if (!base.isVLSStarted()) {
            Log.d(TAG, "starting VLS");
            if (vlsListener == null) {
                vlsListener = new RobotVLSListener();
                base.startVLS(true, true, vlsListener);
                while (!base.isVLSStarted()) ;
                base.setVLSPoseListener(vlsPoseListener);
            }
        }

//             enable obstacle avoidance
            base.setUltrasonicObstacleAvoidanceEnabled(true);
            base.setUltrasonicObstacleAvoidanceDistance(.25f);
            base.setObstacleStateChangeListener(obstacleStateChangedListener);
            Log.d(TAG, "is obstacle avoidance on? " + base.isUltrasonicObstacleAvoidanceEnabled() + " with distance " + base.getUltrasonicObstacleAvoidanceDistance());

    }

    /**
     * Inits the base service
     */
    private void initBase() {
        base = Base.getInstance();
        base.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "Base bind successful");
                base.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                setupNavigationVLS();
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "Base bind failed");
            }
        });
        }

    private ObstacleStateChangedListener obstacleStateChangedListener = new ObstacleStateChangedListener() {
        @Override
        public void onObstacleStateChanged(int ObstacleAppearance) {
            Log.i(TAG, "ObstacleStateChanged " + ObstacleAppearance);
        }
    };

    /**
     * Listener that reacts to Loomo arriving or missing a checkPoint
     */
    private class RobotCheckpointListener implements CheckPointStateListener {
        @Override
        public void onCheckPointArrived(CheckPoint checkPoint, final Pose2D realPose, boolean isLast){
            Log.i(TAG, "Arrived to checkpoint: " + checkPoint);
            tourControl.completedTask("Moving");
        }

        @Override
        public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
            Log.i(TAG, "Missed checkpoint: " + checkPoint);
        }
    }

    private class RobotVLSListener implements StartVLSListener {
        @Override
        public void onOpened() {
            Log.i(TAG, "VLSListener onOpenend");
            base.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_VLS);

        }

        @Override
        public void onError(String errorMessage) {
            Log.i(TAG, "VLSListener error: " + errorMessage);

        }
    }

    private VLSPoseListener vlsPoseListener = new VLSPoseListener() {
        @Override
        public void onVLSPoseUpdate(long timestamp, float pose_x, float pose_y, float pose_theta, float v, float w) {
        }
    };

    public void disconnect() {
        this.base.unbindService();
    }
}
