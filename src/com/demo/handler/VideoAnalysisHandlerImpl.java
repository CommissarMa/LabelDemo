package com.demo.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import com.demo.entity.FrameAnalysisResult;
import com.demo.entity.NormalizedLabel;
import com.demo.entity.PeopleStatus;
import com.demo.entity.Window;
import com.demo.entity.WindowStatus;
import com.demo.util.ImageUtil;

import javafx.scene.image.Image;

public class VideoAnalysisHandlerImpl implements VideoAnalysisHandler{
	
	private VideoCapture capture;//��Ƶ��֡��
	private Mat nextMat;//��һ֡ͼ��
	
	private BackgroundSubtractorMOG2 bgModel;
	
	public final static ExecutorService executor = Executors.newFixedThreadPool(5);
	
	/**
	 * ���췽�� 
	 * @param videoPath ��Ƶ·����ע�⴫����ַ�����ʽ��D://video//1.avi
	 * 2017��5��6��15:19:19
	 */
	public VideoAnalysisHandlerImpl(String videoPath) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);//����Opencv��
		capture=new VideoCapture(videoPath);//������Ƶ
		bgModel=Video.createBackgroundSubtractorMOG2();//��ʼ������ģ��
	}
	
	/**
	 * ��ȡ��Ƶ�ĵ�һ֡
	 * @return JavaFx Image��ʽ��ͼ�񣬷���null�����ȡʧ��
	 * 2017��5��6��20:44:47
	 */
	@Override
	public Image getFirstFrame() {
		if(!capture.isOpened()){//�����ȡ������Ƶ������null
			return null;
		}else{
			Mat frame=new Mat();
			capture.read(frame);//��һ֡
			nextMat=new Mat();
			capture.read(nextMat);//�ڶ�֡
			if(!frame.empty()){//ͼƬ��ȡ�ɹ�
				Image image= ImageUtil.matToJavaFxImage(frame);
				return image;
			}else{//��ȡ����Ƶ��β������null
				return null;
			}
		}
	}
	
	/**
	 * �ж��Ƿ��ܼ�������������Ƶ�ǲ��ǵ���β�ˣ�
	 * @return true�����ܹ�����������false�����ܼ�������
	 * 2017��5��6��19:52:30
	 */
	@Override
	public boolean isAnalysisContinue() {
		if(!nextMat.empty()){//Mat��Ϊ��
			return true;
		}else{
			capture.release();
			return false;
		}
	}

	/**
	 * �Ե�ǰ֡�����쳣�����������ط������
	 * @return FrameAnalysisResult ��������࣬����������Image���쳣�ľ����ַ����ȵ�
	 * 2017��5��6��21:08:46
	 */
	@Override
	public FrameAnalysisResult getCurrentFrameAnalyisResult(List<NormalizedLabel> labelList) {
		Mat currentSrcMat = nextMat.clone();//��ǰ֡
		Mat peopleMat = nextMat.clone();//����������������˵ļ�⣬�����л���
		Image currentSrcImage = ImageUtil.matToJavaFxImage(currentSrcMat);//��ǰ֡��JavaFx Image��ʽ
		
		Future<PeopleStatus> peopleCheckTask = launchPeopleCheckTask(peopleMat);
		
		CompletionService<Window> windowCheckTasks = launchWindowCheckTasks(labelList, currentSrcMat);
		
		PeopleStatus peopleStatus = getPeopleCheckResult(peopleCheckTask);
		
		List<Window> windowList = getWindowCheckResult(windowCheckTasks, labelList.size());
		
		long currentTime = getCurrentTime();
		
		capture.read(nextMat);//��ȡ��һ֡
		return new FrameAnalysisResult(currentSrcImage, peopleStatus, windowList , currentTime);
	}
	
	private Future<PeopleStatus> launchPeopleCheckTask(Mat peopleMat) {
		return executor.submit(new Callable<PeopleStatus>(){
			@Override
			public PeopleStatus call() throws Exception {
				return getPeopleStatus(peopleMat);
			}});
	}
	
	private CompletionService<Window> launchWindowCheckTasks(List<NormalizedLabel> labelList, Mat image) {
		
		Mat redRegionMask = ImageUtil.getRedRegionMask(image);
        
        CompletionService<Window> comp = new ExecutorCompletionService<>(executor);
        for(int i=0;i<labelList.size(); i++){
        	final int  windowIndex = i;
            comp.submit(new Callable<Window>(){
    			@Override
    			public Window call() throws Exception {
    				WindowStatus windowStatus = getWindowStatus(labelList.get(windowIndex), image, redRegionMask);
    				return new Window(windowIndex+1, windowStatus);
    			}});
        }
        
        return comp;
	}
	
	private PeopleStatus getPeopleCheckResult(Future<PeopleStatus> peopleCheckTask) {
		try {
			return peopleCheckTask.get();
		} catch (Exception e) {
			return null;
		}
	}
	
	private List<Window> getWindowCheckResult(CompletionService<Window> windowCheckTasks, int windowNumber) {
        List<Window> windowList = new ArrayList<Window>();
        for(int i=0;i<windowNumber; i++) {
        	try {
        		windowList.add(windowCheckTasks.take().get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
        }
        Collections.sort(windowList);
        return windowList;
	}
	
	private Long getCurrentTime() {
		return (long) capture.get(0);
	}
	
	private PeopleStatus getPeopleStatus(Mat frame){
		Mat resizeFrame= ImageUtil.resizeMat(frame, 100, 200);
		Mat fgGray = getForeground(resizeFrame);
		int peopleArea = ImageUtil.getForegroundSize(fgGray);
		return peopleArea>10 ? PeopleStatus.EXIST : PeopleStatus.NOT_EXIST;
	}
	
	private Mat getForeground(Mat frame) {
		Mat fgGray=new Mat();
		bgModel.apply(frame, fgGray, 0.0005);//���±���ģ�ͣ��õ�ǰ��
		Mat element=Imgproc.getStructuringElement(0, new Size(3,3));//��ʴ�ĽṹԪ��
		Imgproc.erode(fgGray, fgGray, element);//��ʴ
		return fgGray;
	}
	
	private WindowStatus getWindowStatus(NormalizedLabel label, Mat m, Mat thresholded) {
    	int x=(int) (label.getX()*(double)m.cols());
    	int y=(int) (label.getY()*(double)m.rows());
    	int width=(int) (label.getWidth()*(double)m.cols());
    	int height=(int) (label.getHeight()*(double)m.rows());
    	int MaxX=(x+width>m.cols()-1)?(m.cols()-1):(x+width);
    	int MaxY=(y+height>m.rows()-1)?(m.rows()-1):(y+height);
    	int redPixelCount=0;
    	for(int r=y;r<=MaxY;r++){
    		for(int c=x;c<MaxX;c++){
    			if(thresholded.get(r, c)[0]>0){
    				redPixelCount++;
    			}
    		}
    	}
    	return redPixelCount>100 ? WindowStatus.CLOSE : WindowStatus.OPEN;
	}

}

