package com.demo.util;

import java.awt.image.BufferedImage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class ImageUtil {
	
	public static Mat getRedRegionMask(Mat image) {
		Mat hsvImage=new Mat();
        Imgproc.GaussianBlur(image, image, new Size(3,3),0,0); 
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);  
        Mat redRegionMask =new Mat();//��ż�����ɫ֮���ͼ��
        Core.inRange(hsvImage, new Scalar(0,90,90), new Scalar(20,255,255), redRegionMask);
        return redRegionMask;
	}
	
	/**
	 * Opencv�е�Mat��ʽת����JavaFx��֧����ʾ��Image��ʽ
	 * @param matrix Ҫ����ת����Mat��ʽ��ͼƬ
	 * @return ת����JavaFxImage��ʽ��ͼƬ
	 * 2017��5��6��15:13:13
	 */
	public static Image matToJavaFxImage(Mat matrix) {  
        int cols = matrix.cols();  
        int rows = matrix.rows();  
        int elemSize = (int) matrix.elemSize();  
        byte[] data = new byte[cols * rows * elemSize];  
        int type;  
        matrix.get(0, 0, data);  
        switch (matrix.channels()) {  
        case 1:  
            type = BufferedImage.TYPE_BYTE_GRAY;  
            break;  
        case 3:  
            type = BufferedImage.TYPE_3BYTE_BGR;  
            // bgr to rgb  
            byte b;  
            for (int i = 0; i < data.length; i = i + 3) {  
                b = data[i];  
                data[i] = data[i + 2];  
                data[i + 2] = b;  
            }  
            break;  
        default:  
            return null;  
        }  
        BufferedImage image2 = new BufferedImage(cols, rows, type);  
        image2.getRaster().setDataElements(0, 0, cols, rows, data);  
        
        Image javafxImage=SwingFXUtils.toFXImage(image2, null);
        return javafxImage;  
    }
	
	public static Mat resizeMat(Mat src, int height, int width) {
		Mat des=new Mat(height,width,src.type());
		Imgproc.resize(src, des, des.size());
		return des;
	}
	
	public static int getForegroundSize(Mat fgGray) {
		int foregroundSize=0;
		for(int i=0;i<fgGray.rows();i++){//����ǰ��ͼ���ҳ���ɫ��ǰ�������ص�ĸ���
			for(int j=0;j<fgGray.cols();j++){
				double pixelValue=fgGray.get(i, j)[0];
				if(pixelValue>0){//��ɫ��
					foregroundSize++;
				}
			}
		}
		return foregroundSize;
	}
}
