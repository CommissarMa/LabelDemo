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
	
	private VideoCapture capture;//视频读帧类
	private Mat nextMat;//下一帧图像
	
	private BackgroundSubtractorMOG2 bgModel;
	
	public final static ExecutorService executor = Executors.newFixedThreadPool(5);
	
	/**
	 * 构造方法 
	 * @param videoPath 视频路径，注意传入的字符串格式：D://video//1.avi
	 * 2017年5月6日15:19:19
	 */
	public VideoAnalysisHandlerImpl(String videoPath) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);//加载Opencv库
		capture=new VideoCapture(videoPath);//加载视频
		bgModel=Video.createBackgroundSubtractorMOG2();//初始化背景模型
	}
	
	/**
	 * 获取视频的第一帧
	 * @return JavaFx Image格式的图像，返回null代表读取失败
	 * 2017年5月6日20:44:47
	 */
	@Override
	public Image getFirstFrame() {
		if(!capture.isOpened()){//如果读取不了视频，返回null
			return null;
		}else{
			Mat frame=new Mat();
			capture.read(frame);//第一帧
			nextMat=new Mat();
			capture.read(nextMat);//第二帧
			if(!frame.empty()){//图片读取成功
				Image image= ImageUtil.matToJavaFxImage(frame);
				return image;
			}else{//读取到视频结尾，返回null
				return null;
			}
		}
	}
	
	/**
	 * 判断是否还能继续分析（即视频是不是到结尾了）
	 * @return true代表能够继续分析；false代表不能继续分析
	 * 2017年5月6日19:52:30
	 */
	@Override
	public boolean isAnalysisContinue() {
		if(!nextMat.empty()){//Mat不为空
			return true;
		}else{
			capture.release();
			return false;
		}
	}

	/**
	 * 对当前帧进行异常分析，并返回分析结果
	 * @return FrameAnalysisResult 分析结果类，包括处理后的Image，异常的具体字符串等等
	 * 2017年5月6日21:08:46
	 */
	@Override
	public FrameAnalysisResult getCurrentFrameAnalyisResult(List<NormalizedLabel> labelList) {
		Mat currentSrcMat = nextMat.clone();//当前帧
		Mat peopleMat = nextMat.clone();//这个副本用来进行人的检测，并进行绘制
		Image currentSrcImage = ImageUtil.matToJavaFxImage(currentSrcMat);//当前帧的JavaFx Image格式
		
		Future<PeopleStatus> peopleCheckTask = launchPeopleCheckTask(peopleMat);
		
		CompletionService<Window> windowCheckTasks = launchWindowCheckTasks(labelList, currentSrcMat);
		
		PeopleStatus peopleStatus = getPeopleCheckResult(peopleCheckTask);
		
		List<Window> windowList = getWindowCheckResult(windowCheckTasks, labelList.size());
		
		long currentTime = getCurrentTime();
		
		capture.read(nextMat);//读取下一帧
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
		bgModel.apply(frame, fgGray, 0.0005);//更新背景模型，得到前景
		Mat element=Imgproc.getStructuringElement(0, new Size(3,3));//腐蚀的结构元素
		Imgproc.erode(fgGray, fgGray, element);//腐蚀
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

