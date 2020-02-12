package com.appbestsmile.voicelikeme.recordingservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

import com.appbestsmile.voicelikeme.db.RecordItemDataSource;
import com.appbestsmile.voicelikeme.db.RecordingItem;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.appbestsmile.voicelikeme.AppConstants;
import com.appbestsmile.voicelikeme.db.RecordItemDataSource;
import com.appbestsmile.voicelikeme.db.RecordingItem;
import com.appbestsmile.voicelikeme.noisereduction.FrequencyDomainFilterInterface;
import com.appbestsmile.voicelikeme.noisereduction.HanningWindow;
import com.appbestsmile.voicelikeme.noisereduction.MFCC;
import com.appbestsmile.voicelikeme.noisereduction.Spectrum;
import com.appbestsmile.voicelikeme.noisereduction.WavToPCM;
import com.appbestsmile.voicelikeme.noisereduction.WavToPCM.WavInfo;
import com.appbestsmile.voicelikeme.noisereduction.STFT;
import com.appbestsmile.voicelikeme.noisereduction.FFT;
import com.appbestsmile.voicelikeme.noisereduction.WindowFunction;
import com.appbestsmile.voicelikeme.noisereduction.SpectralWhiteningFilter;


class AudioSaveHelper {

  private final RecordItemDataSource recordItemDataSource;
  private FileOutputStream os;
  private File mFile;
  private int mRecordSampleRate;
  private STFT noise_stft;

  @Inject
  public AudioSaveHelper(RecordItemDataSource recordItemDataSource) {
    this.recordItemDataSource = recordItemDataSource;
  }

  public void createNewFile() {
    Log.i("TEsting", "creating file");
    String storeLocation = Environment.getExternalStorageDirectory().getAbsolutePath();

    File folder = new File(storeLocation + "/SoundRecorder");
    if (!folder.exists()) {
      folder.mkdir();
    }

//    int count = 0;
//    long timestamp = new Date().getTime();
    String strNow = android.text.format.DateFormat.format("yyyyMMdd_HHmmss", new java.util.Date()).toString();


//    do {
//      count++;
//      fileName = "Voice_"
//              + (recordItemDataSource.getRecordingsCount() + count)
//              + Constants.AUDIO_RECORDER_FILE_EXT_WAV;
//      String mFilePath = storeLocation + "/SoundRecorder/" + fileName;
//      mFile = new File(mFilePath);
//    } while (mFile.exists() && !mFile.isDirectory());


//    String fileName = Constants.AUDIO_RECORDER_FILE_PREFIX + timestamp + Constants.AUDIO_RECORDER_FILE_EXT_WAV;

    String fileName = strNow + Constants.AUDIO_RECORDER_FILE_EXT_WAV;
    String mFilePath = storeLocation + "/SoundRecorder/" + fileName;
    mFile = new File(mFilePath);

    try {
      os = new FileOutputStream(mFile);

      writeWavHeader(os, Constants.RECORDER_CHANNELS, mRecordSampleRate, Constants.RECORDER_AUDIO_ENCODING);

    } catch (IOException e) {
      // TODO: 4/9/17 handle this
      e.printStackTrace();
    }
  }

