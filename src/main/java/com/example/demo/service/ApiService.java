package com.example.demo.service;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

@Service
public class ApiService {
	
public void uploadFile(MultipartFile vd , String checkSize) throws Exception {
		MultipartFile mFile = vd;
		
		String filePath = System.getProperty("user.dir") + "\\uploads\\";
		String saveFileName = "", savaFilePath = "";
		
			
			String fileName = mFile.getOriginalFilename();
			System.out.println("fileName > " + fileName);
			String fileCutName = fileName.substring(0, fileName.lastIndexOf("."));
			String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
			String saveFilePath = filePath + File.separator + fileName;
			File fileFolder = new File(filePath);
			
			if (!fileFolder.exists()) {
				if (fileFolder.mkdirs()) {
					System.out.println("[file.mkdirs] : Success");
				} else {
					System.out.println("[file.mkdirs] : Fail");
				}
			}
			
			File saveFile = new File(saveFilePath);
			if (saveFile.isFile()) {
				boolean _exist = true;
				
				int index = 0;
				while (_exist) {
					index++;
					
					saveFileName = fileCutName + "(" + index + ")." + fileExt;
					
					String dictFile = filePath + File.separator + saveFileName;
					
					_exist = new File(dictFile).isFile();
					
					if (!_exist) {
						savaFilePath = dictFile;
					}
				}
				
    			mFile.transferTo(new File(savaFilePath));
			} else {
					mFile.transferTo(saveFile);
			}
			
			if(checkSize.equals("Y")) {
				videoMinify(fileName);
			}
		
    }

	 public void videoMinify(String fileName) throws IOException {
		 FFmpeg ffmpeg = new FFmpeg(System.getProperty("user.dir") + "\\ffmpeg\\ffmpeg");
		 FFprobe ffprobe = new FFprobe(System.getProperty("user.dir") + "\\ffmpeg\\ffprobe");

		 FFmpegBuilder builder = new FFmpegBuilder()

		   .setInput( System.getProperty("user.dir") + "\\uploads\\" + fileName)     // Filename, or an FFMPEGProbeResult
		   .overrideOutputFiles(true) // Override the output if it exists

		   .addOutput( System.getProperty("user.dir") + "\\uploads\\" + "output.mp4")   // Filename for the destination
		     .setFormat("mp4")        // Format is inferred from filename, or can be set
		     .setTargetSize(1000_000)  // Aim for a 250KB file

		     .disableSubtitle()       // No subtiles

		     .setAudioChannels(1)         // Mono audio
		     .setAudioCodec("aac")        // using the aac codec
		     .setAudioSampleRate(48_000)  // at 48KHz
		     .setAudioBitRate(32768)      // at 32 kbit/s

		     .setVideoCodec("libx264")     // Video using x264
		     .setVideoFrameRate(24, 1)     // at 24 frames per second
		     .setVideoResolution(100, 100) // at 100x100 resolution

		     .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
		     .done();

		 FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

		 // Run a one-pass encode
		 executor.createJob(builder ,new ProgressListener() {

       // Using the FFmpegProbeResult determine the duration of the input

       @Override
       public void progress(Progress progress) {

         // Print out interesting information about the progress
         System.out.println(
                 String.format(
                         "[%.0f%%] status:%s frame:%d time:%s fps:%.0f speed:%.2fx",
                         progress.status,
                         progress.frame,
                         FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                         progress.fps.doubleValue(),
                         progress.speed));
       }
     }).run();

		 // Or run a two-pass encode (which is better quality at the cost of being slower)
		 executor.createTwoPassJob(builder).run();
			
	 }

}
