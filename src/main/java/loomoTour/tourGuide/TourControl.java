package com.segway.robot.locomotionsample;

import android.util.Log;
import android.content.Context;
import com.segway.robot.locomotionsample.base.LoomoBaseService;
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
//        addPoint(3, 0, "MC-Hammer", "Cant touch this");
//        addPoint(6, 0, "MC-Hammer", "Cant touch this");
//        addPoint(0, 0, "MC-Hammer", "Cant touch this");
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
            while(csvLines.isEmpty() == false && csvLines.peek()[0].equals("") == false){
                String[] pointLine = csvLines.remove();
                xPoints.add((Float.parseFloat(pointLine[2])));
                yPoints.add((Float.parseFloat(pointLine[3])));
            }
            addPoint(xPoints, yPoints, name, description);
        }
    }

    /**
     * Begins the tour
     */
    public void beginTour() {
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
        LoomoBaseService.getInstance().moveToCoordinate(currentPoint.xList.remove(), currentPoint.yList.remove());
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
                    LoomoBaseService.getInstance().moveToCoordinate(currentPoint.xList.remove(), currentPoint.yList.remove());
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