  public void onDataReady(byte[] data) {
    try {
      AppConstants.speex.preprocess(data);

      os.write(data, 0, data.length);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void onRecordingStopped(AudioRecorder.RecordTime currentRecordTime) {
    try {
      os.close();
      updateWavHeader(mFile);

      //Noise Reduction
//      noiseRemoval(mFile);

//      noiseRemovalWithPyton(mFile);
      saveFileDetails(currentRecordTime);
      System.out.println("Record Complete. Saving and closing");
    } catch (IOException e) {
      mFile.deleteOnExit();
      e.printStackTrace();
    }
  }

  @SuppressLint("LongLogTag")
  public void noiseRemovalWithPyton(File rawWav) throws IOException {
    String strFileName = mFile.getName();
    String strFilePath = mFile.getPath();

    Log.d("noiseRemovalWithPyton : ", strFileName);
    Log.d("noiseRemovalWithPyton : ", strFilePath);

// Testing python codes
//    if (! Python.isStarted()) {
//      Python.start(new AndroidPlatform(AppConstants.applicationContext));
//    }
//
//    Python py = Python.getInstance();
//    PyObject myClass = py.getModule("main");
//    myClass.callAttr("test_all_features");
  }


  /*public void noiseRemoval(File rawWav) throws IOException {

    Context applicationContext = AppConstants.applicationContext;


    try{

      DecimalFormat df = new DecimalFormat("#.########");
      System.out.println("rawwav" + rawWav);
      InputStream noiseInputStream = applicationContext.getResources().openRawResource(
              applicationContext.getResources().getIdentifier("cafe_short_",
                      "raw", applicationContext.getPackageName()));

      FileInputStream externalFileInputStream = new FileInputStream(rawWav);

      //get information about the noise and raw WAV file
      WavInfo rawInfo = WavToPCM.readHeader(externalFileInputStream);
      WavInfo noiseInfo = WavToPCM.readHeader(noiseInputStream);

      //get the noise and raw PCM data
      ByteBuffer rawPcm = ByteBuffer.wrap(WavToPCM.readWavPcm(rawInfo, externalFileInputStream));
      ByteBuffer noisePcm = ByteBuffer.wrap(WavToPCM.readWavPcm(noiseInfo, noiseInputStream));

      // calculate the raw wav
      byte[] arrRaw = rawPcm.array();
      short[] shortRaw = new short[arrRaw.length/2];
      ByteBuffer.wrap(arrRaw).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortRaw);
      float[] audio_clip = new float[shortRaw.length];
      for(int i = 0; i < shortRaw.length;i++){
        audio_clip[i] = (float)(Float.parseFloat(df.format(shortRaw[i]/32768.0)));
      }

      // calculate the noise wav
      byte[] arrNoise = noisePcm.array();
      System.out.println("\nbyte array1: " + Arrays.toString(arrNoise));
      short[] shortNoise = new short[arrNoise.length/2];
      ByteBuffer.wrap(arrNoise).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortNoise);
      float[] noise_clip = new float[shortNoise.length];
      for(int i = 0; i < shortNoise.length;i++){
        noise_clip[i] = (float)(Float.parseFloat(df.format(shortNoise[i]/32768.0)));
      }

      int snr = 50;  // signal to noise ratio
      for(int i = 0; i < noise_clip.length;i++){
        noise_clip[i] = noise_clip[i]/snr;
      }
      System.out.println("\nnoise_clip" + Arrays.toString(noise_clip));

      MFCC mfcc = new MFCC();
      mfcc.process(convertToDouble(noise_clip));

      noise_stft = new STFT(2048,512,512, new HanningWindow() ,44100);
      noise_stft.forward(convertToDouble(noise_clip));
      noise_stft.applyFilter(new SpectralWhiteningFilter(noise_stft.getWindowsize(), noise_stft.getSamplingrate(), 0.33f, 30));
      List<Spectrum> magnitude = noise_stft.getMagnitudeSpectrum();

      STFT raw_stft = new STFT(2048,75,512, new HanningWindow() ,44100);
      raw_stft.forward(convertToDouble(audio_clip));
      System.out.println("\nstft_array1" + raw_stft.getStft().toString());


      //amplitude to dB
      //power_db = 20 * log10(amp / amp_ref);



      // print the byte array
//      System.out.println("\nbyte audio_clip: " + Arrays.toString(audio_clip));
//      System.out.println("\nbyte short_noise: " + Arrays.toString(shortNoise));
//      System.out.println("\nbyte noise_clip: " + Arrays.toString(noise_clip));
//      System.out.println("\nbyte noise_clip_length1: " + noise_clip.length);
//      System.out.println("\nbyte audio_clip_length: " + audio_clip.length);

    }catch (Exception e){
      Log.d("error", e.toString());
    }
  }*/


  public static double[] convertToDouble(float[] inputArray)
  {
    if (inputArray== null)
      return null;

    double[] output = new double[inputArray.length];
    for (int i = 0; i < inputArray.length; i++)
      output[i] = inputArray[i];

    return output;
  }
//  public List<ComplexArray> reduce_noise(float[] audio_clip,float[] noise_clip,Boolean use_tensorflow,Boolean verbose){

//      final int RECORDER_AGC_OFF = MediaRecorder.AudioSource.VOICE_RECOGNITION;
//      int audioSourceId = RECORDER_AGC_OFF;
//      int sampleRate = 16000;
//      int fftLen = 2048;
//      int hopLen = 1024;
//      double overlapPercent = 50;  // = (1 - hopLen/fftLen) * 100%
//      String wndFuncName;
//      int nFFTAverage = 2;
//      boolean isAWeighting = false;
//      final int BYTE_OF_SAMPLE = 2;
//      final double SAMPLE_VALUE_MAX = 32767.0;   // Maximum signal value
//      double spectrogramDuration = 4.0;
//
//      double[] micGainDB = null;  // should have fftLen/2+1 elements, i.e. include DC.
//      String calibName = null;



//
//    //define the constants
//    int n_grad_freq = 2;//how many frequency channels to smooth over with the mask.
//    int n_grad_time = 4;//how many time channels to smooth over with the mask.
//    int n_fft = 2048;//number audio of frames between STFT columns.
//    int win_length = 2048;//Each frame of audio is windowed by `window()`. The window will be of length `win_length` and then padded with zeros to match `n_fft`..
//    int hop_length = 512;//number audio of frames between STFT columns.
//    double n_std_thresh = 1.5;//how many standard deviations louder than the mean dB of the noise (at each frequency level) to be considered signal
//    double prop_decrease = 1.0;//To what extent should you decrease noise (1 = all, 0 = none)
//
//    // STFT over noise
//    //MFCC mfccConvert = new MFCC();
////    System.out.println("aaaaaa" + Arrays.toString(convertFloatsToDoubles(noise_clip)));
////    float[] rawStft = mfcc.process(convertFloatsToDoubles(audio_clip));
//    List<ComplexArray> magnitudeSpectrum = STFT.transform(convertFloatsToDoubles(noise_clip),2048);
//
//    System.out.println("hello are you there?");
//    System.out.println("\nbyte stft_array_length: " + magnitudeSpectrum.toString());
//    return magnitudeSpectrum;
//  }

  private void saveFileDetails(AudioRecorder.RecordTime currentRecordTime) {

    if(currentRecordTime == null){
      Log.d("AudioSaveHelper", "Record time too short");
      return;
    }

    RecordingItem recordingItem = new RecordingItem();
    recordingItem.setName(mFile.getName());
    recordingItem.setFilePath(mFile.getPath());
    recordingItem.setTime(System.currentTimeMillis());
    recordingItem.setLength(currentRecordTime.millis);
    recordItemDataSource.insertNewRecordItem(recordingItem);
  }

  /**
   * Writes the proper 44-byte RIFF/WAVE header to/for the given stream
   * Two size fields are left empty/null since we do not yet know the final stream size
   *
   * @param out The stream to write the header to
   * @param channelMask An AudioFormat.CHANNEL_* mask
   * @param sampleRate The sample rate in hertz
   * @param encoding An AudioFormat.ENCODING_PCM_* value
   * @throws IOException
   */
  private void writeWavHeader(OutputStream out, int channelMask, int sampleRate, int encoding)
          throws IOException {
    short channels;
    switch (channelMask) {
      case AudioFormat.CHANNEL_IN_MONO:
        channels = 1;
        break;
      case AudioFormat.CHANNEL_IN_STEREO:
        channels = 2;
        break;
      default:
        throw new IllegalArgumentException("Unacceptable channel mask");
    }

    short bitDepth;
    switch (encoding) {
      case AudioFormat.ENCODING_PCM_8BIT:
        bitDepth = 8;
        break;
      case AudioFormat.ENCODING_PCM_16BIT:
        bitDepth = 16;
        break;
      case AudioFormat.ENCODING_PCM_FLOAT:
        bitDepth = 32;
        break;
      default:
        throw new IllegalArgumentException("Unacceptable encoding");
    }

    writeWavHeader(out, channels, sampleRate, bitDepth);
  }

  /**
   * Writes the proper 44-byte RIFF/WAVE header to/for the given stream
   * Two size fields are left empty/null since we do not yet know the final stream size
   *
   * @param out The stream to write the header to
   * @param channels The number of channels
   * @param sampleRate The sample rate in hertz
   * @param bitDepth The bit depth
   * @throws IOException
   */
  private void writeWavHeader(OutputStream out, short channels, int sampleRate, short bitDepth)
          throws IOException {
    // Convert the multi-byte integers to raw bytes in little endian format as required by the spec
    byte[] littleBytes = ByteBuffer.allocate(14)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(channels)
            .putInt(sampleRate)
            .putInt(sampleRate * channels * (bitDepth / 8))
            .putShort((short) (channels * (bitDepth / 8)))
            .putShort(bitDepth)
            .array();

    // Not necessarily the best, but it's very easy to visualize this way
    out.write(new byte[] {
            // RIFF header
            'R', 'I', 'F', 'F', // ChunkID
            0, 0, 0, 0, // ChunkSize (must be updated later)
            'W', 'A', 'V', 'E', // Format
            // fmt subchunk
            'f', 'm', 't', ' ', // Subchunk1ID
            16, 0, 0, 0, // Subchunk1Size
            1, 0, // AudioFormat
            littleBytes[0], littleBytes[1], // NumChannels
            littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
            littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
            littleBytes[10], littleBytes[11], // BlockAlign
            littleBytes[12], littleBytes[13], // BitsPerSample
            // data subchunk
            'd', 'a', 't', 'a', // Subchunk2ID
            0, 0, 0, 0, // Subchunk2Size (must be updated later)
    });
  }

  /**
   * Updates the given wav file's header to include the final chunk sizes
   *
   * @param wav The wav file to update
   * @throws IOException
   */
  private void updateWavHeader(File wav) throws IOException {
    byte[] sizes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
            // There are probably a bunch of different/better ways to calculate
            // these two given your circumstances. Cast should be safe since if the WAV is
            // > 4 GB we've already made a terrible mistake.
            .putInt((int) (wav.length() - 8)) // ChunkSize
            .putInt((int) (wav.length() - 44)) // Subchunk2Size
            .array();

    RandomAccessFile accessWave = null;
    //noinspection CaughtExceptionImmediatelyRethrown
    try {
      accessWave = new RandomAccessFile(wav, "rw");
      // ChunkSize
      accessWave.seek(4);
      accessWave.write(sizes, 0, 4);

      // Subchunk2Size
      accessWave.seek(40);
      accessWave.write(sizes, 4, 4);
    } catch (IOException ex) {
      // Rethrow but we still close accessWave in our finally
      throw ex;
    } finally {
      if (accessWave != null) {
        try {
          accessWave.close();
        } catch (IOException ex) {
          //
        }
      }
    }
  }

  public void setSampleRate(int sampleRate) {
    this.mRecordSampleRate = sampleRate;
  }
}
