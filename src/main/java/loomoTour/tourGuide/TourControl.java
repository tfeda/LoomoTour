package loomoTour.tourGuide;

import android.util.Log;
import android.content.Context;
import loomoTour.tourGuide.base.BaseService;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;


public class TourControl {

    private Queue<Checkpoint> tourPoints;
    private int checkpointIds;
    private static String TAG = "TourControl:";
    private boolean doneMoving;
    private boolean doneSpeaking;
    private Checkpoint currentPoint;
    private static TourControl instance;


    public TourControl() {
        tourPoints = new LinkedList<Checkpoint>();
        checkpointIds = 0;
        instance = this;
    }

    public static TourControl getInstance(){
        return instance;
    }

    /**
     * Adds a new checkpoint into the tour
     *
     * @param xList:       x coordinate of the loomo bot after moving
     * @param yList:       y coordinate of the loomo bot after moviong
     * @param name:        name of the destination
     * @param description: description of the destination
     */
    private void addPoint(Queue<Float> xList, Queue<Float> yList, String name, String description) {

        Checkpoint newPoint = new Checkpoint(checkpointIds, xList, yList, name, description);
        try {
            tourPoints.add(newPoint);
        } catch (Exception e) {
            Log.e(TAG, "Error adding checkpoint to tour: " + e.toString());
        }
        checkpointIds++;
    }

    /**
     * Adds points to the tour
     */
    public void setupTour(Context context) throws IOException {
        Log.i(TAG, "In setupTour");
        InputStream inputStream = context.getResources().openRawResource(R.raw.test);
        CSVReader csvReader = new CSVReader(inputStream);
        Queue<String[]> csvLines = csvReader.read();
        //Remove the title from the queue
        csvLines.remove();
        //create checkpoints out of the remaining lines
        while(csvLines.isEmpty() == false) {
            String[] line = csvLines.remove();
            String name = line[0];
            String description = line[1];
            Queue<Float> xPoints = new LinkedList<Float>();
            Queue<Float> yPoints = new LinkedList<Float>();
            Log.i(TAG, line[2] + " " + line[3]);
            xPoints.add((Float.parseFloat(line[2])));
            yPoints.add((Float.parseFloat(line[3])));

            while(csvLines.isEmpty() == false && csvLines.peek()[0].equals("") == false){
                String[] pointLine = csvLines.remove();
                Log.i(TAG, pointLine[2] + " " + pointLine[3]);
                xPoints.add((Float.parseFloat(pointLine[2])));
                yPoints.add((Float.parseFloat(pointLine[3])));
            }
            addPoint(xPoints, yPoints, name, description);
        }
    }

    /**
     * Begins the tour
     */
    public void beginTour(Context context) {
        BaseService.getInstance().resetPosition();
        try{
            setupTour(context);
        } catch (IOException e){
            e.printStackTrace();
        }
        executeNextPoint();
    }

    private void executeNextPoint() {
        if (tourPoints.isEmpty()) {
            endTour();
            return;
        }


        doneMoving = false;
        doneSpeaking = true; //TODO CHANGE TO FALSE

        currentPoint = tourPoints.remove();
        BaseService.getInstance().moveToCoordinate(currentPoint.xList.remove(), currentPoint.yList.remove());
        //TODO call the loomo to move to the destination of the new checkpoint
        //TODO concurrently call the loomo to read off the checkpoint name and description
    }

    /**
     * Receives method calls from the Loomo tasks upon completion and
     * executes the next part of the tour when all the task are complete
     *
     * @param task
     */
    public void completedTask(String task) {
        switch (task) {
            case "Moving":
                if(currentPoint.xList.isEmpty()) doneMoving = true;
                else {
                    BaseService.getInstance().moveToCoordinate(currentPoint.xList.remove(), currentPoint.yList.remove());
                }
                break;
            case "Speaking":
                doneSpeaking = true;
                break;
        }
        if (doneMoving && doneSpeaking) {
            executeNextPoint();
        }
    }

    private void endTour() {
        //TODO give loomo an action for when it reaches the end of the tour
    }

    /**
     * Checkpoint node that controls the speaking and movement
     * data for a hypothetical point of the tour
     */
    public class Checkpoint {
        private int id;
        public Queue<Float> xList;
        public Queue<Float> yList;
        public String name;
        public String description;

        public Checkpoint(int id, Queue<Float> xList, Queue<Float> yList, String name, String description) {
            this.xList = xList;
            this.yList = yList;
            this.name = name;
            this.description = description;
            this.id = id;
        }
    }
}
