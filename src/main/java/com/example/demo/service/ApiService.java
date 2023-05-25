package com.example.demo.service;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

@Service
public class ApiService {
	
	@Value("${video.uploadPath}")
	private String uploadPath;
	
	@Value("${video.ffmpegPath}")
	private String ffmpegPath;
	
	@Value("${video.ffprobePath}")
	private String ffprobePath;
	
public void uploadFile(MultipartFile vd , String checkSize) throws Exception {
		MultipartFile mFile = vd;
		Map<String, Object> videoInfo = new HashMap<>();
		
		String filePath = uploadPath;
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
//				videoInfo = getVideoInfo(fileName);
//				System.out.println("============================");
//				System.out.println(videoInfo);
//				System.out.println("fileName > " + fileName);
//				System.out.println("============================");
				videoEffectAddon(fileName);
				// videoMinify(fileName);
			}
		
    }

public void videoMinify(String fileName) throws IOException {
	 FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
	 FFprobe ffprobe = new FFprobe(ffprobePath);

	 FFmpegBuilder builder = new FFmpegBuilder()

		   .setInput( uploadPath + fileName)     // Filename, or an FFMPEGProbeResult
		   .overrideOutputFiles(true) // Override the output if it exists
	
		   .addOutput( uploadPath + fileName + "(format).mp4")   // Filename for the destination
		     .setFormat("mp4")        // Format is inferred from filename, or can be set
		     //.setTargetSize(1000_000)  // Aim for a 250KB file
	
		     .disableSubtitle()       // No subtiles
	
		     .setAudioChannels(1)         // Mono audio
		     .setAudioCodec("aac")        // using the aac codec
		     .setAudioSampleRate(48_000)  // at 48KHz
		     .setAudioBitRate(32768)      // at 32 kbit/s
	
		     .setVideoCodec("libx264")     // Video using x264
		     .setVideoFrameRate(24, 1)     // at 24 frames per second
		     .setVideoResolution(1920, 1080) // at 100x100 resolution
	
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

	 
	public void videoEffectAddon(String fileName) throws IOException {
	  FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
	  FFmpegBuilder builder = new FFmpegBuilder()
	          .addInput(uploadPath + "test.png") // 배경 이미지 입력 파일
	          .addInput(uploadPath + fileName) // 주 영상 입력 파일
	          .addExtraArgs("-filter_complex", "[0:v]colorkey=green[transper];[1:v][transper]overlay[out]") // 필터 설정
	          .addOutput(uploadPath + "result.mp4") // 출력 파일
	          .addExtraArgs("-map", "[out]") // 출력 스트림 매핑
	          .done();
	
	  FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
	  try {
	      executor.createJob(builder).run();
	      System.out.println("FFmpeg 실행 완료");
	  } catch (Exception e) {
	      e.printStackTrace();
	  }
	}

	 
	 
	 public Map<String,Object> getVideoInfo(String fileName) throws IOException {
		 Map<String,Object> result = new HashMap<String, Object>();
		 ObjectMapper objectMapper = new ObjectMapper();
		 
		 FFprobe ffprobe = new FFprobe(ffprobePath);
		 System.out.println("uploadPath + fileName > " + uploadPath + fileName);
		 FFmpegProbeResult probeResult = ffprobe.probe(uploadPath + fileName);
		 
		 FFmpegFormat format = probeResult.getFormat();
		 String formatString = objectMapper.writeValueAsString(format);
		 Map<String,Object> fResult = JsonUtils.jsonToMap(formatString);
		 
		 //System.out.format("%nFile: '%s' ; Format: '%s' ; Duration: %.3fs", format.filename, format.format_long_name,format.duration);
		 if(fResult != null) {
			 result.putAll(fResult);
		 }
			
		 FFmpegStream stream = probeResult.getStreams().get(0);
		 String streamString = objectMapper.writeValueAsString(stream);
		 streamString = streamString.replace("\"r_frame_rate\":"+stream.r_frame_rate, "\"r_frame_rate\":\"" + stream.r_frame_rate + '"');
		 streamString = streamString.replace("\"avg_frame_rate\":"+stream.avg_frame_rate, "\"avg_frame_rate\":\"" + stream.avg_frame_rate + '"');
		 streamString = streamString.replace("\"time_base\":"+stream.time_base, "\"time_base\":\"" + stream.time_base + '"');
		 
		 
		 Map<String,Object> sResult = JsonUtils.jsonToMap(streamString);
		 //System.out.format("%nCodec: '%s' ; Width: %dpx ; Height: %dpx",stream.codec_long_name,stream.width,stream.height);
		 
		 if(sResult != null) {
			 result.putAll(sResult);
		 }
		 
		 return result;
			 
	 }
	 
	 /*
	 @GetMapping(value = "/{fileName}", produces = "application/octet-stream")
	public ResponseEntity<ResourceRegion> streamVideo(@PathVariable String fileName,
	                                                  @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
	    String FILE_DIRECTORY = uploadPath + fileName;
	    File videoFile = new File(FILE_DIRECTORY);
	    if (!videoFile.exists()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    }

	    // 비디오 파일을 읽어들입니다.
	    RandomAccessFile randomAccessFile = new RandomAccessFile(videoFile, "r");
	    long videoLength = randomAccessFile.length();

	    // 비디오 청크의 시작 위치와 크기를 계산합니다.
	    long start = 0, end = videoLength - 1;
	    if (rangeHeader != null && !rangeHeader.isEmpty()) {
	        String[] ranges = rangeHeader.split("=");
	        if (ranges.length > 1) {
	            String[] parts = ranges[1].split("-");
	            try {
	                if (parts.length > 1) {
	                    start = Long.parseLong(parts[0]);
	                    end = Long.parseLong(parts[1]);
	                } else {
	                    start = Long.parseLong(parts[0]);
	                    end = videoLength - 1;
	                }
	            } catch (NumberFormatException e) {
	                start = 0;
	                end = videoLength - 1;
	            }
	        }
	    }

	    // 비디오 청크를 생성합니다.
	    long contentLength = end - start + 1;
	    InputStream inputStream = new BufferedInputStream(new FileInputStream(videoFile));
	    inputStream.skip(start);
	    inputStream.mark(Integer.MAX_VALUE);
	    ResourceRegion region = new ResourceRegion(new InputStreamResource(inputStream), 0, contentLength) {
	        @Override
	        public InputStream getInputStream() throws IOException {
	            InputStream inputStream = new BufferedInputStream(new FileInputStream(videoFile));
	            inputStream.skip(start);
	            inputStream.mark(Integer.MAX_VALUE);
	            return inputStream;
	        }
	    };

	    // HTTP 응답 헤더를 설정합니다.
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("Content-Type", "video/mp4");
	    headers.set("Accept-Ranges", "bytes");
	    headers.set("Content-Length", String.valueOf(contentLength));
	    if (rangeHeader != null && !rangeHeader.isEmpty()) {
	        headers.set("Content-Range", "bytes " + start + "-" + end + "/" + videoLength);
	        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
	                             .headers(headers)
	                             .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                             .body(region);
	    } else {
	        headers.set("Content-Range", "bytes 0-" + (videoLength - 1) + "/" + videoLength);
	        return ResponseEntity.status(HttpStatus.OK)
	                             .headers(headers)
	                             .contentType(MediaType.APPLICATION_OCTET_STREAM)
	                             .body(region);
	    }
	}
	
	  */
	 
	 
	 public String save(Map<String, MultipartFile> files , String tc , String qrcodeUrl , String qrCodeLocation) throws Exception {
		  String resultFileName = "";
			String filePath = uploadPath;
			String saveFileName = "", savaFilePath = "";
			List<String> fileList = new ArrayList<>();
		 for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
       String paramName = entry.getKey();
       MultipartFile file = entry.getValue();
       // 파일 처리 로직 수행
	 			String fileName = file.getOriginalFilename();
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
					
					file.transferTo(new File(savaFilePath));
				} else {
					file.transferTo(saveFile);
				}
				
				fileList.add(fileName); 
		 	}
		 
		 Date now = new Date();
		 SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss_S");
		 String tempName = format.format(now) + ".mp4";
		 
		 FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
		 FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
		  
		 if(tc.equals("1")) {
	      FFmpegBuilder builder = new FFmpegBuilder()
					      	  .addInput(uploadPath + fileList.get(0).toString())
					          .addInput(uploadPath + fileList.get(1).toString())
					          .addInput(uploadPath + fileList.get(2).toString())
					          .addInput(uploadPath + fileList.get(3).toString())
					          .setComplexFilter("[0:v][1:v]hstack=inputs=2[top];[2:v][3:v]hstack=inputs=2[bottom];[top][bottom]vstack=inputs=2[v]")
					          .addOutput(uploadPath + tempName)
					          .setVideoCodec("libx264")
					          .setConstantRateFactor(10)
					          .addExtraArgs("-pix_fmt", "yuv420p", "-map", "[v]")
					          .done();
	 		 // Run a one-pass encode
	      /*
	 		 executor.createJob(builder ,new ProgressListener() {
	 	
	 	    // Using the FFmpegProbeResult determine the duration of the input
	 	
	 	    @Override
	 	    public void progress(Progress progress) {
	 	    	System.out.println("progress > " + progress.toString());
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
	 		 */
	 	   executor.createJob(builder).run();
	 	  resultFileName = tempName;
			  
		 }else if (tc.equals("2")){
			 //qrcodeUrl,qrCodeLocation
			 int qrSize = 200;
       ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L; // 오류 수정 레벨 (L, M, Q, H)
       QRCodeWriter qrCodeWriter = new QRCodeWriter();
       BitMatrix bitMatrix = qrCodeWriter.encode(qrcodeUrl, BarcodeFormat.QR_CODE, qrSize, qrSize);
       
       // BufferedImage 생성 및 그래픽스2D 객체 가져오기
       BufferedImage qrCodeImage = new BufferedImage(qrSize, qrSize, BufferedImage.TYPE_INT_RGB);
       Graphics2D graphics = (Graphics2D) qrCodeImage.getGraphics();
       
       // QR 코드 이미지 커스터마이징
       graphics.setColor(Color.WHITE); // 배경 색상 설정
       graphics.fillRect(0, 0, qrSize, qrSize);
       graphics.setColor(Color.BLACK); // QR 코드 색상 설정
       
       // QR 코드 그리기
       for (int x = 0; x < qrSize; x++) {
           for (int y = 0; y < qrSize; y++) {
               if (bitMatrix.get(x, y)) {
                   graphics.fillRect(x, y, 1, 1);
               }
           }
       }
       
       // 그래픽스 자원 해제
       graphics.dispose();
       
       // QR 코드 이미지 파일로 저장
       String qrCodeFileName = format.format(now) + "qrCode.png";
       File qrCodeFile = new File(filePath + qrCodeFileName);
       ImageIO.write(qrCodeImage, "png", qrCodeFile);
       
       String overlay = "";
       switch (qrCodeLocation) {
       case "leftTop":
      	 overlay = "overlay=0:0";
				break;
       case "leftBottom":
      	 overlay = "overlay=0:main_h-overlay_h";
				break;
       case "centor":
      	 overlay = "overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2";
				break;
       case "rightTop":
      	 overlay = "overlay=main_w-overlay_w:0";
				break;
       case "rightBottom":
      	 overlay = "overlay=main_w-overlay_w:main_h-overlay_h";
				break;

			default:
				overlay = "overlay=0:0";
				break;
			}
       
       
       FFmpegBuilder builder = new FFmpegBuilder()
           .addInput(uploadPath + fileList.get(0).toString())
           .addInput(uploadPath + qrCodeFileName)
           .setComplexFilter("[1][0]scale2ref=oh*mdar:ih*0.2[logo][video];[video][logo]" + overlay)
           .addOutput(uploadPath + tempName)
           .done();

       executor.createJob(builder).run();
       resultFileName = tempName; 
		 }
		 
		 	return resultFileName;

		 
    }
	 
	 
	 
	 

}
