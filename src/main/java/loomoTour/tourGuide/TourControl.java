package loomoTour.tourGuide;

import android.util.Log;
import android.content.Context;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import loomoTour.tourGuide.base.BaseService;
import loomoTour.tourGuide.speech.SpeechService;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Main controller of the tour. Manipulates Loomo's Services based on the specification of a read in file.
 */
public class TourControl {

    private Queue<TourPoint> tourPoints;
    private int checkpointIds;
    private static String TAG = "TourControl:";
    private boolean doneMoving;
    private boolean doneSpeaking;
    private TourPoint currentPoint;
    private static TourControl instance;


    public TourControl() {
        checkpointIds = 0;
        instance = this;
    }

    public static TourControl getInstance() {
        return instance;
    }

    /**
     * Adds a new checkpoint into the tour
     *
     * @param xList:       x coordinate of the loomo bot after moving
     * @param yList:       y coordinate of the loomo bot after moviong
     * @param name:        name of the destination
     * @param description: description of the destination that is read by the Loomo Speak service
     */
    private void addTourPoint(Queue<Float> xList, Queue<Float> yList, String name, String description) {

        TourPoint newPoint = new TourPoint(checkpointIds, xList, yList, name, description);
        try {
            tourPoints.add(newPoint);
        } catch (Exception e) {
            Log.e(TAG, "Error adding checkpoint to tour: " + e.toString());
        }
        checkpointIds++;
    }

    /**
     * Adds points to the tour from a file specified in /src/res/raw/
     * @param context application context to pass file information to the file Reader
     */
    public void setupTour(Context context) throws IOException {
        tourPoints = new LinkedList<TourPoint>();

        /****************************************CHOOSE TOUR FILE HERE******************************************/

        InputStream inputStream = context.getResources().openRawResource(R.raw.example);

        /*******************************************************************************************************/
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        Iterator<CSVRecord> csvIterator = csvParser.iterator();

        //create checkpoints out of the remaining lines
        CSVRecord record = csvIterator.next();
        while (csvIterator.hasNext()) {
            String name = record.get(0);
            String description = record.get(1);


            Queue<Float> xPoints = new LinkedList<Float>();
            Queue<Float> yPoints = new LinkedList<Float>();
            xPoints.add((Float.parseFloat(record.get(2))));
            yPoints.add((Float.parseFloat(record.get(3))));

            if (csvIterator.hasNext()) {
                record = csvIterator.next();
                if (record.get(0).isEmpty()) {
                    while (csvIterator.hasNext()) {
                        if (!record.get(0).isEmpty()) {
                            break;
                        }
                        xPoints.add((Float.parseFloat(record.get(2))));
                        yPoints.add((Float.parseFloat(record.get(3))));
                        if(csvIterator.hasNext()) record = csvIterator.next();
                    }
                    if(csvIterator.hasNext() == false){
                        xPoints.add((Float.parseFloat(record.get(2))));
                        yPoints.add((Float.parseFloat(record.get(3))));
                    }
                    addTourPoint(xPoints, yPoints, name, description);
                }
            }
        }
        List<TourPoint> tourPointList= (LinkedList<TourPoint>) tourPoints;
        for(int i = 0; i < tourPointList.size(); i++){
            tourPointList.get(i).print();
        }
    }

    public void beginTour(Context context) {
        BaseService.getInstance().resetPosition();
        try {
            setupTour(context);
        } catch (IOException e) {
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
        doneSpeaking = false;
        currentPoint = tourPoints.remove();
        Log.i(TAG, "current Point:" + " " + currentPoint.xQueue.peek() + " " + currentPoint.yQueue.peek() + " " + currentPoint.description);
        BaseService.getInstance().moveToCoordinate(currentPoint.xQueue.remove(), currentPoint.yQueue.remove());
        if (currentPoint.description.equals("")) {
            doneSpeaking = true;
        } else {
            SpeechService.getInstance().speak(currentPoint.description);

        }
    }

    /**
     * Receives method calls from the Loomo tasks upon completion and
     * executes the next part of the tour when all the task are complete
     *
     * @param task
     */
    public void completedTask(String task) {
        Log.i(TAG, "Task completed: " + task);
        switch (task) {
            case "Moving":
                if (currentPoint.xQueue.isEmpty()) doneMoving = true;
                else {
                    BaseService.getInstance().moveToCoordinate(currentPoint.xQueue.remove(), currentPoint.yQueue.remove());
                }
                break;
            case "Speaking":
                doneSpeaking = true;
                break;
        }
        if (doneMoving && doneSpeaking) {
            Log.i(TAG, "Tasks are complete");
            executeNextPoint();
        }
    }

    /**
     * In progress: The end of navigation and speech is reached
     */
    private void endTour() {
        //TODO give loomo an action for when it reaches the end of the tour
    }

    /**
     * TourPoint node that holds the speaking and movement
     * data for a hypothetical point of the tour
     */
    public class TourPoint {
        private int id;
        public Queue<Float> xQueue;
        public Queue<Float> yQueue;
        public String name;
        public String description;

        public TourPoint(int id, Queue<Float> xList, Queue<Float> yList, String name, String description) {
            this.xQueue = xList;
            this.yQueue = yList;
            this.name = name;
            this.description = description;
            this.id = id;
        }

        /**
         * Prints out tourPoint information to the console for debugging
         */
        public void print(){
            List<Float> xList= (LinkedList<Float>) xQueue;
            List<Float> yList= (LinkedList<Float>) yQueue;

            String xyList = "{";
            for (int i = 0; i < xQueue.size(); i++){
                xyList += "(" + xList.get(i) + ", " + yList.get(i) + ") ";
            }
            xyList += "}";

            Log.i(TAG, name + " " + description + " " + xyList);

        }
    }
}
