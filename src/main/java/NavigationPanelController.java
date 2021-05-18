
/** Author: Shubham Rane www.linkedin.com/in/shubham-rane97 **/

import com.github.sarxos.webcam.Webcam;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;


public class NavigationPanelController {

    public TextArea systemLogTA_ID;
    public TextField opName_ID;
    public Button captureImageBtn_ID;
    public TextField batteryLife_ID;
    public TextField distanceCovered_ID;
    public ImageView imageView_ID;
    public TextField weatherRtf_ID;
    public Button backTrack_btn;

    /** Initialize Value */
    public void initialize() throws IOException {

        /** Sets operator name fetching from user_ID field from login */
        opName_ID.setText(LoginController.operatorName);

        /** Battery of laptop showing here can be replaced with vehicles battery */
        Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
        Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
        batteryLife_ID.setText(batteryStatus.toString());

        /** Turn on Video Cam */
        try{

            turnOnVideoCam();

        }catch (Exception e){
            System.out.println("Camera Failed to Start");
            e.printStackTrace();
        }

        /** Get weather data */
        try{

            /** Gets Latitude and Longitude */
            String latitude = LocalMapGenerator.latitudeGetter( LocalMapGenerator.publicIP_Finder() );
            String longitude = LocalMapGenerator.longitudeGetter( LocalMapGenerator.publicIP_Finder() );

            /** Call Weather api for weather data */
            String currentWeather = LocalMapGenerator.getWeatherData(latitude, longitude);
            weatherRtf_ID.setText( currentWeather );

        }catch(Exception e){
            weatherRtf_ID.setText("N/A");
            System.out.println("Couldn't get weather data");
            e.printStackTrace();
        }

    }

    /**
     * Calculating for How long key was pressed
     * */
    private KeyCode currKey;
    private KeyCode lastKey = null;
    private long keyPressedSystemTime = 0;
    private long keyHeldDuration = 0;

    /** System time when key was pressed */
    public void arrowKeyStrokesHandler(KeyEvent keyEvent) {
        currKey = keyEvent.getCode();
        if(currKey != lastKey){
            lastKey = currKey;
            if(currKey == KeyCode.W){ //UP
                keyPressedSystemTime = System.currentTimeMillis();
            }
            else if(currKey == KeyCode.D){ //RIGHT
                keyPressedSystemTime = System.currentTimeMillis();
            }
            else if(currKey == KeyCode.A){ //LEFT
                keyPressedSystemTime = System.currentTimeMillis();
            }
            else if(currKey == KeyCode.S){ //DOWN/BACK
                keyPressedSystemTime = System.currentTimeMillis();
            }
            else if(currKey == KeyCode.SPACE){ // Stop/Space
                keyPressedSystemTime = System.currentTimeMillis();
            }
        }
    }

