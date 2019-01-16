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

    public void restartService() {
        initBase();
    }

    public void resetPosition() {
        Log.d(TAG, "Resetting navigation coordinates to 0,0");
        setupNavigationVLS();
        base.cleanOriginalPoint();
        PoseVLS pose2D = base.getVLSPose(-1);
        base.setOriginalPoint(pose2D);

        lastXPosition = 0f;
        lastYPosition = 0f;
        Log.i(TAG, "Reset position");
    }

    public void moveToCoordinate(float x, float y) {
        setupNavigationVLS();
        base.addCheckPoint(x, y);
    }

    private void setupNavigationVLS() {
        setNavControlMode();
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
//            try {
//                while (!base.isVLSStarted()) {
//                    Log.d(TAG, "Waiting for VLS to get ready...");
//                    Thread.sleep(100);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            // enable obstacle avoidance
            Log.d(TAG, "is obstacle avoidance on? " + base.isUltrasonicObstacleAvoidanceEnabled() + " with distance " + base.getUltrasonicObstacleAvoidanceDistance());
            base.setUltrasonicObstacleAvoidanceEnabled(true);
            base.setUltrasonicObstacleAvoidanceDistance(0.5f);
            base.setObstacleStateChangeListener(obstacleStateChangedListener);
            Log.d(TAG, "is obstacle avoidance on? " + base.isUltrasonicObstacleAvoidanceEnabled() + " with distance " + base.getUltrasonicObstacleAvoidanceDistance());

    }

    private void setNavControlMode() {
        if (base.getControlMode() != Base.CONTROL_MODE_NAVIGATION) {
            Log.d(TAG, "Setting control mode to: NAVIGATION");
            base.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        }
    }

    private void initBase() {
        base = Base.getInstance();
        base.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "Base bind successful");
                base.setControlMode(Base.CONTROL_MODE_NAVIGATION);

                base.setOnCheckPointArrivedListener(new CheckPointStateListener() {
                    @Override
                    public void onCheckPointArrived(CheckPoint checkPoint, final Pose2D realPose, boolean isLast) {
                        Log.i(TAG, "Position before moving: " + lastXPosition + " / " + lastYPosition);
                        lastXPosition = checkPoint.getX();
                        lastYPosition = checkPoint.getY();
                        Log.i(TAG, "Position after moving: " + lastXPosition + " / " + lastYPosition);
                    }

                    @Override
                    public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
                        lastXPosition = checkPoint.getX();
                        lastYPosition = checkPoint.getY();
                        Log.i(TAG, "Missed checkpoint: " + lastXPosition + " " + lastYPosition);
                    }
                });

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
            Log.d(TAG, "onVLSPoseUpdate() called with: timestamp = [" + timestamp + "], pose_x = [" + pose_x + "], pose_y = [" + pose_y + "], pose_theta = [" + pose_theta + "], v = [" + v + "], w = [" + w + "]");
            Log.d(TAG, "Ultrasonic: " + base.getUltrasonicDistance());
        }
    };

    public void disconnect() {
        this.base.unbindService();
    }
}
