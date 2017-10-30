package com.sergiohilgert.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
  
  ArrayList<String> celebUrls = new ArrayList<String>();
  ArrayList<String> celebNames = new ArrayList<String>();
  int chosenCeleb = 0;
  int correctAnswer;
  String[] answers = new String[4];
  ImageView imageView;
  Button button1;
  Button button2;
  Button button3;
  Button button4;
  
  public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{
  
    @Override
    protected Bitmap doInBackground(String... strings) {
      try {
        URL url = new URL(strings[0]);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.connect();
        InputStream inputStream = httpURLConnection.getInputStream();
        Bitmap result = BitmapFactory.decodeStream(inputStream);
        return  result;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }
  }
  
  public class DownloadTask extends AsyncTask<String, Void, String>{
  
    @Override
    protected String doInBackground(String... strings) {
      String result = "";
      URL url;
      HttpURLConnection httpURLConnection = null;
      
      try{
        url = new URL(strings[0]);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = httpURLConnection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        int data = reader.read();
        char current;
        while(data != -1) {
          current = (char)data;
          result += current;
          data = reader.read();
        }
        return result;
      }catch (Exception e){
        e.printStackTrace();
      }
      
      return null;
    }
  }
  
  private void restart(){
    
    Random random = new Random();
    chosenCeleb = random.nextInt(celebNames.size());
    ImageDownloader imageTask = new ImageDownloader();
    Bitmap celebImage = null;
    try {
      celebImage = imageTask.execute(celebUrls.get(chosenCeleb)).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    imageView.setImageBitmap(celebImage);
    correctAnswer = random.nextInt(4);
    int incorrect = random.nextInt(celebNames.size());
    for(int i = 0; i < 4; ++i){
      if(i == correctAnswer){
        answers[i] = celebNames.get(chosenCeleb);
      }else{
        while(incorrect == chosenCeleb || already_in_array(celebNames.get(incorrect))){
          incorrect = random.nextInt(celebUrls.size());
        }
        answers[i] = celebNames.get(incorrect);
      }
    }
    button1.setText(answers[0]);
    button2.setText(answers[1]);
    button3.setText(answers[2]);
    button4.setText(answers[3]);
  }
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    imageView = (ImageView) findViewById(R.id.imageView);
    button1 = (Button) findViewById(R.id.button1);
    button2 = (Button) findViewById(R.id.button2);
    button3 = (Button) findViewById(R.id.button3);
    button4 = (Button) findViewById(R.id.button4);
    
    DownloadTask downloadTask = new DownloadTask();
    String result = null;
    try {
      result = downloadTask.execute("http://www.posh24.se/kandisar").get();
      String[] splitResult = result.split("<div class=\"sidebarContainer\">");
      Pattern pattern = Pattern.compile("<img src=\"(.*?)\"");
      Matcher matcher = pattern.matcher(splitResult[0]);
      
      while(matcher.find()){
        celebUrls.add(matcher.group(1));
      }
      pattern = Pattern.compile("alt=\"(.*?)\"");
      matcher = pattern.matcher(splitResult[0]);
      
      while(matcher.find()){
        celebNames.add(matcher.group(1));
      }
      
      restart();
      
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
  boolean already_in_array(String str){
    for(int i = 0; i < 4; ++i){
      if(answers[i] != null && answers[i].equals(str))
        return true;
    }
    return false;
  }
  
  public void celebChosen(View view){
    if(view.getTag().equals(String.valueOf(correctAnswer+1))){
      Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
    }else{
      Toast.makeText(getApplicationContext(), "Wrong! It was: " + answers[correctAnswer], Toast.LENGTH_LONG).show();
    }
    restart();
  }
}