    /** System time when key was released */
    StringBuilder backTrackingLog = new StringBuilder();
    StringBuilder forwardTrackingLog = new StringBuilder();
    public void arrowKeyReleaseHandler(KeyEvent keyEvent) throws IOException {

        /** Values initializers for heatmap */
        String direction = null;
        long distance = 0;

        /** Checks which key was released to map its released system time*/
        KeyCode releasedKey = keyEvent.getCode();
        if (currKey == releasedKey) {
            keyHeldDuration = System.currentTimeMillis() - keyPressedSystemTime;
            keyPressedSystemTime = 0;
            lastKey = null;
        }

        /** Controls and SystemLogging on TextArea */
        if (currKey == KeyCode.W ) {

            if(keyHeldDuration / 1000 > 0){
                forwardTrackingLog.append("Forward : " + keyHeldDuration /1000 + " sec ");
                forwardTrackingLog.append(keyHeldDuration % 1000 + " millisec");
                secondsTravelled += keyHeldDuration;

                backTrackingLog.append("Reverse : ").append(keyHeldDuration / 1000).append(" sec ");
                backTrackingLog.append(keyHeldDuration % 1000).append(" millisec");

                direction = "Forward";
                distance = keyHeldDuration /1000;

            }else{
                forwardTrackingLog.append("Forward : " + keyHeldDuration %1000 + " millisec");
                backTrackingLog.append("Reverse : ").append(keyHeldDuration % 1000).append(" millisec");
            }

        }
        else if (currKey == KeyCode.A){
            if(keyHeldDuration /1000 > 0){
                forwardTrackingLog.append("Left        : " + keyHeldDuration /1000 + " sec ");
                forwardTrackingLog.append(keyHeldDuration %1000 + " millisec");
                secondsTravelled += keyHeldDuration;

                backTrackingLog.append("Right      : ").append(keyHeldDuration / 1000).append(" sec ");
                backTrackingLog.append(keyHeldDuration % 1000).append(" millisec");

                direction = "Left";
                distance = keyHeldDuration /1000;

            }else{
                forwardTrackingLog.append("Left        : " + keyHeldDuration %1000 + " millisec");
                backTrackingLog.append("Right      : ").append(keyHeldDuration % 1000).append(" millisec");
            }

        }
        else if(currKey == KeyCode.S){
            if(keyHeldDuration /1000 > 0){
                forwardTrackingLog.append("Reverse : " + keyHeldDuration /1000 + " sec ");
                forwardTrackingLog.append(keyHeldDuration %1000 + " millisec");
                secondsTravelled += keyHeldDuration;

                backTrackingLog.append("Forward : ").append(keyHeldDuration / 1000).append(" sec ");
                backTrackingLog.append(keyHeldDuration % 1000).append(" millisec");


                direction = "Reverse";
                distance = keyHeldDuration /1000;

            }else{
                forwardTrackingLog.append("Reverse : " + keyHeldDuration %1000 + " millisec");
                backTrackingLog.append("Forward : ").append(keyHeldDuration % 1000).append(" millisec");
            }

        }
        else if(currKey == KeyCode.D){
            if(keyHeldDuration /1000 > 0){
                forwardTrackingLog.append("Right     : " + keyHeldDuration /1000 + " sec ");
                forwardTrackingLog.append(keyHeldDuration %1000 + " millisec");
                secondsTravelled += keyHeldDuration;

                backTrackingLog.append("Left       : ").append(keyHeldDuration / 1000).append(" sec ");
                backTrackingLog.append(keyHeldDuration % 1000).append(" millisec");

                direction = "Right";
                distance = keyHeldDuration /1000;

            }else{
                forwardTrackingLog.append("Right     : " + keyHeldDuration %1000 + " millisec");
                backTrackingLog.append("Left       : ").append(keyHeldDuration % 1000).append(" millisec");
            }

        }
        else if(currKey == KeyCode.SPACE){
            forwardTrackingLog.append("Brake");
            forwardTrackingLog.append("\n");

            backTrackingLog.append("Brake");
            backTrackingLog.append("\n");
        }

        /** Resets and pushes each log on next line in systemLogs textfield*/
        forwardTrackingLog.append("\n");
        backTrackingLog.append("\n");
        keyHeldDuration = 0;

        systemLogTA_ID.setText(String.valueOf(forwardTrackingLog));

        /** Call method to calculate total distance covered w.r.t. seconds key was pressed*/
        totalDistanceTravelled();

        /** Sending data to heatMapGenerator class after each key release*/
        assert direction != null;
        HeatMapGenerator.heatChartGenerator(direction, distance);
        HeatMapGenerator.heatMapGeneration();

        /** Responsible for creating blended heatChart with maps*/
        new MapBlender().combineHeatmapWithGoogleMap();

        /** Periodically check for battery status after each key release*/
        batteryStatusChecker();

    }

    /** Total distance travelled */
    float secondsTravelled = 0;
    float movedDistance = 0;
    double vehicleSpeed = 5.0; // 50 miles per hour
    private void totalDistanceTravelled(){
        System.out.println(secondsTravelled);

        movedDistance = (float) ( (vehicleSpeed / 360.0) * secondsTravelled);
        distanceCovered_ID.setText(String.valueOf(movedDistance));
        System.out.println(movedDistance);
    }

    /** Toggle between Track & Backtrack logs */
    boolean isBackTrackOn = false;
    public void backtrackBtnClicked() {
        if(!isBackTrackOn){
            isBackTrackOn = true;
            backTrack_btn.setText("Track");
            systemLogTA_ID.setText(String.valueOf(backTrackingLog));
        }else{
            isBackTrackOn = false;
            backTrack_btn.setText("Backtrack");
            systemLogTA_ID.setText(String.valueOf(forwardTrackingLog));
        }

    }

    /** Battery of laptop/vehicle displayed here */
    private void batteryStatusChecker(){
        Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
        Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
        batteryLife_ID.setText(batteryStatus.toString());
    }

    /** Capture Images from default camera and sent to ImageProcessor */
    boolean isCaptureClicked = false;
    public void captureImageBtnClicked() {

        isCaptureClicked = true;
        ImageProcessor.stopCapture();

        Webcam webCamObj = Webcam.getDefault();
        webCamObj.open();

        BufferedImage capturedImage = webCamObj.getImage();

        byte[] pixels = ((DataBufferByte) capturedImage.getRaster().getDataBuffer()).getData();
        Mat capturedMat = new Mat(capturedImage.getHeight(), capturedImage.getWidth(), CvType.CV_8UC3);
        capturedMat.put(0, 0, pixels);

        ImageProcessor.detectFaceFromImages(capturedMat, isCaptureClicked);

        isCaptureClicked = false;
        webCamObj.close();
        turnOnVideoCam();
    }

    /** Capture video from video cam **/
    static VideoCapture capture;
    private void turnOnVideoCam() {
        capture = new VideoCapture(0);

        new AnimationTimer() {
            @Override public void handle(long l) {
                /** Calling Image processor class for face detection */
                imageView_ID.setImage(ImageProcessor.getCapture());
            }
        }.start();

    }

}
